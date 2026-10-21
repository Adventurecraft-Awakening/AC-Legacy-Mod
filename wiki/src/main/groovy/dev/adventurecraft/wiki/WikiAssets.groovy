package dev.adventurecraft.wiki

import groovy.json.JsonSlurper

import java.nio.ByteBuffer
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.CRC32

/** Asset coverage, PNG integrity, and publication checks shared by local builds and CI. */
class WikiAssets {
    private static final byte[] PNG_SIGNATURE = [137, 80, 78, 71, 13, 10, 26, 10].collect { (byte) it } as byte[]
    private static final Set RECORD_KEYS = ['field', 'id', 'file', 'sha256', 'alt'] as Set

    static String sha256(File file) {
        MessageDigest.getInstance('SHA-256').digest(file.bytes).encodeHex().toString()
    }

    static List<File> files(File root) {
        if (!root.isDirectory()) return []
        List<File> result = []
        root.eachFileRecurse { File file ->
            require(!Files.isSymbolicLink(file.toPath()), "Symlinks are forbidden: ${file}")
            if (file.isFile()) result.add(file)
        }
        result.sort { a, b -> relative(root, a) <=> relative(root, b) }
    }

    static String relative(File root, File file) {
        root.toPath().relativize(file.toPath()).toString().replace(File.separator, '/')
    }

