package dev.adventurecraft.wiki

/** Entry point shared by the standalone Gradle wiki tasks. */
class WikiTool {
    static void main(String[] args) {
        if (args.length < 2) throw new IllegalArgumentException('Expected command and repository root')
        File root = new File(args[1]).canonicalFile
        switch (args[0]) {
            case 'build':
                new WikiBuilder(root).build(new File(args[2]))
                break
            case 'sources':
                WikiQuality.checkSources(new File(root, 'wiki/pages'))
                new WikiBuilder(root).check()
                break
            case 'quality':
                WikiQuality.checkQuality(new File(args[2]), positiveInteger(args[3]))
                WikiAssets.verifyManifest(new File(args[2]), positiveInteger(args[3]))
                break
            case 'manifest':
                WikiAssets.verifyManifest(new File(args[2]), positiveInteger(args[3]))
                break
            case 'assets':
                WikiAssets.verifyRenderedAssets(root, resolve(root, args[2]))
                break
            case 'links':
                WikiQuality.checkLinks(new File(args[2]), resolve(root, args[3]), !Boolean.parseBoolean(args[4]))
                break
            case 'sync-baseline':
            case 'check-baseline':
                new WikiBuilder(root).syncBaseline(args[0] == 'check-baseline')
                break
            default:
                throw new IllegalArgumentException("Unknown wiki command: ${args[0]}")
        }
    }

    static File resolve(File root, String path) {
        File file = new File(path)
        file.absolute ? file : new File(root, path)
    }

    static int positiveInteger(String value) {
        if (!(value ==~ /[1-9][0-9]*/)) throw new IllegalArgumentException('wikiMinimumPages must be a positive integer')
        Integer.parseInt(value)
    }
}
