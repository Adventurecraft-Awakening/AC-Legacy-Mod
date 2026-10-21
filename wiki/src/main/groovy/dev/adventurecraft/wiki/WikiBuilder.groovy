package dev.adventurecraft.wiki

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/** Builds the repository-managed GitHub wiki from reviewed source and Java registries. */
class WikiBuilder {
    static final String BUILDER_SOURCE = 'wiki/src/main/groovy/dev/adventurecraft/wiki/WikiBuilder.groovy'
    static final String SCRIPT_SOURCE = 'wiki/src/main/groovy/dev/adventurecraft/wiki/WikiScriptApi.groovy'
    static final String FANDOM_API = 'https://adventurecraftmod.fandom.com/api.php'
    static final String FANDOM_WIKI = 'https://adventurecraftmod.fandom.com/wiki/'
    static final String USER_AGENT = 'AC-Legacy-Wiki-Audit/1.0 (+https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod)'
    static final Set<String> MEDIA_SUFFIXES = ['.avif', '.gif', '.jpeg', '.jpg', '.png', '.svg', '.webp'] as Set
    static final Map REPLACEMENTS = [
        'AdventureCraft Wiki': 'Home', 'Main Page': 'Home', 'Installing AdventureCraft': 'Getting-Started',
        'Gamemodes': 'Controls-and-Game-Modes', 'Commands': 'Current-Commands', 'Version History': 'Release-History',
        'Script Guide': 'Scripting', 'Available Scripts': 'Scripting', 'Mobs': 'Current-Entities', 'NPC': 'Current-Entities'
    ]
    static final Map DISPOSITION_LABELS = [
        'rewritten-current': 'Rewritten from current source',
        'inventory-verified': 'Current existence verified; legacy behavior not imported',
        'historical-script': 'Historical community script; not runtime-verified',
        'historical-reference': 'Historical reference; not imported as current documentation'
    ]
    static final Set LEGACY_MAP_CATEGORIES = ['adventure', 'horror', 'puzzle', 'survival', 'canceled maps',
        'map download site unavailable', 'missing map link', 'unreleased map', 'timed', 'fighting'] as Set
    static final Set LEGACY_MAP_CATALOG_PAGES = ['AC Maps', 'AdventureMap', 'Download Maps', 'Download Maps (Outdated)',
        'Download maps bonus', 'Downloading and Adding Maps', 'Maps', 'Sorted Map List'] as Set
    static final Map<String, List<String>> COMMAND_DOCS = [
        config: ['/config', "Opens the current world's configuration screen.", 'World required.'],
        test: ['/test', 'Opens the map-editing HUD.', 'This is an internal/test-facing command in the current registry.'],
        scriptstats: ['/scriptstats', 'Opens the per-script timing statistics screen.', 'World required.'],
        textureatlas: ['/textureatlas', 'Opens the texture-atlas inspection screen.', 'Client-side inspection command.'],
        day: ['/day', "Sets the current world's time of day to 0.", 'The value is source-derived, not inherited from historical documentation.'],
        night: ['/night', "Sets the current world's time of day to 14000.", 'Historical Fandom documentation stated 12000; that is not the current implementation.'],
        removemobs: ['/removemobs', 'Removes every loaded Mob except the player and reports the count.', 'Already-removed entities are counted separately from newly removed entities.'],
        cameraclear: ['/cameraclear', 'Clears every point from the camera block currently being edited.', 'Fails with an in-game message when no cutscene camera is active.'],
        fullbright: ['/fullbright', 'Sets all 16 world brightness-ramp entries to full brightness and rebuilds renderers.', 'This command sets brightness; it is not implemented as a reversible boolean toggle.'],
        scriptstatreset: ['/scriptstatreset', 'Clears collected timing statistics for all loaded map scripts.', 'World and script handler required.'],
        health: ['/health [amount]', 'Sets both health and maximum health; the omitted amount defaults to 12.', 'The amount must be an integer of at least 1. Player heart-piece progress is reset.'],
        undo: ['/undo [amount]', 'Undoes up to the requested number of recorded edit actions.', 'The omitted amount defaults to 1.'],
        redo: ['/redo [amount]', 'Redoes up to the requested number of undone edit actions.', 'The omitted amount defaults to 1.'],
        mapedit: ['/mapedit [true|false]', 'Sets or toggles level-editing mode.', 'With no value, the current state is inverted.'],
        renderpaths: ['/renderpaths [true|false]', 'Sets or toggles entity-path debug rendering.', 'With no value, the current state is inverted.'],
        renderfov: ['/renderfov [true|false]', 'Sets or toggles field-of-view debug rendering.', 'With no value, the current state is inverted.'],
        rendercollisions: ['/rendercollisions [true|false]', 'Sets or toggles collision debug rendering.', 'With no value, the current state is inverted.'],
        renderrays: ['/renderrays [true|false]', 'Sets or toggles raycast debug rendering.', 'With no value, the current state is inverted.'],
        fluidcollision: ['/fluidcollision [true|false]', 'Sets or toggles whether debug targeting can hit fluids.', 'With no value, the current state is inverted.'],
        fly: ['/fly [true|false] or /fly <speed>', 'Sets/toggles flying or sets flight speed.', 'Flight speed accepts a floating-point value from 0 through 1000.'],
        noclip: ['/noclip [true|false]', 'Sets or toggles collision-free movement.', 'Enabling noclip also enables flying.'],
        cameraadd: ['/cameraadd <time>', "Adds a quadratic-blend camera point at the executing entity's view position and rotation.", 'A camera block must currently be active for editing.'],
        help: ['/help [page|command]', 'Lists command help pages or describes a command path.', 'Page numbers begin at 1; a command path may include subcommands.'],
        gamerule: ['/gamerule <rule> [value]', 'Reads or sets a registered per-world AdventureCraft game rule.', 'Omitting the value reports the current rule value.'],
        undostack: ['/undostack [clear]', 'Reports undo/redo stack sizes or clears both stacks.', 'The `clear` subcommand is destructive to the current edit history.']
    ]

    final File root
    final File wikiDir
    final File sourceDir
    final File baselineFile

