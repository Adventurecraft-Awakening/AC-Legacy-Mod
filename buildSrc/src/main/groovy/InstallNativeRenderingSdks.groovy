import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

/** Downloads accepted vendor SDKs into the user's cache, never into source or JARs. */
@DisableCachingByDefault(because = "Installs separately licensed SDKs and remembers this checkout's development setup")
abstract class InstallNativeRenderingSdks extends DefaultTask {
    @Input abstract Property<Boolean> getAcceptNvidiaLicense()
    @Input abstract Property<Boolean> getOffline()
    @Input abstract Property<String> getPlatform()
    @InputFile abstract RegularFileProperty getManifestFile()
    @OutputDirectory abstract DirectoryProperty getSdkDirectory()
    @OutputFile abstract RegularFileProperty getLocalPropertiesFile()

    @TaskAction void install() {
        def manifest = new JsonSlurper().parse(manifestFile.get().asFile)
        if (!acceptNvidiaLicense.get()) {
            throw new GradleException("Read the pinned DLSS and NRD licenses, then run installNativeRenderingSdks -PacceptNvidiaNativeLicense=true.\n" +
                manifest.licenses.collect { "${it.name}: ${it.url}" }.join("\n"))
        }
        String targetPlatform = platform.get()
        if (!(targetPlatform in manifest.platforms)) {
            throw new GradleException("Native rendering SDK setup is not available for ${targetPlatform}; the built-in renderer remains available.")
        }
        Path root = sdkDirectory.get().asFile.toPath().toAbsolutePath().normalize()
        Path checkout = localPropertiesFile.get().asFile.parentFile.toPath().toRealPath()
        if (root.startsWith(checkout)) {
            throw new GradleException("Native SDKs must be installed outside the repository: ${root}")
        }
        Files.createDirectories(root)
        root = root.toRealPath()
        if (root.startsWith(checkout)) {
            throw new GradleException("Native SDK cache resolves inside the repository: ${root}")
        }
        // Serialize installations from different development checkouts.
        new RandomAccessFile(root.resolve(".install.lock").toFile(), "rw").withCloseable { file ->
            file.channel.lock().withCloseable {
                for (def artifact : manifest.artifacts) {
                    if (artifact.platform != null && artifact.platform != targetPlatform) continue
                    if (artifact.files != null) installFiles(root, artifact)
                    else installArchive(root, artifact)
                }
                manifest.licenses.each { license ->
                    Path source = checkedPath(root, license.path.toString())
                    if (!Files.isRegularFile(source) || sha256(source) != license.sha256) {
                        throw new GradleException("Installed license does not match the accepted ${license.name} license.")
                    }
                }
                root.resolve("license-acceptance.json").toFile().setText(JsonOutput.prettyPrint(JsonOutput.toJson([
                    acceptanceId: manifest.acceptanceId,
                    licenses: manifest.licenses,
                    explicitlyAccepted: true
                ])) + "\n", "UTF-8")
            }
        }
        File settings = localPropertiesFile.get().asFile
        Properties values = new Properties()
        if (settings.isFile()) settings.withInputStream { values.load(it) }
        boolean changed = values.getProperty("nativeRenderingSdkRoot") != root.toString() ||
            values.getProperty("nativeRenderingLicenseAcceptance") != manifest.acceptanceId.toString()
        values.setProperty("nativeRenderingSdkRoot", root.toString())
        values.setProperty("nativeRenderingLicenseAcceptance", manifest.acceptanceId.toString())
        if (changed) settings.withOutputStream { values.store(it, "Local AdventureCraft development settings; do not commit") }
        logger.lifecycle("Installed native rendering SDKs at ${root}.")
        logger.lifecycle("Accepted SDK setup is remembered for subsequent development builds and runs.")
    }

