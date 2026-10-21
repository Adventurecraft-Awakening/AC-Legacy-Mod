package dev.adventurecraft.wiki

import java.util.regex.Matcher
import java.util.regex.Pattern

/** A brace-aware scanner for the public surface of the scripting Java package. */
final class WikiScriptApi {
    static final String DEFAULT_SOURCE_BASE_URL = 'https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/'
    private static final Pattern TYPE_PATTERN = Pattern.compile(
        /\bpublic\s+(?:(?:abstract|final|sealed|non-sealed|static|strictfp)\s+)*(class|interface|record|enum)\s+([A-Za-z_$][\w$]*)\b/)
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile(
        /@[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*(?:\s*\([^)]*\))?\s*/, Pattern.DOTALL)

    static final class ScriptApiException extends RuntimeException {
        ScriptApiException(String message) { super(message) }
    }

    static final class ApiMember {
        final String category
        final String name
        final String signature
        final int line
        final boolean deprecated

        ApiMember(String category, String name, String signature, int line, boolean deprecated = false) {
            this.category = category
            this.name = name
            this.signature = signature
            this.line = line
            this.deprecated = deprecated
        }
    }

    static final class ApiType {
        final String name
        final String kind
        final String signature
        final int line
        final String sourcePath
        final List<String> extendedTypes
        final List<String> implementedTypes
        final List<ApiMember> members

        ApiType(String name, String kind, String signature, int line, String sourcePath,
                List<String> extendedTypes, List<String> implementedTypes, List<ApiMember> members) {
            this.name = name
            this.kind = kind
            this.signature = signature
            this.line = line
            this.sourcePath = sourcePath
            this.extendedTypes = Collections.unmodifiableList(new ArrayList<>(extendedTypes))
            this.implementedTypes = Collections.unmodifiableList(new ArrayList<>(implementedTypes))
            this.members = Collections.unmodifiableList(new ArrayList<>(members))
        }

        List<String> getExtends() { extendedTypes }
        List<String> getImplements() { implementedTypes }
        String getSource_path() { sourcePath }
    }

    /** Mask comments and literals without changing offsets or line numbers. */
    static String maskJava(String source) {
        char[] chars = source.toCharArray()
        int index = 0
        while (index < source.length()) {
            int end = -1
            if (source.startsWith('//', index)) {
                end = source.indexOf('\n', index + 2)
                if (end < 0) end = source.length()
            } else if (source.startsWith('/*', index)) {
                end = source.indexOf('*/', index + 2)
                end = end < 0 ? source.length() : end + 2
            } else if (source.startsWith('"""', index)) {
                end = source.indexOf('"""', index + 3)
                end = end < 0 ? source.length() : end + 3
            } else if (source.charAt(index) == '"' as char || source.charAt(index) == '\'' as char) {
                char quote = source.charAt(index)
                end = index + 1
                while (end < source.length()) {
                    if (source.charAt(end) == '\\' as char) {
                        end += 2
                        continue
                    }
                    if (source.charAt(end++) == quote) break
                }
            }
            if (end >= 0) {
                for (int pos = index; pos < Math.min(end, chars.length); pos++) {
                    if (chars[pos] != '\r' as char && chars[pos] != '\n' as char) chars[pos] = ' ' as char
                }
                index = end
            } else {
                index++
            }
        }
        new String(chars)
    }

    static String normalizeSignature(String value) {
        value.replaceAll(/\s+/, ' ').trim().replaceAll(/;+$/, '').trim()
    }

    static int findMatchingBrace(String masked, int opening) {
        int depth = 0
        for (int index = opening; index < masked.length(); index++) {
            char character = masked.charAt(index)
            if (character == '{' as char) depth++
            else if (character == '}' as char && --depth == 0) return index
        }
        throw new ScriptApiException("unclosed Java block at offset ${opening}")
    }