    WikiBuilder(File root) {
        this.root = root.canonicalFile
        wikiDir = new File(this.root, 'wiki')
        sourceDir = new File(wikiDir, 'pages')
        baselineFile = new File(wikiDir, 'fandom-baseline.json')
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message)
    }

    String display(File file) {
        def path = file.toPath().toAbsolutePath().normalize()
        path.startsWith(root.toPath()) ? root.toPath().relativize(path).toString().replace('\\', '/') : path.toString()
    }

    String read(File file) {
        require(file.isFile(), "required file is missing: ${display(file)}")
        file.getText('UTF-8')
    }

    String read(String path) { read(new File(root, path)) }

    Map json(File file) {
        try {
            def data = new JsonSlurper().parseText(read(file))
            require(data instanceof Map, "${display(file)} must contain a JSON object")
            (Map) data
        } catch (groovy.json.JsonException error) {
            throw new IllegalStateException("invalid JSON in ${display(file)}: ${error.message}", error)
        }
    }

    static List<File> files(File directory) {
        if (!directory.exists()) return []
        def result = []
        directory.eachFileRecurse { if (it.isFile()) result << it }
        result.sort { a, b -> a.path <=> b.path }
    }

    List<File> sourcePages() { (sourceDir.listFiles() ?: []).findAll { it.isFile() && it.name.endsWith('.md') }.sort { it.name } }
    Set<String> sourcePageNames() { sourcePages().collect { it.name[0..-4] } as Set }

    Map<String, String> readProperties() {
        def values = [:]
        read('gradle.properties').readLines().each { raw ->
            def line = raw.trim()
            if (line && !line.startsWith('#') && line.contains('=')) {
                def pair = line.split('=', 2)
                values[pair[0].trim()] = pair[1].trim()
            }
        }
        values
    }

    String gitRevision() {
        def revision = System.getenv('GITHUB_SHA')
        if (revision) return revision
        try {
            def process = new ProcessBuilder('git', 'rev-parse', 'HEAD').directory(root).redirectError(ProcessBuilder.Redirect.DISCARD).start()
            def value = process.inputStream.getText('UTF-8').trim()
            process.waitFor() == 0 ? value : 'unknown'
        } catch (IOException ignored) { 'unknown' }
    }

    Map loadPageMetadata() {
        def data = json(new File(wikiDir, 'page-metadata.json'))
        require(data.format == 1 && data.pages instanceof Map, 'wiki/page-metadata.json must use format 1 and contain a pages object')
        def missing = sourcePageNames() - data.pages.keySet()
        def stale = data.pages.keySet() - sourcePageNames()
        require(!missing && !stale, "authored wiki metadata mismatch (missing metadata: ${missing.sort().join(', ')}; stale metadata: ${stale.sort().join(', ')})")
        data.pages.each { page, metadata ->
            require(metadata instanceof Map && metadata.status && metadata.evidence instanceof List && metadata.evidence,
                "${page} must have a status and non-empty evidence list")
            metadata.evidence.each { require(new File(root, it.toString()).isFile(), "${page} evidence does not exist: ${it}") }
        }
        data.pages
    }

    Map readLang() {
        def values = [:]
        read('src/main/resources/assets/adventurecraft/lang/en_US.lang').readLines().each { line ->
            if (line && !line.trim().startsWith('#') && line.contains('=')) {
                def pair = line.split('=', 2)
                values[pair[0]] = pair.length > 1 ? pair[1] : ''
            }
        }
        values
    }

    static String sourceUrl(String path) { "https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod/blob/main/${path}" }
    static String markdownEscape(Object value) { value.toString().replace('|', '\\|').replace('\n', ' ') }
    static String markdownImageAlt(Object value) { value.toString().replace('\\', '\\\\').replace(']', '\\]').replace('|', '\\|').replace('\n', ' ').trim() }
    static String pageLink(String target, Object label, boolean code = false) {
        def escaped = markdownEscape(label)
        "[${code ? '`' + escaped + '`' : escaped}](${target})"
    }
    static String pageSlug(String value) {
        def slug = value.replaceAll(/[^A-Za-z0-9]+/, '-').replaceAll(/^-+|-+$/, '')
        require(slug as boolean, "cannot create a GitHub wiki slug from '${value}'")
        slug
    }
    static String featurePageName(String kind, String name) { "${kind}-${pageSlug(name)}" }

    List<Map> parseBlocks(Map lang) { parseRegistry(lang, true) }
    List<Map> parseItems(Map lang) { parseRegistry(lang, false) }

    private List<Map> parseRegistry(Map lang, boolean blocks) {
        def path = blocks ? 'src/main/java/dev/adventurecraft/awakening/tile/AC_Blocks.java' : 'src/main/java/dev/adventurecraft/awakening/item/AC_Items.java'
        def entries = []
        read(path).readLines().eachWithIndex { line, index ->
            if ((blocks ? line.contains('public static final') && line.contains('new AC_Block') : line.contains('public static') && line.contains('new '))) {
                def field = line =~ /public static .*?\s+(\w+)\s*=/
                def ctor = line =~ (blocks ? /new (AC_Block\w+)\((\d+)/ : /new (\w+)\((\d+)/)
                def desc = line =~ /\.setDescriptionId\("([^"]+)"\)/
                require(field.find() && ctor.find() && desc.find(), "cannot parse ${blocks ? 'block' : 'item'} registration at ${path}:${index + 1}")
                int constructorId = ctor.group(2).toInteger()
                def descriptionId = desc.group(1)
                def entry = [field: field.group(1), 'class': ctor.group(1), id: constructorId + (blocks ? 0 : 256),
                    description_id: descriptionId, name: lang["${blocks ? 'tile' : 'item'}.${descriptionId}.name".toString()] ?: descriptionId, line: index + 1]
                if (!blocks) entry.constructor_id = constructorId
                entries << entry
            }
        }
        require(entries.size() >= (blocks ? 60 : 30), "parsed only ${entries.size()} AdventureCraft ${blocks ? 'blocks' : 'items'}; the source format may have changed")
        entries
    }

    List<Map> parseEntities() {
        def entries = []
        def path = 'src/main/java/dev/adventurecraft/awakening/mixin/entity/MixinEntityRegistry.java'
        read(path).readLines().eachWithIndex { line, index ->
            def match = line =~ /setId\((AC_\w+)\.class,\s*"([^"]+)",\s*(\d+)\);/
            if (match.find()) entries << ['class': match.group(1), name: match.group(2), id: match.group(3).toInteger(), line: index + 1]
        }
        require(entries.size() >= 10, "parsed only ${entries.size()} AdventureCraft entities; the source format may have changed")
        entries
    }

    Map parseCommands() {
        def source = read('src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java')
        def commands = registeredCommands(source)
        require(commands.size() >= 20, "parsed only ${commands.size()} top-level commands; the source format may have changed")
        def rules = (read('src/main/java/dev/adventurecraft/awakening/world/GameRules.java') =~ /register\("ac:([a-z_]+)",\s*([^,;)]+)(?:,\s*([^,]+),\s*([^)]+))?\)/).collect { match ->
            [name: match[1], 'default': match[2].trim(), range: match[3] ? "${match[3].trim()}–${match[4].trim()}" : 'boolean']
        }
        require(rules.size() >= 8, "parsed only ${rules.size()} game rules; the source format may have changed")
        [commands: commands, rules: rules]
    }

    /** Inspect registered roots, rather than treating every nested Brigadier literal as a command. */
    static List<String> registeredCommands(String source) {
        def commands = []
        def registrations = source =~ /\bdispatcher\s*\.\s*register\s*\(/
        while (registrations.find()) {
            String expression = source.substring(registrations.end())
            String name = rootCommandLiteral(expression)
            if (!name) {
                // gamerule builds a root variable, appends dynamic children, then registers that root.
                def variable = expression =~ /^\s*([A-Za-z_$][\w$]*)\s*(?:\)|\.)/
                if (variable.find()) {
                    def assignments = source.substring(0, registrations.start()) =~
                        (/(?m)\b/ + java.util.regex.Pattern.quote(variable.group(1)) + /\s*=\s*/)
                    while (assignments.find()) {
                        String assigned = rootCommandLiteral(source.substring(assignments.end(), registrations.start()))
                        if (assigned) name = assigned
                    }
                }
            }
            require(name as boolean, "cannot parse registered command root near source offset ${registrations.start()}")
            commands << name
        }
        commands.unique()
    }

    private static String rootCommandLiteral(String expression) {
        // These helpers wrap the root as their first argument; .then(...) children follow it.
        def literal = expression =~ /^\s*(?:(?:optionalArg|requiredArg)\s*\(\s*)*literal\s*\(\s*"([a-z]+)"\s*\)/
        literal.find() ? literal.group(1) : null
    }

    Map loadBaseline() {
        def data = json(baselineFile)
        require(data.license?.name == 'CC-BY-SA', 'Fandom baseline must retain its CC-BY-SA attribution metadata')
        require(data.pages instanceof List && data.pages, 'Fandom baseline contains no pages')
        def titles = data.pages.collect { it.title }
        require(titles.every { it instanceof String && it }, 'Fandom baseline contains an invalid page title')
        require(titles.size() == titles.toSet().size(), 'Fandom baseline contains duplicate page titles')
        def excluded = titles.findAll { it.toLowerCase(Locale.ROOT).contains('map') }
        require(!excluded, "Fandom baseline contains excluded legacy map page(s): ${excluded.sort().join(', ')}")
        require(data.source_page_count instanceof Number && data.excluded_legacy_map_page_count instanceof Number &&
            data.source_page_count == data.pages.size() + data.excluded_legacy_map_page_count,
            'Fandom baseline source and legacy-map exclusion counts are inconsistent')
        data
    }

    Map loadFeatureDetails(List blocks, List items, List entities) {
        def data = json(new File(wikiDir, 'feature-details.json'))
        require(data.schema_version == 1, 'wiki/feature-details.json must use schema_version 1')
        def expected = [blocks: blocks.collect { it.field }.toSet(), items: items.collect { it.field }.toSet(), entities: entities.collect { it['class'] }.toSet()]
        def required = ['purpose', 'behavior', 'mode_notes', 'interactions', 'evidence'] as Set
        def result = [:]
        expected.each { kind, keys ->
            def entries = data[kind]
            require(entries instanceof Map && entries.keySet() == keys,
                "${kind} feature detail coverage mismatch; missing=${(keys - (entries instanceof Map ? entries.keySet() : [])).sort()}, extra=${((entries instanceof Map ? entries.keySet() : []) - keys).sort()}")
            entries.each { key, entry ->
                require(entry instanceof Map && entry.keySet().containsAll(required), "${kind}.${key} is missing required feature-detail fields")
                require(entry.interactions instanceof List && entry.evidence instanceof List && entry.evidence,
                    "${kind}.${key} interactions/evidence must be lists and evidence must not be empty")
                entry.evidence.each { require(new File(root, it.toString()).isFile(), "${kind}.${key} evidence does not exist: ${it}") }
            }
            result[kind] = entries
        }
        result
    }

    Map buildFeaturePageTargets(List blocks, List items, List entities) {
        def groups = [Block: blocks, Item: items, Entity: entities]
        Set<List> keys = [] as Set
        groups.each { label, entries -> entries.each { keys << [label, it.name.toString()] } }
        def humanOwners = [:].withDefault { [] as Set }
        def namespacedOwners = [:].withDefault { [] as Set }
        keys.each { key -> humanOwners[pageSlug(key[1])] << key; namespacedOwners[featurePageName(key[0], key[1])] << key }
        def reserved = sourcePageNames() + ['Current-Blocks', 'Current-Items', 'Current-Entities', 'Current-Commands', 'Fandom-Baseline-Index',
            'Scripting-API', 'Verification-Status', 'Aliases', 'Script', '_Sidebar', '_Footer']
        def targets = [:]
        keys.toList().sort { a, b -> a[0] <=> b[0] ?: a[1] <=> b[1] }.each { key ->
            def human = pageSlug(key[1])
            boolean conflict = humanOwners[human].size() > 1 || namespacedOwners[human].any { it != key }
            targets[key] = (human in reserved || conflict) ? featurePageName(key[0], key[1]) : human
        }
        targets
    }

    Map renderFeaturePages(List blocks, List items, List entities, Map details, Map targets, Map renders) {
        def pages = [:]
        def evidence = [:]
        [['block', 'Block', blocks, 'field'], ['item', 'Item', items, 'field'], ['entity', 'Entity', entities, 'class']].each { config ->
            def (kind, label, entries, detailKey) = config
            entries.groupBy { it.name.toString() }.sort { a, b -> a.key.toLowerCase(Locale.ROOT) <=> b.key.toLowerCase(Locale.ROOT) }.each { name, group ->
                def pageName = targets[[label, name]]
                def lines = ["# ${name}", '', "> Current ${kind} reference generated from the AC-Legacy registry and source-backed feature audit."]
                def records = kind != 'entity' && renders != null ? group.collect { renders["${kind}s".toString()][it.field] } : []
                if (records.size() == 1) {
                    def record = records[0]
                    lines.addAll(['', "![${markdownImageAlt(record.alt)}](assets/registry/${record.file})", '', "*Native ${kind} render for `${group[0].field}`.*"])
                } else if (records) {
                    lines.addAll(['', '## Registry render gallery', '', '| Registry field | Native render |', '| --- | --- |'])
                    group.eachWithIndex { entry, i -> lines << "| `${entry.field}` | ![${markdownImageAlt(records[i].alt)}](assets/registry/${records[i].file}) |" }
                }
                lines.addAll(['', '## Registry variants', ''])
                if (kind == 'entity') {
                    lines.addAll(['| Registry name | Numeric ID | Implementation |', '| --- | ---: | --- |'])
                    group.each { lines << "| `${it.name}` | ${it.id} | `${it['class']}` |" }
                } else {
                    lines.addAll(['| Field | Numeric ID | Implementation |', '| --- | ---: | --- |'])
                    group.each { lines << "| `${it.field}` | ${it.id} | `${it['class']}` |" }
                }
                def collected = [] as Set
                if (records) {
                    collected << 'wiki/assets/registry/render-manifest.json'
                    records.each { collected << "wiki/assets/registry/${it.file}".toString() }
                }
                group.each { entry ->
                    def key = entry[detailKey].toString()
                    def detail = details[[block: 'blocks', item: 'items', entity: 'entities'][kind]][key]
                    lines.addAll(['', "## ${group.size() > 1 ? key : 'Behavior'}", '', detail.purpose.toString(), '', detail.behavior.toString()])
                    if (detail.mode_notes) lines.addAll(['', "**Mode notes:** ${detail.mode_notes}"])
                    if (detail.interactions) { lines.addAll(['', '**Interactions:**', '']); detail.interactions.each { lines << "- ${it}" } }
                    if (detail.unknowns) { lines.addAll(['', '**Not established by current evidence:**', '']); detail.unknowns.each { lines << "- ${it}" } }
                    collected.addAll(detail.evidence.collect { it.toString() })
                }
                lines.addAll(['', '## Evidence', ''])
                collected.sort().each { lines << "- [`${it}`](${sourceUrl(it)})" }
                def indexPage = [block: 'Current Blocks', item: 'Current Items', entity: 'Current Entities'][kind]
                lines.addAll(['', "Back to [[${indexPage}]].", ''])
                // Keep the namespaced compatibility page even when a human-readable title is available.
                [pageName, featurePageName(label, name)].unique().each { outputPage ->
                    pages["${outputPage}.md".toString()] = lines.join('\n')
                    evidence[outputPage] = collected.sort()
                }
            }
        }
        [pages: pages, evidence: evidence]
    }

    String renderRegistry(String kind, List entries, Map targets) {
        String label = [block: 'Block', item: 'Item', entity: 'Entity'][kind]
        String plural = label == 'Entity' ? 'Entities' : label + 's'
        def lines = ["# Current ${plural}", '']
        if (kind == 'block') {
            int count = entries.collect { targets[[label, it.name]] }.toSet().size()
            lines.addAll(['> Generated from the current Java registry and English localization. IDs are current AC-Legacy IDs; historical Fandom ID charts are version-specific.', '',
                "The **${count} linked names** each open a source-backed block page. Rows that share a localized name are documented together as registry variants.", '',
                '| Name | ID | Registry field | Implementation |', '| --- | ---: | --- | --- |'])
        } else if (kind == 'item') {
            int count = entries.collect { targets[[label, it.name]] }.toSet().size()
            lines.addAll(['> Generated from the current Java registry and English localization. Numeric IDs are runtime `Item.id` values; Beta 1.7.3 adds 256 to the constructor index declared in `AC_Items.java`.', '',
                "The **${count} linked names** each open a source-backed item page.", '',
                '| Name | ID | Registry field | Implementation |', '| --- | ---: | --- | --- |'])
        } else {
            lines.addAll(['> Generated from the current entity registry. This lists AdventureCraft-owned entity registrations, not vanilla Minecraft entities.', '',
                '| Registry name | Numeric ID | Implementation |', '| --- | ---: | --- |'])
        }
        def path = [block: 'src/main/java/dev/adventurecraft/awakening/tile/AC_Blocks.java', item: 'src/main/java/dev/adventurecraft/awakening/item/AC_Items.java',
            entity: 'src/main/java/dev/adventurecraft/awakening/mixin/entity/MixinEntityRegistry.java'][kind]
        entries.each { entry ->
            def link = "${sourceUrl(path)}#L${entry.line}"
            def nameLink = pageLink(targets[[label, entry.name]], entry.name)
            lines << (kind == 'entity' ? "| ${nameLink} | ${entry.id} | [`${entry['class']}`](${link}) |" :
                "| ${nameLink} | ${entry.id} | `${entry.field}` | [`${entry['class']}`](${link}) |")
        }
        if (kind == 'block') lines.addAll(['', 'See [[Fact-Checking-and-Provenance]] for how legacy block pages are handled.'])
        lines << ''
        lines.join('\n')
    }

    String renderCommands(List commands, List rules) {
        def missing = commands.toSet() - COMMAND_DOCS.keySet()
        def stale = COMMAND_DOCS.keySet() - commands.toSet()
        require(!missing, "commands are missing documentation: ${missing.sort().join(', ')}")
        require(!stale, "documented commands are no longer registered: ${stale.sort().join(', ')}")
        def lines = ['# Current Commands', '',
            '> Generated from `ServerCommands.java` and `GameRules.java`. Each command links to a source-backed detail page.', '',
            'In single-player, command chat is normally opened while debug mode is active. Use `/help <command>` in game for the runtime command tree.', '',
            '| Command syntax | Current behavior |', '| --- | --- |']
        commands.each { name -> lines << "| ${pageLink('Command-' + name, COMMAND_DOCS[name][0], true)} | ${COMMAND_DOCS[name][1]} |" }
        lines.addAll(['', '## Game rules', '', '| Rule | Default | Type/range |', '| --- | --- | --- |'])
        rules.each { lines << "| `${it.name}` | `${it['default']}` | ${it.range} |" }
        lines.addAll(['', '## Historical command correction', '',
            'The current `/day` command sets time of day to `0`; `/night` sets it to `14000`, not the historical-wiki value of `12000`.', '',
            'The Fandom-era commands `/toggledecay`, `/togglemelting`, `/mobsburn` are not registered by the current source. Their behavior is represented by `/gamerule` entries above.', ''])
        lines.join('\n')
    }

    Map renderCommandPages(List commands, List rules) {
        def path = 'src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java'
        def source = read(path).readLines()
        def output = [:]
        commands.each { name ->
            def (syntax, description, notes) = COMMAND_DOCS[name]
            int line = source.findIndexOf { it.contains('literal("' + name + '")') } + 1
            if (line == 0) line = 1
            def lines = ["# /${name}", '', "`${syntax}`", '', description, '', '## Usage notes', '', notes]
            if (name == 'gamerule') {
                lines.addAll(['', '## Registered rules', '', '| Rule | Default | Type/range |', '| --- | --- | --- |'])
                rules.each { lines << "| `${it.name}` | `${it['default']}` | ${it.range} |" }
            }
            lines.addAll(['', '## Evidence', '', "[`ServerCommands.java` line ${line}](${sourceUrl(path)}#L${line})", '', 'Back to [[Current Commands]].', ''])
            output["Command-${name}.md".toString()] = lines.join('\n')
        }
        output
    }

    static String fandomPageUrl(String title) {
        def encoded = new StringBuilder()
        title.replace(' ', '_').getBytes(StandardCharsets.UTF_8).each { raw ->
            int value = raw & 0xff
            char character = (char) value
            if ((value >= 65 && value <= 90) || (value >= 97 && value <= 122) || (value >= 48 && value <= 57) || "_.~()'!,:-".indexOf(value) >= 0) {
                encoded.append(character)
            } else encoded.append(String.format('%%%02X', value))
        }
        FANDOM_WIKI + encoded
    }

    String renderBaseline(Map data) {
        def pages = data.pages
        def counts = pages.countBy { it.disposition }
        def lines = ['# Fandom Baseline Index', '',
            "> Inventory captured ${data.captured_at} from the [AdventureCraft Fandom wiki](${data.source}). It contains **${pages.size()}** non-map main-namespace pages from **${data.source_page_count}** source pages. **${data.excluded_legacy_map_page_count}** legacy community-map and map-catalog pages were removed by policy.", '',
            'This is a page-level provenance and triage record, not a claim that every historical sentence is valid for AC-Legacy. Only rewritten current pages and generated registries are published as current behavior.', '',
            '## Dispositions', '', '| Disposition | Pages | Meaning |', '| --- | ---: | --- |']
        DISPOSITION_LABELS.each { key, value -> lines << "| `${key}` | ${counts[key] ?: 0} | ${value} |" }
        lines.addAll(['', '## Complete page inventory', '', '| Fandom page | Last revision | Disposition | Current replacement |', '| --- | --- | --- | --- |'])
        pages.each { page ->
            def replacement = page.replacement ? "[[${page.replacement}]]" : '—'
            def timestamp = page.revision_timestamp.toString().split('T', 2)[0]
            def revisionUrl = "${fandomPageUrl(page.title)}?oldid=${page.revision_id}"
            lines << "| [${markdownEscape(page.title)}](${revisionUrl}) | ${timestamp} | `${page.disposition}` | ${replacement} |"
        }
        lines.addAll(['', "Baseline text remains available under [${data.license.name}](${data.license.url}) on Fandom. This repository stores metadata and hashes, not a bulk copy of Fandom prose or media.", ''])
        lines.join('\n')
    }

    Map renderAliasPages(List blocks, List items, List entities, Map targets) {
        def candidates = [:].withDefault { [] }
        [Block: blocks, Item: items, Entity: entities].each { label, entries ->
            entries.collect { it.name.toString() }.unique().sort { it.toLowerCase(Locale.ROOT) }.each { name -> candidates[name] << [label, targets[[label, name]]] }
        }
        def current = { label, name -> targets[[label, name]] ?: featurePageName(label, name) }
        candidates.putAll([
            'AdventureCraft Wiki': [['Wiki home', 'Home']],
            'Effects Block': [['Block', current('Block', 'Effect Block')]],
            'Lightbulb': [['Block', current('Block', 'Light Bulb')]],
            'Main Page': [['Wiki home', 'Home']],
            'Paintbrush': [['Item', current('Item', 'Paint Brush')]],
            'Pant Bucket': [['Item', current('Item', 'Paint Bucket')]],
            'Script': [['Block', current('Block', 'Script Block')]]
        ])
        def pages = [:]
        def evidence = [:]
        def index = ['# Aliases', '',
            'Current terminology aliases replace historical spelling variants and provide unambiguous entry points for generated feature pages. Community-map aliases are deliberately excluded.', '',
            '| Alias | Current target |', '| --- | --- |']
        def reserved = sourcePageNames() + ['Current-Blocks', 'Current-Items', 'Current-Entities', 'Current-Commands', 'Fandom-Baseline-Index',
            'Scripting-API', 'Verification-Status', 'Aliases', '_Sidebar', '_Footer'] + targets.values() + targets.keySet().collect { featurePageName(it[0], it[1]) }
        candidates.keySet().sort { it.toLowerCase(Locale.ROOT) }.each { displayName ->
            def alias = pageSlug(displayName)
            if (!(alias in reserved)) {
                def candidateTargets = candidates[displayName].unique()
                def lines = ["# ${displayName}", '']
                String targetText
                if (candidateTargets.size() == 1) {
                    def (label, target) = candidateTargets[0]
                    lines.addAll(["This term refers to ${pageLink(target, label + ': ' + displayName)}.", ''])
                    targetText = pageLink(target, displayName)
                } else {
                    lines.addAll(['This name is used by more than one current registry entry:', ''])
                    candidateTargets.each { pair -> lines << "- ${pageLink(pair[1], pair[0] + ': ' + displayName)}" }
                    lines << ''
                    targetText = candidateTargets.collect { pageLink(it[1], it[0]) }.join(', ')
                }
                lines.addAll(['This alias adds no behavior claims. See the target page for source evidence.', ''])
                pages["${alias}.md".toString()] = lines.join('\n')
                evidence[alias] = ['wiki/feature-details.json', 'wiki/fandom-baseline.json']
                index << "| ${pageLink(alias, displayName)} | ${targetText} |"
            }
        }
        index << ''
        pages['Aliases.md'] = index.join('\n')
        evidence['Aliases'] = ['wiki/feature-details.json', 'wiki/fandom-baseline.json']
        [pages: pages, evidence: evidence]
    }

    static String renderSidebar() {
        '''**AC-Legacy Wiki**

* [[Home]]

**Use and authoring**

* [[Getting Started]]
* [[Controls and Game Modes]]
* [[Scripting]]
* [[Glossary]]

**Generated reference**

* [[Current Commands]]
* [[Current Blocks]]
* [[Current Items]]
* [[Current Entities]]
* [[Scripting API]]

**Project and provenance**

* [[Release History]]
* [[Verification Status]]
* [[Media Provenance]]
* [[Fact Checking and Provenance]]
* [[Fandom Baseline Index]]
* [[Aliases]]
* [[Contributing to the Wiki]]
'''
    }

    static String renderFooter(Map properties, String revision) {
        def shortRevision = revision == 'unknown' ? revision : revision.take(12)
        """---

Applies to AC-Legacy **${properties.mod_version}** at source revision **`${shortRevision}`**. Current documentation is generated from the [AC-Legacy-Mod source](https://github.com/Adventurecraft-Awakening/AC-Legacy-Mod). Historical baseline links point to the [AdventureCraft Fandom wiki](https://adventurecraftmod.fandom.com/wiki/AdventureCraft_Wiki), whose text is reported as [CC-BY-SA](https://www.fandom.com/licensing). See [[Fact Checking and Provenance]] and [[Verification Status]].
"""
    }

    String renderVerificationStatus(List entries, Map properties, String revision) {
        def shortRevision = revision == 'unknown' ? revision : revision.take(12)
        def lines = ['# Verification Status', '', "> Generated for AC-Legacy **${properties.mod_version}** at source revision **`${shortRevision}`**.", '',
            'Every canonical page has a status and repository evidence. Generated reference pages are re-derived from their evidence on each build.', '',
            '| Page | Status | Evidence |', '| --- | --- | --- |']
        entries.sort { it.page.toLowerCase(Locale.ROOT) }.each { entry ->
            def links = entry.evidence.collect { "[`${new File(it.toString()).name}`](${sourceUrl(it.toString())})" }.join(', ')
            lines << "| [[${entry.page}]] | `${entry.status}` | ${links} |"
        }
        lines.addAll(['', 'Statuses describe evidence quality, not gameplay stability. `verified` means the documented current claim is backed by the listed source; `generated` is rebuilt from a registry/API source; `historical-index` only inventories retained historical references; `alias` redirects terminology without adding behavior claims.', ''])
        lines.join('\n')
    }

    static String expandTokens(String markdown, Map properties, Map baseline) {
        def tokens = [MOD_VERSION: properties.mod_version, MINECRAFT_VERSION: properties.minecraft_version, LOADER_VERSION: properties.loader_version,
            RHINO_VERSION: properties.rhino_version, BASELINE_CAPTURED_AT: baseline.captured_at, BASELINE_PAGE_COUNT: baseline.pages.size(),
            BASELINE_SOURCE_PAGE_COUNT: baseline.source_page_count, BASELINE_EXCLUDED_MAP_COUNT: baseline.excluded_legacy_map_page_count]
        tokens.each { name, value -> markdown = markdown.replace('{{' + name + '}}', value.toString()) }
        def unresolved = (markdown =~ /\{\{[A-Z0-9_]+\}\}/).collect { it }.unique().sort()
        require(!unresolved, "unresolved wiki template token(s): ${unresolved.join(', ')}")
        markdown
    }

    static String sha256(File file) { MessageDigest.getInstance('SHA-256').digest(file.bytes).encodeHex().toString() }

    static void write(File file, String content) {
        Files.createDirectories(file.parentFile.toPath())
        file.setText(content.stripTrailing() + '\n', 'UTF-8')
    }

    static void copy(File source, File destination) {
        Files.createDirectories(destination.parentFile.toPath())
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    /** Replaces only the selected generated output directory; reviewed wiki sources are protected. */
    List<File> build(File output) {
        output = output.canonicalFile
        def outputPath = output.toPath()
        def buildPath = new File(root, 'build').canonicalFile.toPath()
        require(output != root && output != wikiDir.canonicalFile && !root.toPath().startsWith(output.toPath()) &&
            !output.toPath().startsWith(wikiDir.canonicalFile.toPath()), "wiki output must be outside the reviewed wiki source directory: ${output}")
        require(!outputPath.startsWith(root.toPath()) || (outputPath.startsWith(buildPath) && outputPath != buildPath),
            "wiki output inside the repository must be beneath build/: ${output}")
        def properties = readProperties()
        def lang = readLang()
        def baseline = loadBaseline()
        def revision = gitRevision()
        def authored = loadPageMetadata()
        def blocks = parseBlocks(lang)
        def items = parseItems(lang)
        def entities = parseEntities()
        def commandData = parseCommands()
        def commands = commandData.commands
        def rules = commandData.rules
        def details = loadFeatureDetails(blocks, items, entities)
        def renders = WikiAssets.loadRegistryManifest(root, blocks, items, new File(wikiDir, 'assets/registry/render-manifest.json'))
        def targets = buildFeaturePageTargets(blocks, items, entities)
        def features = renderFeaturePages(blocks, items, entities, details, targets, renders)
        def aliases = renderAliasPages(blocks, items, entities, targets)
        def scriptTypes = WikiScriptApi.extractApi(new File(root, 'src/main/java/dev/adventurecraft/awakening/script'))
        def scriptPages = WikiScriptApi.renderReference(scriptTypes, sourceUrl(''))
        def generated = [
            'Current-Blocks.md': renderRegistry('block', blocks, targets), 'Current-Items.md': renderRegistry('item', items, targets),
            'Current-Entities.md': renderRegistry('entity', entities, targets), 'Current-Commands.md': renderCommands(commands, rules),
            'Fandom-Baseline-Index.md': renderBaseline(baseline), '_Sidebar.md': renderSidebar(), '_Footer.md': renderFooter(properties, revision)
        ]
        generated.putAll(renderCommandPages(commands, rules))
        generated.putAll(features.pages)
        generated.putAll(aliases.pages)
        generated.putAll(scriptPages)
        require(!output.exists() || output.deleteDir(), "cannot clear wiki output: ${output}")
        Files.createDirectories(output.toPath())
        sourcePages().each { source -> write(new File(output, source.name), expandTokens(read(source), properties, baseline)) }
        generated.each { name, content -> write(new File(output, name.toString()), content.toString()) }

        File logo = new File(wikiDir, 'assets/current/acLogo.png')
        byte[] logoBytes = logo.bytes
        require(logoBytes.length >= 24 && Arrays.equals(Arrays.copyOfRange(logoBytes, 0, 8), ([137, 80, 78, 71, 13, 10, 26, 10] as byte[])) &&
            ByteBuffer.wrap(logoBytes, 16, 4).int == 320 && ByteBuffer.wrap(logoBytes, 20, 4).int == 31,
            'documentation logo must be the composed 320x31 PNG')
        files(new File(wikiDir, 'assets')).findAll { mediaFile(it) }.each { source ->
            copy(source, new File(output, 'assets/' + new File(wikiDir, 'assets').toPath().relativize(source.toPath()).toString()))
        }
        copy(new File(root, 'src/main/resources/assets/adventurecraft/icon.png'), new File(output, 'assets/current/icon.png'))

        def verification = [:]
        authored.each { page, metadata -> verification[page] = [status: metadata.status, evidence: metadata.evidence] }
        def fixed = [
            'Current-Blocks': ['generated', ['src/main/java/dev/adventurecraft/awakening/tile/AC_Blocks.java', 'src/main/resources/assets/adventurecraft/lang/en_US.lang']],
            'Current-Items': ['generated', ['src/main/java/dev/adventurecraft/awakening/item/AC_Items.java', 'src/main/resources/assets/adventurecraft/lang/en_US.lang']],
            'Current-Entities': ['generated', ['src/main/java/dev/adventurecraft/awakening/mixin/entity/MixinEntityRegistry.java']],
            'Current-Commands': ['generated', ['src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java', 'src/main/java/dev/adventurecraft/awakening/world/GameRules.java']],
            'Fandom-Baseline-Index': ['historical-index', ['wiki/fandom-baseline.json']],
            '_Sidebar': ['navigation', [BUILDER_SOURCE]], '_Footer': ['navigation', [BUILDER_SOURCE]]
        ]
        if (renders != null) ['Current-Blocks', 'Current-Items'].each { fixed[it][1] << 'wiki/assets/registry/render-manifest.json' }
        fixed.each { page, data -> verification[page] = [status: data[0], evidence: data[1]] }
        features.evidence.each { page, evidence -> verification[page] = [status: 'generated', evidence: evidence] }
        aliases.evidence.each { page, evidence -> verification[page] = [status: 'alias', evidence: evidence] }
        commands.each { name -> verification["Command-${name}".toString()] = [status: 'generated', evidence: ['src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java']] }
        verification['Scripting-API'] = [status: 'generated', evidence: [SCRIPT_SOURCE, 'src/main/java/dev/adventurecraft/awakening/script/Script.java']]
        scriptTypes.each { type -> verification["Scripting-API-${type.name}".toString()] = [status: 'generated', evidence: [SCRIPT_SOURCE, type.sourcePath]] }
        verification['Verification-Status'] = [status: 'process', evidence: [BUILDER_SOURCE, 'wiki/page-metadata.json', 'wiki/feature-details.json']]
        def entries = verification.collect { page, metadata ->
            [page: page, status: metadata.status, applies_to: properties.mod_version, verified_at_commit: revision, evidence: metadata.evidence]
        }
        write(new File(output, 'Verification-Status.md'), renderVerificationStatus(entries, properties, revision))
        def pageFiles = (output.listFiles() ?: []).findAll { it.isFile() && it.name.endsWith('.md') }.sort { it.name }
        def pageNames = pageFiles.collect { it.name[0..-4] }.toSet()
        require(pageNames == verification.keySet(), "verification coverage mismatch; missing=${(pageNames - verification.keySet()).sort()}, stale=${(verification.keySet() - pageNames).sort()}")
        def byPage = entries.collectEntries { [(it.page): it] }
        def manifest = [format: 1, pages: pageFiles.collect { path ->
            def info = byPage[path.name[0..-4]]
            [file: path.name, sha256: sha256(path), status: info.status, applies_to: info.applies_to, verified_at_commit: info.verified_at_commit, evidence: info.evidence]
        }, assets: files(new File(output, 'assets')).collect { path -> [file: output.toPath().relativize(path.toPath()).toString().replace('\\', '/'), sha256: sha256(path)] }]
        write(new File(output, 'wiki-manifest.json'), JsonOutput.prettyPrint(JsonOutput.toJson(manifest)))
        pageFiles
    }

    static boolean mediaFile(File file) { MEDIA_SUFFIXES.any { file.name.toLowerCase(Locale.ROOT).endsWith(it) } }

    void validateCurrentEvidence() {
        def expected = [
            'build.gradle': ['JavaVersion.VERSION_27'],
            'src/main/java/dev/adventurecraft/awakening/ACMainThread.java': ['new File(getWorkingDirectory(), "../maps")'],
            'src/main/java/dev/adventurecraft/awakening/mixin/client/MixinMinecraft.java': [
                'eventKey == Keyboard.KEY_F5', 'this.options.thirdPersonView = !this.options.thirdPersonView',
                'EditorInput.handleShortcut', 'EditorInput.handleHistoryShortcut'],
            'src/main/java/dev/adventurecraft/awakening/client/input/EditorInput.java': [
                'EditorSession.forLevel(client.level)', 'key == Keyboard.KEY_F4', 'editor.active = !editor.active',
                'key == Keyboard.KEY_F6', 'if (editor.active)', 'resetAll()',
                'key == Keyboard.KEY_F7', 'displayGUIPalette()',
                '!editor.active || !controlDown', 'key == Keyboard.KEY_Z', 'ServerCommands.cmdUndo',
                'key == Keyboard.KEY_Y', 'ServerCommands.cmdRedo'],
            'src/main/java/dev/adventurecraft/awakening/common/AC_JScriptHandler.java': [
                'mapDirectory.toPath().resolve("scripts")', 'this.catalog.load(progress)'],
            'src/main/java/dev/adventurecraft/awakening/script/ScriptCatalog.java': [
                'Files.list(this.directory)', 'Files::isRegularFile',
                'WorldScriptRuntime.key(path.getFileName().toString()).endsWith(".js")'],
            'src/main/java/dev/adventurecraft/awakening/script/WorldScriptRuntime.java': [
                'new ScriptBindings(this, world, client)'],
            'src/main/java/dev/adventurecraft/awakening/script/ScriptBindings.java': ['objects.forEach(runtime::bind)'],
            'src/main/java/dev/adventurecraft/awakening/common/ServerCommands.java': ['exWorld.setTimeOfDay(0L)', 'exWorld.setTimeOfDay(14000L)']
        ]
        expected.each { path, snippets ->
            def source = read(path)
            snippets.each { require(source.contains(it), "current wiki evidence changed: '${it}' is missing from ${path}") }
        }
        def source = read('src/main/java/dev/adventurecraft/awakening/script/ScriptBindings.java')
        def installed = (source =~ /objects\.put\("([^"]+)"/).collect { it[1] }.toSet()
        def missing = ['time', 'world', 'chat', 'weather', 'effect', 'particle', 'sound', 'ui', 'screen', 'script', 'keyboard', 'renderer'].toSet() - installed
        require(!missing, "current scripting globals changed; missing: ${missing.sort().join(', ')}")
        def obsolete = ['toggledecay', 'togglemelting', 'mobsburn'].toSet().intersect(parseCommands().commands.toSet())
        require(!obsolete, "historical commands were registered again; review the wiki: ${obsolete.sort().join(', ')}")
    }

    static Set<String> wikiLinkTargets(String markdown) {
        def targets = (markdown =~ /\[\[([^\]|#]+)/).collect { it[1].trim().replace(' ', '-') }.toSet()
        def links = markdown =~ /(?<!!)\[(?:[^\]\n]|\](?!\())*\]\(([^)\n]+)\)/
        links.each { match ->
            String destination = match[1].trim()
            if (destination.startsWith('<') && destination.contains('>')) destination = destination.substring(1, destination.indexOf('>'))
            else destination = destination.split(/\s+["']/, 2)[0]
            if (!(destination =~ /^[A-Za-z][A-Za-z0-9+.-]*:/).find() && !destination.startsWith('/') && !destination.startsWith('#')) {
                String path = destination.split(/[?#]/, 2)[0]
                path = URLDecoder.decode(path.replace('+', '%2B'), StandardCharsets.UTF_8).trim()
                if (path && !path.contains('/') && !path.contains('\\')) {
                    if (path.toLowerCase(Locale.ROOT).endsWith('.md')) path = path.substring(0, path.length() - 3)
                    else if (path.contains('.')) return
                    targets << path.replace(' ', '-')
                }
            }
        }
        targets
    }

    void check() {
        validateCurrentEvidence()
        def baseline = loadBaseline()
        def unknown = baseline.pages.collect { it.disposition }.toSet() - DISPOSITION_LABELS.keySet()
        require(!unknown, "unknown Fandom baseline disposition(s): ${unknown.sort().join(', ')}")
        File temporary = Files.createTempDirectory('ac-wiki-').toFile()
        try {
            def output = new File(temporary, 'first')
            def pages = build(output)
            def names = pages.collect { it.name[0..-4] }.toSet()
            def errors = []
            (['Home', '_Sidebar', '_Footer', 'Fact-Checking-and-Provenance'].toSet() - names).sort().each { errors << "missing required wiki page ${it}.md" }
            names.each { if ((it =~ /[\\\/:*?"<>|]/).find()) errors << "invalid GitHub wiki page title: ${it}" }
            require(names.size() == names.collect { it.toLowerCase(Locale.ROOT) }.toSet().size(), 'case-insensitive wiki page filename collision')
            def forbidden = [
                'JDK **21**': 'current game build requires Java 27', 'Java **21**': 'current game build requires Java 27', 'Java 21+': 'current game build requires Java 27',
                'JDK **25**': 'current game build requires Java 27', 'Java **25**': 'current game build requires Java 27',
                'Fabric Loader 0.16': 'current minimum is read from gradle.properties',
                'F5 toggles level editing': 'F5 toggles third-person view; /mapedit controls map editing',
                'player.sendMessage("Hello': 'current chat output API is chat.print',
                'world.setBlock(x': 'current API uses setBlockID/setBlockIDAndMetadata'
            ]
            pages.each { page ->
                def content = read(page)
                wikiLinkTargets(content).each { target -> if (!(target in names)) errors << "${page.name}: broken internal wiki link '${target}'" }
                forbidden.each { text, reason -> if (content.contains(text)) errors << "${page.name}: stale claim '${text}' (${reason})" }
            }
            def second = new File(temporary, 'second')
            build(second)
            def firstFiles = files(output).collectEntries { [(output.toPath().relativize(it.toPath()).toString()): sha256(it)] }
            def secondFiles = files(second).collectEntries { [(second.toPath().relativize(it.toPath()).toString()): sha256(it)] }
            if (firstFiles != secondFiles) errors << 'wiki output is not deterministic'
            require(!errors, 'wiki validation failed:\n  - ' + errors.join('\n  - '))
            println "Validated ${pages.size()} generated wiki pages and ${baseline.pages.size()} Fandom baseline records."
        } finally { temporary.deleteDir() }
    }

    Map apiRequest(Map parameters) {
        def query = (parameters + [format: 'json', formatversion: '2']).collect { key, value ->
            URLEncoder.encode(key.toString(), StandardCharsets.UTF_8) + '=' + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8)
        }.join('&')
        def connection = URI.create(FANDOM_API + '?' + query).toURL().openConnection()
        connection.setRequestProperty('User-Agent', USER_AGENT)
        connection.connectTimeout = 60000
        connection.readTimeout = 60000
        try {
            connection.inputStream.withCloseable { stream ->
                def response = new JsonSlurper().parse(stream, 'UTF-8')
                require(response instanceof Map && !response.error, "Fandom API request failed: ${response.error ?: 'invalid response'}")
                (Map) response
            }
        } finally { if (connection instanceof HttpURLConnection) connection.disconnect() }
    }

    List classifyPage(String title, List categories, Set currentNames) {
        if (REPLACEMENTS[title]) return ['rewritten-current', REPLACEMENTS[title]]
        def categoryNames = categories.collect { it.toLowerCase(Locale.ROOT) }.toSet()
        def categoryText = categoryNames.join(' ')
        if (title.toLowerCase(Locale.ROOT) in currentNames) {
            if (categoryText.contains('item')) return ['inventory-verified', 'Current-Items']
            if (categoryText.contains('mob') || title.toLowerCase(Locale.ROOT).contains('npc')) return ['inventory-verified', 'Current-Entities']
            return ['inventory-verified', 'Current-Blocks']
        }
        if (categoryNames.intersect(LEGACY_MAP_CATEGORIES)) return ['historical-map', null]
        if ('scripts' in categoryNames) return ['historical-script', null]
        ['historical-reference', null]
    }

    Set<String> fetchLegacyMapCatalogLinks() {
        Set<String> titles = new LinkedHashSet<>(LEGACY_MAP_CATALOG_PAGES)
        def parameters = [action: 'query', prop: 'links', plnamespace: '0', pllimit: 'max', titles: LEGACY_MAP_CATALOG_PAGES.sort().join('|')]
        while (true) {
            def response = apiRequest(parameters)
            response.query.pages.each { page -> (page.links ?: []).each { titles << it.title.toString() } }
            if (!response['continue']) return titles
            response['continue'].each { key, value -> parameters[key] = value.toString() }
        }
    }

    /** Network refresh is explicit and is never a dependency of building or checking the wiki. */
    void syncBaseline(boolean checkOnly = false) {
        def pageIndex = []
        def parameters = [action: 'query', list: 'allpages', apnamespace: '0', aplimit: 'max']
        while (true) {
            def response = apiRequest(parameters)
            pageIndex.addAll(response.query.allpages)
            if (!response['continue']) break
            response['continue'].each { key, value -> parameters[key] = value.toString() }
        }
        def currentNames = readLang().findAll { key, value -> (key.startsWith('tile.') || key.startsWith('item.')) && key.endsWith('.name') }
            .values().collect { it.toLowerCase(Locale.ROOT) }.toSet()
        currentNames.addAll(parseEntities().collect { it.name.toLowerCase(Locale.ROOT) })
        def records = []
        pageIndex.collate(20).each { batch ->
            def response = apiRequest([action: 'query', prop: 'revisions', rvprop: 'ids|timestamp|size|sha1|content', rvslots: 'main', titles: batch.collect { it.title }.join('|')])
            response.query.pages.each { page ->
                def revision = page.revisions[0]
                String content = revision.slots.main.content ?: ''
                def categories = (content =~ /(?i)\[\[Category:([^\]|]+)/).collect { it[1].trim() }.unique().sort()
                def redirect = content =~ /(?i)^\s*#REDIRECT\s*\[\[([^\]|#]+)/
                def (disposition, replacement) = classifyPage(page.title, categories, currentNames)
                records << [title: page.title, page_id: page.pageid, revision_id: revision.revid, revision_timestamp: revision.timestamp,
                    content_bytes: revision.size, content_sha1: revision.sha1, is_redirect: content.stripLeading().toUpperCase(Locale.ROOT).startsWith('#REDIRECT'),
                    redirect_target: redirect.find() ? redirect.group(1).trim() : null, categories: categories, disposition: disposition, replacement: replacement]
            }
        }
        int sourceCount = records.size()
        def catalog = fetchLegacyMapCatalogLinks()
        def excluded = records.findAll { it.disposition == 'historical-map' || it.title in catalog || it.title.toLowerCase(Locale.ROOT).contains('map') }
        records.removeAll(excluded)
        def site = apiRequest([action: 'query', meta: 'siteinfo', siprop: 'general|rightsinfo']).query
        records.sort { it.title.toLowerCase(Locale.ROOT) }
        def snapshot = [format: 1, source: site.general.base, api: FANDOM_API, captured_at: site.general.time,
            scope: 'Non-map namespace-0 pages returned by list=allpages; legacy community maps and catalogs are excluded; content is hashed but not copied.',
            source_page_count: sourceCount, excluded_legacy_map_page_count: excluded.size(), license: [name: site.rightsinfo.text, url: site.rightsinfo.url], pages: records]
        if (checkOnly) {
            def existing = json(baselineFile)
            snapshot.captured_at = existing.captured_at
            require(existing == snapshot, 'the committed Fandom baseline differs from the live API; run `./gradlew -p wiki syncBaseline`')
            println "Fandom baseline is current (${records.size()} pages)."
        } else {
            write(baselineFile, JsonOutput.prettyPrint(JsonOutput.toJson(snapshot)))
            println "Wrote ${records.size()} Fandom page records to ${display(baselineFile)}."
        }
    }
}
