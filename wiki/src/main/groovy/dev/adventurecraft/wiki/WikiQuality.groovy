package dev.adventurecraft.wiki

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.JsonException

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.regex.Pattern

/** Dependency-free checks for authored pages, generated output, and external links. */
class WikiQuality {
    static final Set<String> IMAGE_SUFFIXES = ['.avif', '.gif', '.jpeg', '.jpg', '.png', '.svg', '.webp'] as Set
    static final Map<String, String> MISSPELLINGS = [
        adress: 'address', begining: 'beginning', commnad: 'command', dependancy: 'dependency',
        enviroment: 'environment', occurence: 'occurrence', recieve: 'receive', seperate: 'separate',
        teh: 'the', wether: 'whether'
    ]
    private static final Pattern MARKDOWN_IMAGE = ~/!\[([^\]]*)\]\(([^)]+)\)/
    private static final Pattern MARKDOWN_LINK = ~/(?<!!)\[(?:[^\]\n]|\](?!\())*\]\(([^)\n]+)\)/
    private static final Pattern HTML_IMAGE = ~/(?i)<img\b([^>]*)>/
    private static final Pattern WIKI_LINK = ~/\[\[([^\]]+)\]\]/
    private static final Pattern PIPED_WIKI_LINK = ~/\[\[[^\]\n]*\|[^\]\n]*\]\]/
    private static final Pattern URL = ~/https?:\/\/[^\s<>"\]]+/
    private static final Pattern TOKEN = ~/\{\{[A-Z0-9_]+\}\}/

    static void checkSources(File directory) {
        failIssues(checkMarkdown(directory, true, true) + checkMedia(directory, false))
        println "Validated ${markdownFiles(directory).size()} authored Markdown pages."
    }

    static void checkQuality(File directory, int minimumPages = 250) {
        if (minimumPages < 1) {
            throw new IllegalStateException('minimumPages must be a positive integer')
        }
        failIssues(checkManifest(directory, minimumPages) + checkMarkdown(directory, false, false) +
            checkMedia(directory) + checkWikiLinks(directory))
        println "Validated generated wiki quality in ${directory.canonicalPath}."
    }

    /** Writes both report formats even when strict mode subsequently fails. */
    static Map checkLinks(File directory, File report, boolean softFail = true) {
        Map payload = writeLinkReport(directory, report)
        if (!softFail && payload.summary.broken) {
            throw new IllegalStateException("found ${payload.summary.broken} broken external link(s)")
        }
        return payload
    }

    static List<File> markdownFiles(File root) {
        if (root.isFile()) {
            return [root]
        }
        return treeFiles(root).findAll { it.isFile() && it.name.endsWith('.md') }
    }

    private static List<File> treeFiles(File root) {
        if (!root.isDirectory()) {
            return []
        }
        // Files.walk does not follow symbolic links, so validation cannot recurse outside its root.
        Files.walk(root.toPath()).withCloseable { paths ->
            paths.filter { it != root.toPath() }.map { it.toFile() }.toList().sort(false) { a, b ->
                displayPath(a, root) <=> displayPath(b, root)
            }
        }
    }

    private static String decodeUtf8(byte[] bytes) {
        StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
    }

    private static String readUtf8(File file) {
        try {
            return decodeUtf8(Files.readAllBytes(file.toPath()))
        } catch (CharacterCodingException error) {
            throw new IllegalStateException("${file}: is not valid UTF-8 (${error.message})", error)
        }
    }

    private static String displayPath(File file, File root) {
        def path = file.toPath().toAbsolutePath().normalize()
        def base = root.toPath().toAbsolutePath().normalize()
        return (path.startsWith(base) ? base.relativize(path).toString() : path.toString()).replace('\\', '/')
    }

    private static String suffix(File file) {
        int dot = file.name.lastIndexOf('.')
        return dot < 0 ? '' : file.name.substring(dot)
    }

    private static int lineNumber(String text, int position) {
        return text.substring(0, position).count('\n') + 1
    }

    static List<String> checkMarkdown(File root, boolean allowTokens, boolean spellcheck) {
        List<String> issues = []
        List<File> files = markdownFiles(root)
        if (!files) {
            return ["${root}: contains no Markdown files".toString()]
        }
        files.each { path ->
            String rel = displayPath(path, root)
            byte[] raw = Files.readAllBytes(path.toPath())
            String text
            try {
                text = decodeUtf8(raw)
            } catch (CharacterCodingException error) {
                issues << "${rel}: invalid UTF-8 (${error.message})".toString()
                return
            }
            if (raw.any { it == 13 }) issues << "${rel}: use LF, not CRLF, line endings".toString()
            if (raw.length && raw[-1] != 10) issues << "${rel}: missing final newline".toString()
            if (text.indexOf(0) >= 0) issues << "${rel}: contains a NUL byte".toString()
            if (!allowTokens) {
                TOKEN.matcher(text).findAll().toSet().sort().each {
                    issues << "${rel}: unresolved template token ${it}".toString()
                }
            }
            List<String> lines = text.readLines()
            if (lines.any { line -> ['<<<<<<<', '=======', '>>>>>>>'].any { line.startsWith(it) } }) {
                issues << "${rel}: contains a merge-conflict marker".toString()
            }
            boolean inFence = false
            String fenceChar = ''
            int previousHeading = 0
            int h1Count = 0
            String previousWord = ''
            int previousLine = 0
            lines.eachWithIndex { line, index ->
                int number = index + 1
                if (line.replaceFirst(/[ \t]+$/, '') != line) issues << "${rel}:${number}: trailing whitespace".toString()
                if (line.contains('\t')) issues << "${rel}:${number}: tab character; use spaces".toString()
                def fence = line =~ /^\s*(`{3,}|~{3,})/
                if (fence.find()) {
                    String current = fence.group(1).substring(0, 1)
                    if (!inFence) {
                        inFence = true
                        fenceChar = current
                    } else if (current == fenceChar) {
                        inFence = false
                        fenceChar = ''
                    }
                    previousWord = ''
                    return
                }
                if (inFence) return
                if (line.stripLeading().startsWith('|') && PIPED_WIKI_LINK.matcher(line).find()) {
                    issues << "${rel}:${number}: piped wiki link breaks Markdown table columns; use a standard Markdown link".toString()
                }
                def heading = line =~ /^(#{1,6})\s+\S/
                if (heading.find()) {
                    int level = heading.group(1).length()
                    if (level == 1) h1Count++
                    if (previousHeading && level > previousHeading + 1) {
                        issues << "${rel}:${number}: heading jumps from H${previousHeading} to H${level}".toString()
                    }
                    previousHeading = level
                }
                if (spellcheck) {
                    String prose = allowTokens ? TOKEN.matcher(line).replaceAll(' ') : line
                    prose = prose.replaceAll(/`+[^`]*`+/, ' ')
                        .replaceAll(/!?(\[[^\]]*\])\([^)]*\)/, '$1')
                        .replaceAll(/https?:\/\/\S+/, ' ').replaceAll(/[*_~>#|]/, ' ')
                    (prose =~ /[A-Za-z]+(?:['’-][A-Za-z]+)*/).each { word ->
                        String folded = word.toLowerCase(Locale.ROOT)
                        if (MISSPELLINGS.containsKey(folded)) {
                            issues << "${rel}:${number}: possible misspelling '${word}'; use '${MISSPELLINGS[folded]}'".toString()
                        }
                        if (folded == previousWord && previousLine == number && !(folded in ['had', 'that'])) {
                            issues << "${rel}:${number}: repeated word '${word}'".toString()
                        }
                        previousWord = folded
                        previousLine = number
                    }
                } else {
                    previousWord = ''
                }
            }
            if (inFence) issues << "${rel}: unclosed fenced code block".toString()
            if (h1Count > 1) issues << "${rel}: contains ${h1Count} H1 headings; expected at most one".toString()
            if (path.name == 'Home.md' && h1Count != 1) issues << "${rel}: Home.md must contain exactly one H1 heading".toString()
        }
        return issues
    }

    static String splitDestination(String destination) {
        String value = destination.trim()
        if (value.startsWith('<') && value.contains('>')) return value.substring(1, value.indexOf('>'))
        return value.split(/\s+["']/, 2)[0]
    }

    private static boolean remoteDestination(String destination) {
        return (destination =~ /^[A-Za-z][A-Za-z0-9+.-]*:/).find() || destination.startsWith('//')
    }

    private static String decodedPath(String destination) {
        return URLDecoder.decode(destination.split(/[?#]/, 2)[0].replace('+', '%2B'), StandardCharsets.UTF_8)
    }

    static File localMediaPath(File page, File root, String destination) {
        if (remoteDestination(destination)) return null
        String pathText = decodedPath(destination)
        if (!pathText || pathText.startsWith('/')) return null
        File candidate = new File(page.parentFile, pathText).canonicalFile
        if (!candidate.toPath().startsWith(root.canonicalFile.toPath())) {
            throw new IllegalStateException("local media escapes the wiki root: ${destination}")
        }
        return candidate
    }

    static List<String> checkMedia(File root, boolean verifyLocal = true) {
        List<String> issues = []
        markdownFiles(root).each { path ->
            String rel = displayPath(path, root)
            String text
            try {
                text = readUtf8(path)
            } catch (IllegalStateException error) {
                issues << error.message
                return
            }
            def images = MARKDOWN_IMAGE.matcher(text)
            while (images.find()) {
                int line = lineNumber(text, images.start())
                String alt = images.group(1).replaceAll(/\s+/, ' ').trim()
                String destination = splitDestination(images.group(2))
                if (!alt || alt.toLowerCase(Locale.ROOT) in ['image', 'photo', 'picture', 'screenshot']) {
                    issues << "${rel}:${line}: image needs specific, non-empty alt text".toString()
                }
                if (destination.startsWith('data:')) {
                    issues << "${rel}:${line}: embedded data-URI images are not allowed".toString()
                    continue
                }
                File local
                try {
                    local = localMediaPath(path, root, destination)
                } catch (IllegalStateException | IllegalArgumentException error) {
                    issues << "${rel}:${line}: ${error.message}".toString()
                    continue
                }
                if (local != null && verifyLocal) {
                    if (!local.isFile()) issues << "${rel}:${line}: missing local image '${destination}'".toString()
                    else if (!(suffix(local).toLowerCase(Locale.ROOT) in IMAGE_SUFFIXES)) {
                        issues << "${rel}:${line}: unsupported image type '${suffix(local)}'".toString()
                    }
                }
            }
            def htmlImages = HTML_IMAGE.matcher(text)
            while (htmlImages.find()) {
                def alt = htmlImages.group(1) =~ /(?i)\balt\s*=\s*(["'])(.*?)\1/
                if (!alt.find() || !alt.group(2).trim()) {
                    issues << "${rel}:${lineNumber(text, htmlImages.start())}: HTML image needs non-empty alt text".toString()
                }
            }
        }
        treeFiles(root).each { path ->
            if (Files.isSymbolicLink(path.toPath())) issues << "${displayPath(path, root)}: symlinks are not allowed".toString()
            else if (path.isFile() && suffix(path).toLowerCase(Locale.ROOT) in IMAGE_SUFFIXES && path.length() > 10L * 1024 * 1024) {
                issues << "${displayPath(path, root)}: image exceeds the 10 MiB limit".toString()
            }
        }
        return issues
    }

    static File safeManifestPath(File root, Object value) {
        if (!(value instanceof String) || !value) {
            throw new IllegalStateException('manifest file names must be non-empty strings')
        }
        if (value.startsWith('/') || value.split('/', -1).contains('..') || value.contains('\\')) {
            throw new IllegalStateException("unsafe manifest path: '${value}'")
        }
        File result = new File(root, value).absoluteFile.toPath().normalize().toFile()
        if (!result.canonicalFile.toPath().startsWith(root.canonicalFile.toPath())) {
            throw new IllegalStateException("manifest path escapes output: '${value}'")
        }
        return result
    }

    static String sha256(File file) {
        return MessageDigest.getInstance('SHA-256').digest(Files.readAllBytes(file.toPath())).encodeHex().toString()
    }

    static List<String> checkManifest(File root, int minimumPages) {
        List<String> issues = []
        Object manifest
        try {
            manifest = new JsonSlurper().parseText(readUtf8(new File(root, 'wiki-manifest.json')))
        } catch (IOException | IllegalStateException | IllegalArgumentException | JsonException error) {
            return ["wiki-manifest.json: cannot read manifest (${error.message})".toString()]
        }
        if (!(manifest instanceof Map)) return ['wiki-manifest.json: manifest must be an object']
        if (manifest.format != 1) issues << 'wiki-manifest.json: unsupported or missing format; expected 1'
        Object pages = manifest.pages
        Object assets = manifest.containsKey('assets') ? manifest.assets : []
        if (!(pages instanceof List) || !(assets instanceof List)) return issues + ['wiki-manifest.json: pages and assets must be arrays']
        if (pages.size() < minimumPages) issues << "wiki-manifest.json: generated ${pages.size()} pages; minimum is ${minimumPages}".toString()
        Map<String, String> declared = [:]
        Set<String> pageNames = [] as Set
        [page: pages, asset: assets].each { kind, entries ->
            entries.eachWithIndex { entry, index ->
                if (!(entry instanceof Map)) {
                    issues << "wiki-manifest.json: ${kind} entry ${index} must be an object".toString()
                    return
                }
                File path
                try {
                    path = safeManifestPath(root, entry.file)
                } catch (IllegalStateException error) {
                    issues << "wiki-manifest.json: ${error.message}".toString()
                    return
                }
                String relative = displayPath(path, root)
                if (declared.containsKey(relative)) {
                    issues << "wiki-manifest.json: duplicate file '${relative}'".toString()
                    return
                }
                Object digest = entry.sha256
                if (!(digest instanceof String) || !(digest ==~ /[0-9a-f]{64}/)) {
                    issues << "wiki-manifest.json: invalid SHA-256 for ${relative}".toString()
                    return
                }
                declared[relative] = digest
                if (kind == 'page') {
                    pageNames << relative
                    if (path.parentFile.toPath().toAbsolutePath().normalize() != root.toPath().toAbsolutePath().normalize() || suffix(path) != '.md') {
                        issues << "wiki-manifest.json: page must be a top-level .md file: ${relative}".toString()
                    }
                } else if (!(suffix(path).toLowerCase(Locale.ROOT) in IMAGE_SUFFIXES)) {
                    issues << "wiki-manifest.json: unsupported asset type: ${relative}".toString()
                }
            }
        }
        List<String> folded = declared.keySet().collect { it.toLowerCase(Locale.ROOT) }
        if (folded.size() != folded.toSet().size()) issues << 'wiki-manifest.json: case-insensitive file-name collision'
        (['Home.md', '_Footer.md', '_Sidebar.md'] - pageNames).sort().each {
            issues << "wiki-manifest.json: missing required page ${it}".toString()
        }
        Set<String> actual = treeFiles(root).findAll { it.isFile() && it.name != 'wiki-manifest.json' }
            .collect { displayPath(it, root) } as Set
        (actual - declared.keySet()).sort().each { issues << "wiki-manifest.json: undeclared output file ${it}".toString() }
        (declared.keySet() - actual).sort().each { issues << "wiki-manifest.json: declared file is missing: ${it}".toString() }
        actual.intersect(declared.keySet()).sort().each {
            if (sha256(new File(root, it)) != declared[it]) issues << "wiki-manifest.json: checksum mismatch for ${it}".toString()
        }
        return issues
    }

    static List<String> checkWikiLinks(File root) {
        List<String> issues = []
        Set<String> normalized = (root.listFiles() ?: []).findAll { it.isFile() && it.name.endsWith('.md') }
            .collect { it.name[0..-4].toLowerCase(Locale.ROOT).replace(' ', '-') } as Set
        markdownFiles(root).each { path ->
            String text
            try {
                text = readUtf8(path)
            } catch (IllegalStateException error) {
                issues << error.message
                return
            }
            def wikiLinks = WIKI_LINK.matcher(text)
            while (wikiLinks.find()) {
                String raw = wikiLinks.group(1)
                List<String> parts = raw.split(/\|/, 2) as List
                Set<String> targets = [parts[0], parts[-1]].collect { it.split('#', 2)[0].trim() }.findAll { it } as Set
                if (targets && !targets.any { it.toLowerCase(Locale.ROOT).replace(' ', '-') in normalized }) {
                    issues << "${displayPath(path, root)}:${lineNumber(text, wikiLinks.start())}: broken wiki link [[${raw}]]".toString()
                }
            }
            def links = MARKDOWN_LINK.matcher(text)
            while (links.find()) {
                String destination = splitDestination(links.group(1))
                if (remoteDestination(destination) || destination.startsWith('/') || destination.startsWith('#')) continue
                String target
                try {
                    target = decodedPath(destination).trim()
                } catch (IllegalArgumentException error) {
                    issues << "${displayPath(path, root)}:${lineNumber(text, links.start())}: invalid Markdown link destination ${destination}".toString()
                    continue
                }
                if (!target || target.contains('/') || target.contains('\\')) continue
                if (target.toLowerCase(Locale.ROOT).endsWith('.md')) target = target.substring(0, target.length() - 3)
                else if (suffix(new File(target))) continue
                if (!(target.toLowerCase(Locale.ROOT).replace(' ', '-') in normalized)) {
                    issues << "${displayPath(path, root)}:${lineNumber(text, links.start())}: broken Markdown wiki link [${target}]".toString()
                }
            }
        }
        return issues
    }

    private static void failIssues(List<String> issues) {
        if (issues) throw new IllegalStateException('wiki quality checks failed:\n  - ' + issues.join('\n  - '))
    }

    static Map<String, List<String>> extractUrls(File root) {
        Map<String, List<String>> found = new TreeMap<>()
        markdownFiles(root).each { path ->
            readUtf8(path).readLines().eachWithIndex { line, index ->
                def urls = URL.matcher(line)
                while (urls.find()) {
                    String url = urls.group().replaceFirst(/[.,;:)`]+$/, '').split('#', 2)[0]
                    String location = "${displayPath(path, root)}:${index + 1}".toString()
                    if (!found.containsKey(url)) found[url] = []
                    if (!(location in found[url])) found[url] << location
                }
            }
        }
        return found
    }

    static Map checkUrl(String url) {
        Integer status = null
        String detail = ''
        for (String method : ['HEAD', 'GET']) {
            HttpURLConnection connection
            try {
                connection = (HttpURLConnection) URI.create(url).toURL().openConnection()
                connection.requestMethod = method
                connection.connectTimeout = 20_000
                connection.readTimeout = 20_000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty('Accept', 'text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.1')
                connection.setRequestProperty('User-Agent', 'AC-Legacy-Wiki-Link-Check/1.0 (+https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod)')
                if (method == 'GET') connection.setRequestProperty('Range', 'bytes=0-0')
                status = connection.responseCode
                if (status >= 200 && status < 400) {
                    if (method == 'GET') connection.inputStream.withCloseable { it.read() }
                    return [url: url, state: 'ok', status: status, detail: '']
                }
                detail = connection.responseMessage ?: "HTTP ${status}"
            } catch (IOException | IllegalArgumentException error) {
                detail = error.message ?: error.class.simpleName
                if (method == 'GET') return [url: url, state: 'unverified', status: null, detail: detail]
            } finally {
                connection?.disconnect()
            }
        }
        String state = status in [404, 410] ? 'broken' : status in [401, 403, 429] ? 'restricted' : 'unverified'
        return [url: url, state: state, status: status, detail: detail]
    }

    static Map writeLinkReport(File root, File report, int workers = 8) {
        if (workers < 1 || workers > 32) throw new IllegalStateException('workers must be between 1 and 32')
        Map<String, List<String>> locations = extractUrls(root)
        def executor = Executors.newFixedThreadPool(workers)
        List<Map> results
        try {
            results = executor.invokeAll(locations.keySet().collect { url -> { checkUrl(url) } as Callable<Map> })
                .collect { it.get() }
        } finally {
            executor.shutdownNow()
        }
        results.each { it.locations = locations[it.url] }
        Map<String, Integer> counts = ['ok', 'broken', 'restricted', 'unverified'].collectEntries { state ->
            [(state): results.count { it.state == state }]
        }
        Map payload = [format: 1, summary: counts, links: results]
        report.absoluteFile.parentFile.mkdirs()
        report.setText(JsonOutput.prettyPrint(JsonOutput.toJson(payload)) + '\n', 'UTF-8')
        int dot = report.name.lastIndexOf('.')
        File markdownReport = new File(report.absoluteFile.parentFile, (dot < 0 ? report.name : report.name.substring(0, dot)) + '.md')
        List<String> lines = [
            '# External wiki link report', '',
            "Checked **${results.size()}** unique external links: ${counts.ok} reachable, ${counts.broken} broken, ${counts.restricted} access-restricted, and ${counts.unverified} inconclusive.".toString(),
            '', 'Access-restricted and inconclusive results are reported for review but are not assumed broken.'
        ]
        List<Map> noteworthy = results.findAll { it.state != 'ok' }
        if (noteworthy) {
            lines.addAll(['', '| State | HTTP | URL | First reference |', '|---|---:|---|---|'])
            noteworthy.take(100).each {
                String url = it.url.replace('|', '%7C')
                String location = it.locations[0].replace('|', '\\|')
                lines << "| ${it.state} | ${it.status ?: '—'} | <${url}> | `${location}` |".toString()
            }
            if (noteworthy.size() > 100) lines.addAll(['', "The JSON artifact contains ${noteworthy.size() - 100} additional results.".toString()])
        }
        markdownReport.setText(lines.join('\n') + '\n', 'UTF-8')
        println lines[2]
        return payload
    }
}