    static List<String> splitTypeList(String value) {
        List<String> result = []
        int start = 0
        int depth = 0
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index)
            if (character == '<' as char) depth++
            else if (character == '>' as char) depth = Math.max(0, depth - 1)
            else if (character == ',' as char && depth == 0) {
                String item = normalizeSignature(value.substring(start, index))
                if (item) result.add(item)
                start = index + 1
            }
        }
        String item = normalizeSignature(value.substring(start))
        if (item) result.add(item)
        result
    }

    static List<List<String>> parseInheritance(String header, String kind) {
        List<String> extendedTypes = []
        List<String> implementedTypes = []
        def extended = header =~ /\bextends\s+(.+?)(?=\bimplements\b|$)/
        if (extended.find()) extendedTypes = splitTypeList(extended.group(1))
        def implemented = header =~ /\bimplements\s+(.+)$/
        if (implemented.find()) implementedTypes = splitTypeList(implemented.group(1))
        if (kind == 'record') extendedTypes = []
        [extendedTypes, implementedTypes]
    }

    private static Map stripLeadingAnnotations(String rawHeader) {
        int offset = 0
        while (offset < rawHeader.length() && Character.isWhitespace(rawHeader.charAt(offset))) offset++
        boolean deprecated = false
        while (true) {
            def annotation = ANNOTATION_PATTERN.matcher(rawHeader)
            annotation.region(offset, rawHeader.length())
            if (!annotation.lookingAt()) break
            String value = rawHeader.substring(offset, annotation.end())
            deprecated |= (value =~ /^@(?:[\w$]+\.)*Deprecated\b/).find()
            offset = annotation.end()
            while (offset < rawHeader.length() && Character.isWhitespace(rawHeader.charAt(offset))) offset++
        }
        [declaration: rawHeader.substring(offset).trim(), offset: offset, deprecated: deprecated]
    }

    static String parameterText(String declaration) {
        int opening = declaration.indexOf('(')
        if (opening < 0) return ''
        int depth = 0
        for (int index = opening; index < declaration.length(); index++) {
            if (declaration.charAt(index) == '(' as char) depth++
            else if (declaration.charAt(index) == ')' as char && --depth == 0) {
                return declaration.substring(opening + 1, index).trim()
            }
        }
        declaration.substring(opening + 1).trim()
    }

    static ApiMember classifyMember(String rawHeader, String ownerName, String ownerKind,
                                    String source, int absoluteOffset) {
        Map parsed = stripLeadingAnnotations(rawHeader)
        String declaration = normalizeSignature(parsed.declaration as String)
        if (!declaration) return null
        if ((declaration.split(/\(/, 2)[0] =~ /\b(class|interface|record|enum)\b/).find()) return null
        boolean explicitlyPublic = (declaration =~ /^public\b/).find()
        boolean implicitlyPublic = ownerKind == 'interface' && !(declaration =~ /^(?:private|protected)\b/).find()
        if (!(explicitlyPublic || implicitlyPublic)) return null
        if (implicitlyPublic && !explicitlyPublic) declaration = "public ${declaration}"

        String declarator = declaration.split('=', 2)[0].trim()
        int opening = declarator.indexOf('(')
        String name
        String category
        String signature
        if (opening >= 0) {
            def memberName = declarator.substring(0, opening) =~ /([A-Za-z_$][\w$]*)\s*$/
            if (!memberName.find()) return null
            name = memberName.group(1)
            if (name == ownerName) category = 'constructors'
            else if (name.startsWith('set')) category = 'setters'
            else if ((name.startsWith('get') || name.startsWith('is')) && !parameterText(declaration)) category = 'getters'
            else category = 'methods'
            signature = declaration
        } else {
            def memberName = declarator =~ /([A-Za-z_$][\w$]*)\s*(?:\[\s*\])?\s*$/
            if (!memberName.find()) return null
            name = memberName.group(1)
            category = 'fields'
            signature = declarator + ';'
        }
        new ApiMember(category, name, signature, lineNumber(source, absoluteOffset + (parsed.offset as int)), parsed.deprecated as boolean)
    }

    static List<ApiMember> recordMembers(String typeName, String header, int line) {
        int opening = header.indexOf('(', header.indexOf(typeName) + typeName.length())
        int closing = header.lastIndexOf(')')
        if (opening < 0 || closing < opening) return []
        List<String> components = splitTypeList(header.substring(opening + 1, closing))
        if (!components) return []
        List<ApiMember> members = [new ApiMember('constructors', typeName, "public ${typeName}(${components.join(', ')})", line)]
        for (String component : components) {
            def match = component =~ /^(.+?)\s+([A-Za-z_$][\w$]*)$/
            if (match.find()) members.add(new ApiMember('getters', match.group(2), "public ${match.group(1)} ${match.group(2)}()", line))
        }
        members
    }

    static List<ApiMember> scanMembers(String source, String masked, int bodyOpen, int bodyClose,
                                       String ownerName, String ownerKind) {
        List<ApiMember> members = []
        int index = bodyOpen + 1
        while (index < bodyClose) {
            while (index < bodyClose && (Character.isWhitespace(masked.charAt(index)) || masked.charAt(index) == ';' as char)) index++
            if (index >= bodyClose) break
            int start = index
            int scan = index
            boolean equalsSeen = false
            int parenDepth = 0
            boolean completed = false
            while (scan < bodyClose) {
                char character = masked.charAt(scan)
                if (character == '(' as char) parenDepth++
                else if (character == ')' as char) parenDepth = Math.max(0, parenDepth - 1)
                else if (character == '=' as char && parenDepth == 0) equalsSeen = true
                else if (character == ';' as char && parenDepth == 0) {
                    ApiMember member = classifyMember(masked.substring(start, scan), ownerName, ownerKind, source, start)
                    if (member != null) members.add(member)
                    index = scan + 1
                    completed = true
                    break
                } else if (character == '{' as char && parenDepth == 0) {
                    int closing = findMatchingBrace(masked, scan)
                    if (equalsSeen) {
                        scan = closing + 1
                        continue
                    }
                    ApiMember member = classifyMember(masked.substring(start, scan), ownerName, ownerKind, source, start)
                    if (member != null) members.add(member)
                    index = closing + 1
                    completed = true
                    break
                }
                scan++
            }
            if (!completed) break
        }
        members
    }

    private static Matcher publicTopLevelType(String masked) {
        def type = TYPE_PATTERN.matcher(masked)
        int depth = 0
        int scanned = 0
        while (type.find()) {
            for (; scanned < type.start(); scanned++) {
                if (masked.charAt(scanned) == '{' as char) depth++
                else if (masked.charAt(scanned) == '}' as char) depth--
            }
            if (depth == 0) return type
        }
        null
    }

    static ApiType parseJavaSource(String source, String sourcePath) {
        String masked = maskJava(source)
        def type = publicTopLevelType(masked)
        if (type == null) throw new ScriptApiException("no public top-level Java type found in ${sourcePath}")
        int bodyOpen = masked.indexOf('{', type.end())
        if (bodyOpen < 0) throw new ScriptApiException("public type has no body in ${sourcePath}")
        int bodyClose = findMatchingBrace(masked, bodyOpen)
        String kind = type.group(1)
        String name = type.group(2)
        String header = normalizeSignature(masked.substring(type.start(), bodyOpen))
        List<List<String>> inheritance = parseInheritance(header, kind)
        int line = lineNumber(source, type.start())
        List<ApiMember> members = scanMembers(source, masked, bodyOpen, bodyClose, name, kind)
        if (kind == 'record') members = recordMembers(name, header, line) + members
        new ApiType(name, kind, header, line, sourcePath.replace('\\', '/'), inheritance[0], inheritance[1], members)
    }

    private static int lineNumber(String source, int offset) {
        int line = 1
        for (int index = 0; index < offset; index++) if (source.charAt(index) == '\n' as char) line++
        line
    }

    static List<ApiType> extractApi(File sourceDir) {
        List<File> paths = (sourceDir.listFiles()?.findAll { it.isFile() && it.name.endsWith('.java') } ?: [])
            .sort { it.name.toLowerCase(Locale.ROOT) }
        if (!paths) throw new ScriptApiException("no Java sources found in ${sourceDir}")
        File root = new File(System.getProperty('user.dir')).canonicalFile
        while (root != null && !new File(root, '.git').exists()) root = root.parentFile
        File relativeBase = root != null && sourceDir.canonicalFile.toPath().startsWith(root.toPath()) ? root : sourceDir.canonicalFile
        List<ApiType> types = paths.collect { path ->
            String relativePath = relativeBase.toPath().relativize(path.canonicalFile.toPath()).toString().replace('\\', '/')
            String source = path.getText('UTF-8')
            if (publicTopLevelType(maskJava(source)) == null) return null
            parseJavaSource(source, relativePath)
        }.findAll { it != null }
        if (types.collect { it.name }.toSet().size() != types.size()) throw new ScriptApiException('duplicate public scripting type names')
        types.sort { it.name.toLowerCase(Locale.ROOT) }
    }

    static String wikiPageName(String typeName) { "Scripting-API-${typeName}" }

    private static String markdownTableLink(String target, String label) {
        "[${label.replace('|', '\\|')}](${target})"
    }

    private static String sourceUrl(ApiType type, String baseUrl, Integer line = null) {
        String url = baseUrl.replaceAll('/+$', '') + '/' + type.sourcePath
        line ? "${url}#L${line}" : url
    }

    private static String typeReference(String value, Set<String> localNames, boolean inTable = false) {
        def simpleName = value =~ /^(?:[\w$.]+\.)?([A-Za-z_$][\w$]*)/
        if (simpleName.find() && localNames.contains(simpleName.group(1))) {
            String name = simpleName.group(1)
            return inTable ? markdownTableLink(wikiPageName(name), "`${value}`") : "[[${wikiPageName(name)}|`${value}`]]"
        }
        "`${value}`"
    }

    static String renderTypePage(ApiType type, List<ApiType> allTypes, String sourceBaseUrl = DEFAULT_SOURCE_BASE_URL) {
        Set<String> localNames = allTypes.collect { it.name }.toSet()
        int typeIndex = allTypes.indexOf(type)
        List<String> lines = [
            "# Script API: ${type.name}", '',
            '> Generated from the current Java source. Do not edit this page by hand.', '',
            '[[Scripting-API|← Script API index]]', '',
            "**Declaration:** `${type.signature}`",
            "**Source:** [${type.sourcePath}](${sourceUrl(type, sourceBaseUrl, type.line)})",
        ]
        if (type.extendedTypes) lines.add('**Extends:** ' + type.extendedTypes.collect { typeReference(it, localNames) }.join(', '))
        if (type.implementedTypes) lines.add('**Implements:** ' + type.implementedTypes.collect { typeReference(it, localNames) }.join(', '))
        Map<String, String> headings = [constructors: 'Constructors', fields: 'Fields', getters: 'Getters', setters: 'Setters', methods: 'Other methods']
        headings.each { category, heading ->
            List<ApiMember> members = type.members.findAll { it.category == category }
            if (members) {
                lines.addAll(['', "## ${heading}", '', '| Signature | Status | Source |', '| --- | --- | --- |'])
                members.each { member ->
                    String signature = member.signature.replace('|', '\\|')
                    String status = member.deprecated ? '**Deprecated**' : 'Current'
                    lines.add("| `${signature}` | ${status} | [line ${member.line}](${sourceUrl(type, sourceBaseUrl, member.line)}) |")
                }
            }
        }
        if (!type.members) lines.addAll(['', '_No directly declared public members._'])
        List<String> navigation = []
        if (typeIndex > 0) {
            ApiType previous = allTypes[typeIndex - 1]
            navigation.add("[[${wikiPageName(previous.name)}|← ${previous.name}]]")
        }
        navigation.add('[[Scripting-API|API index]]')
        if (typeIndex + 1 < allTypes.size()) {
            ApiType following = allTypes[typeIndex + 1]
            navigation.add("[[${wikiPageName(following.name)}|${following.name} →]]")
        }
        lines.addAll(['', '---', '', navigation.join(' · '), ''])
        lines.join('\n')
    }

    static String renderIndex(List<ApiType> types, String sourceBaseUrl = DEFAULT_SOURCE_BASE_URL) {
        List<String> lines = [
            '# Scripting API', '',
            '> Generated from the current `script` Java package. Signatures marked deprecated remain in source but should not be used in new scripts.', '',
            "This reference contains **${types.size()} types**. Members are directly declared by each type; follow the inheritance links for inherited API.", '',
            '| Type | Kind | Inheritance | Declared members |', '| --- | --- | --- | ---: |',
        ]
        Set<String> localNames = types.collect { it.name }.toSet()
        types.each { type ->
            String inheritance = (type.extendedTypes + type.implementedTypes).collect { typeReference(it, localNames, true) }.join(', ') ?: '—'
            lines.add("| ${markdownTableLink(wikiPageName(type.name), type.name)} | ${type.kind} | ${inheritance} | ${type.members.size()} |")
        }
        lines.add('')
        lines.join('\n')
    }

    static Map<String, String> renderReference(List<ApiType> types, String sourceBaseUrl = DEFAULT_SOURCE_BASE_URL) {
        List<ApiType> ordered = types.sort(false) { it.name.toLowerCase(Locale.ROOT) }
        Map<String, String> pages = new TreeMap<>()
        pages['Scripting-API.md'] = renderIndex(ordered, sourceBaseUrl)
        ordered.each { pages[wikiPageName(it.name) + '.md'] = renderTypePage(it, ordered, sourceBaseUrl) }
        pages
    }

    /** Write only owned API pages and remove obsolete generated API pages. */
    static List<File> writeReference(File outputDir, File sourceDir, String sourceBaseUrl = DEFAULT_SOURCE_BASE_URL) {
        Map<String, String> pages = renderReference(extractApi(sourceDir), sourceBaseUrl)
        outputDir.mkdirs()
        outputDir.listFiles()?.findAll { it.name ==~ /Scripting-API.*\.md/ && !pages.containsKey(it.name) }?.each { it.delete() }
        pages.collect { name, content ->
            File output = new File(outputDir, name)
            output.setText(content, 'UTF-8')
            output
        }
    }
}