    protected void installFiles(Path root, def artifact) {
        for (def source : artifact.files) {
            if (source.platform != null && source.platform != platform.get()) continue
            Path destination = checkedPath(root.resolve(artifact.directory.toString()), source.path.toString())
            if (Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS) && sha256(destination) == source.sha256) continue
            Files.createDirectories(destination.parent)
            Path temporary = Files.createTempFile(destination.parent, ".native-", ".download")
            try {
                download(source, temporary)
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                Files.deleteIfExists(temporary)
            }
        }
    }

    protected void installArchive(Path root, def artifact) {
        Path destination = checkedPath(root, artifact.directory.toString())
        Path receipt = destination.resolve(".ac-installed.json")
        if (Files.isRegularFile(receipt, LinkOption.NOFOLLOW_LINKS)) {
            def previous = new JsonSlurper().parse(receipt.toFile())
            if (previous.archiveSha256 == artifact.sha256 && previous.files.every { relative, expected ->
                Path file = checkedPath(destination, relative.toString())
                Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) && sha256(file) == expected
            }) return
        }
        Path downloads = root.resolve(".downloads")
        Files.createDirectories(downloads)
        Path archive = downloads.resolve(artifact.sha256.toString() + "." + artifact.archive)
        if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS) || sha256(archive) != artifact.sha256) {
            Path temporary = Files.createTempFile(downloads, ".native-", ".download")
            try {
                download(artifact, temporary)
                Files.move(temporary, archive, StandardCopyOption.REPLACE_EXISTING)
            } finally {
                Files.deleteIfExists(temporary)
            }
        }
        Path staging = Files.createTempDirectory(root, ".extract-")
        Map<String, String> files = new TreeMap<>()
        try {
            if (artifact.archive == "zip") {
                new ZipInputStream(Files.newInputStream(archive)).withCloseable { input ->
                    for (def entry = input.nextEntry; entry != null; entry = input.nextEntry) {
                        extractEntry(staging, artifact, entry.name, entry.directory, false, input, files)
                    }
                }
            } else if (artifact.archive == "tar.gz") {
                new TarArchiveInputStream(new GZIPInputStream(Files.newInputStream(archive))).withCloseable { input ->
                    for (def entry = input.nextEntry; entry != null; entry = input.nextEntry) {
                        if (!(entry.directory || entry.file)) throw new GradleException("Unsupported archive entry ${entry.name}; links are not installed.")
                        extractEntry(staging, artifact, entry.name, entry.directory, (entry.mode & 0111) != 0, input, files)
                    }
                }
            } else {
                throw new GradleException("Unsupported SDK archive format ${artifact.archive}")
            }
            if (files.isEmpty()) throw new GradleException("SDK archive ${artifact.id} contained no selected files.")
            staging.resolve(".ac-installed.json").toFile().setText(JsonOutput.toJson([
                archiveSha256: artifact.sha256, files: files
            ]) + "\n", "UTF-8")
            // A failed extraction leaves the previous complete component untouched.
            if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) deleteTree(destination)
            Files.move(staging, destination)
        } finally {
            if (Files.exists(staging)) deleteTree(staging)
        }
    }

    protected static void extractEntry(Path staging, def artifact, String rawName, boolean directory,
                                     boolean executable, InputStream input, Map<String, String> files) {
        String name = rawName
        while (name.startsWith("./")) name = name.substring(2)
        // Validate the original path before stripping its archive root or filtering.
        if (name.isEmpty()) return
        checkedPath(staging, name)
        if (artifact.stripRoot) {
            int slash = name.indexOf('/')
            if (slash < 0) return
            name = name.substring(slash + 1)
        }
        if (name.isEmpty()) return
        if (artifact.include != null && !artifact.include.any { name == it || name.startsWith(it.toString()) }) return
        Path destination = checkedPath(staging, name)
        if (directory) {
            Files.createDirectories(destination)
            return
        }
        Files.createDirectories(destination.parent)
        Files.copy(input, destination)
        if (executable && !destination.toFile().setExecutable(true, false)) {
            throw new GradleException("Could not mark native tool executable: ${name}")
        }
        files.put(name, sha256(destination))
    }

    protected void download(def source, Path temporary) {
        URI uri = URI.create(source.url.toString())
        if (uri.scheme != "https") throw new GradleException("SDK download must use HTTPS: ${uri}")
        if (offline.get()) throw new GradleException("Pinned SDK file is not cached or failed verification; rerun without --offline to download ${uri}.")
        logger.lifecycle("Downloading ${uri}")
        def connection = uri.toURL().openConnection()
        connection.connectTimeout = 15000
        connection.readTimeout = 60000
        connection.setRequestProperty("User-Agent", "AdventureCraft-Native-SDK-Setup")
        connection.inputStream.withCloseable { input -> Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING) }
        if (sha256(temporary) != source.sha256) {
            throw new GradleException("SHA-256 mismatch for ${uri}; SDK installation stopped.")
        }
    }

    protected static Path checkedPath(Path root, String relative) {
        if (relative.contains('\\') || relative.contains(':') || relative.startsWith('/')) {
            throw new GradleException("Invalid SDK archive path ${relative}")
        }
        Path resolved = root.resolve(relative).normalize()
        if (!resolved.startsWith(root) || relative.split('/').contains('..')) {
            throw new GradleException("SDK archive path escapes its directory: ${relative}")
        }
        // No existing symlink is ever followed when writing into the shared cache.
        for (Path path = resolved; path != null && path.startsWith(root); path = path.parent) {
            if (Files.isSymbolicLink(path)) throw new GradleException("SDK cache contains a symbolic link: ${path}")
        }
        return resolved
    }

    static String sha256(Path source) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(source).withCloseable { input ->
            byte[] buffer = new byte[1024 * 1024]
            for (int count = input.read(buffer); count >= 0; count = input.read(buffer)) {
                if (count > 0) digest.update(buffer, 0, count)
            }
        }
        return digest.digest().encodeHex().toString()
    }

    protected static void deleteTree(Path root) {
        Files.walk(root).withCloseable { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
        }
    }
}