    static Map json(File file) {
        require(file.isFile(), "Required manifest is missing: ${file}")
        def data = new JsonSlurper().parse(file, 'UTF-8')
        require(data instanceof Map, "Manifest must contain an object: ${file}")
        data as Map
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message)
    }

    static File safeFile(File root, Object path) {
        require(path instanceof String && path && !path.contains('\\'), "Unsafe asset path: ${path}")
        File file = new File(root, path as String)
        require(!new File(path as String).absolute && !(path as String).split('/').any { it in ['..', '.', ''] }, "Unsafe asset path: ${path}")
        require(file.canonicalPath.startsWith(root.canonicalPath + File.separator), "Asset path escapes output: ${path}")
        File ancestor = file
        while (ancestor != null && ancestor != root) {
            require(!Files.isSymbolicLink(ancestor.toPath()), "Symlinks are forbidden: ${ancestor}")
            ancestor = ancestor.parentFile
        }
        require(file.isFile(), "Missing asset: ${file}")
        file
    }

    static void validateRgbaPng(File file, int width, int height) {
        require(file.isFile(), "Registry render is missing: ${file}")
        byte[] data = file.bytes
        require(data.length >= 8 && Arrays.equals(Arrays.copyOf(data, 8), PNG_SIGNATURE), "Registry render is not a PNG: ${file}")
        int offset = 8
        boolean ihdr = false, idat = false, iend = false
        while (offset < data.length) {
            require(offset + 12L <= data.length, "Truncated PNG chunk: ${file}")
            long length = Integer.toUnsignedLong(ByteBuffer.wrap(data, offset, 4).getInt())
            long end = offset + 12L + length
            require(end <= data.length, "Truncated PNG chunk: ${file}")
            String type = new String(data, offset + 4, 4, 'US-ASCII')
            CRC32 crc = new CRC32()
            crc.update(data, offset + 4, (int) length + 4)
            long expected = Integer.toUnsignedLong(ByteBuffer.wrap(data, (int) (end - 4), 4).getInt())
            require(crc.value == expected, "Invalid PNG checksum: ${file}")
            if (!ihdr) {
                require(type == 'IHDR' && length == 13, "PNG needs a leading IHDR: ${file}")
                ByteBuffer header = ByteBuffer.wrap(data, offset + 8, 13)
                int actualWidth = header.getInt(), actualHeight = header.getInt()
                require(actualWidth == width && actualHeight == height, "PNG dimensions must be ${width}x${height}, found ${actualWidth}x${actualHeight}: ${file}")
                require((header.get() & 255) == 8 && (header.get() & 255) == 6, "Registry render must be 8-bit RGBA PNG: ${file}")
                require(header.get() == 0 && header.get() == 0 && header.get() == 0, "Unsupported PNG encoding: ${file}")
                ihdr = true
            } else {
                require(type != 'IHDR', "PNG has multiple IHDR chunks: ${file}")
            }
            if (type == 'IDAT') idat = true
            if (type == 'IEND') {
                require(length == 0 && end == data.length, "Invalid PNG IEND: ${file}")
                iend = true
                break
            }
            offset = (int) end
        }
        require(ihdr && idat && iend, "Incomplete PNG: ${file}")
    }

    static Map loadRegistryManifest(File root, List blocks, List items, File manifestFile = null) {
        File path = manifestFile ?: new File(root, 'wiki/assets/registry/render-manifest.json')
        if (!path.isFile()) return null
        Map data = json(path)
        require(data.format == 1, 'Wiki registry render manifest must use format 1')
        require(data.width instanceof Integer && data.width > 0 && data.height instanceof Integer && data.height > 0,
            'Wiki registry render dimensions must be positive integers')
        require(data.width == data.height, 'Wiki registry renders must use fixed square dimensions')
        require(data.contains_community_maps == false, 'Wiki registry render manifest must explicitly exclude community maps')
        Map result = [:]
        [blocks: blocks, items: items].each { String kind, List expectedEntries ->
            require(data[kind] instanceof List, "Registry render ${kind} must be an array")
            Map byField = [:]
            data[kind].each { record ->
                require(record instanceof Map && record.keySet() == RECORD_KEYS, "Invalid registry render ${kind} record")
                require(record.field instanceof String && record.field, "Registry render ${kind} entry has no field")
                require(!byField.containsKey(record.field), "Duplicate registry render: ${kind}.${record.field}")
                byField[record.field] = record
            }
            Map expectedByField = expectedEntries.collectEntries { [(it.field.toString()): it] }
            require(byField.keySet() == expectedByField.keySet(), "${kind} registry render coverage mismatch; missing=${expectedByField.keySet() - byField.keySet()}, extra=${byField.keySet() - expectedByField.keySet()}")
            expectedByField.each { String field, expected ->
                Map record = byField[field]
                require(record.id instanceof Integer && record.id == expected.id, "Registry render ${kind}.${field} ID does not match current registry")
                require(record.file == "${kind}/${field}.png".toString(), "Registry render ${kind}.${field} has an unexpected file")
                require(record.alt instanceof String && record.alt.trim() && !record.alt.contains('\n') && !record.alt.contains('\r'), "Registry render ${kind}.${field} needs single-line alt text")
                require(record.sha256 instanceof String && record.sha256 ==~ /[0-9a-f]{64}/, "Registry render ${kind}.${field} needs lowercase SHA-256")
                File image = safeFile(path.parentFile, record.file)
                validateRgbaPng(image, data.width as int, data.height as int)
                require(sha256(image) == record.sha256, "Registry render checksum mismatch: ${image}")
            }
            Set actual = files(new File(path.parentFile, kind)).collect { relative(path.parentFile, it) } as Set
            Set expected = byField.values().collect { it.file } as Set
            require(actual == expected, "${kind} registry render file coverage mismatch; missing=${expected - actual}, extra=${actual - expected}")
            result[kind] = byField
        }
        result
    }

    static void verifyRenderedAssets(File root, File rendered) {
        File committedManifest = new File(root, 'wiki/assets/registry/render-manifest.json')
        Map committed = json(committedManifest)
        File renderedManifest = new File(rendered, 'render-manifest.json')
        Map generated = json(renderedManifest)
        [committed, generated].each { Map manifest ->
            require(manifest.width == 128 && manifest.height == 128, 'Registry asset checks require 128x128 renders')
            require(manifest.blocks instanceof List && manifest.blocks && manifest.items instanceof List && manifest.items, 'Registry asset arrays must be non-empty')
        }
        // Each tree owns its hashes; pixels may differ between graphics drivers.
        loadRegistryManifest(root, committed.blocks, committed.items, committedManifest)
        loadRegistryManifest(root, committed.blocks, committed.items, renderedManifest)
        ['blocks', 'items'].each { String kind ->
            Set expected = committed[kind].collect { [it.field, it.id, it.file, it.alt] } as Set
            Set actual = generated[kind].collect { [it.field, it.id, it.file, it.alt] } as Set
            require(actual == expected, "Regenerated ${kind} registry identity differs; missing=${expected - actual}, extra=${actual - expected}")
        }
        println "Verified ${committed.blocks.size()} blocks and ${committed.items.size()} items in committed and regenerated asset trees."
    }

    static void verifyManifest(File output, int minimumPages = 250) {
        require(minimumPages > 0, 'Wiki minimum page count must be positive')
        require(!Files.isSymbolicLink(output.toPath()), "Symlinks are forbidden: ${output}")
        Map manifest = json(new File(output, 'wiki-manifest.json'))
        require(manifest.format == 1 && manifest.pages instanceof List && manifest.assets instanceof List, 'Wiki manifest must use format 1 with pages and assets arrays')
        require(manifest.pages.size() >= minimumPages, "Generated ${manifest.pages.size()} pages; deletion safety floor is ${minimumPages}")
        Set<String> declared = [] as Set
        manifest.pages.each { entry ->
            require(entry instanceof Map && entry.file instanceof String && entry.file.endsWith('.md') && !entry.file.contains('/'), "Invalid wiki page manifest entry: ${entry}")
        }
        manifest.assets.each { entry ->
            require(entry instanceof Map && entry.file instanceof String && entry.file.startsWith('assets/'), "Invalid wiki asset manifest entry: ${entry}")
        }
        (manifest.pages + manifest.assets).each { entry ->
            require(declared.add(entry.file as String), "Duplicate wiki manifest file: ${entry.file}")
            File file = safeFile(output, entry.file)
            require(entry.sha256 instanceof String && entry.sha256 ==~ /[0-9a-f]{64}/, "Invalid SHA-256 for ${entry.file}")
            require(sha256(file) == entry.sha256, "Wiki checksum mismatch: ${entry.file}")
        }
        Set actual = files(output).collect { relative(output, it) }.findAll { it != 'wiki-manifest.json' } as Set
        require(actual == declared, "Wiki output manifest mismatch; missing=${declared - actual}, extra=${actual - declared}")
        ['Home.md', '_Sidebar.md', '_Footer.md'].each { require(declared.contains(it), "Missing required wiki page: ${it}") }
        println "Verified ${manifest.pages.size()} wiki pages and ${manifest.assets.size()} media assets."
    }
}
