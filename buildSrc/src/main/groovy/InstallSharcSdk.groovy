import groovy.json.JsonSlurper
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
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/** Explicit, licensed SDK installation; ordinary builds only read the installed SDK. */
@DisableCachingByDefault(because = "Installs an accepted SDK and configures this checkout's local development environment")
abstract class InstallSharcSdk extends DefaultTask {
    @Input abstract Property<Boolean> getAcceptLicense()
    @InputFile abstract RegularFileProperty getProvenanceFile()
    @OutputDirectory abstract DirectoryProperty getSdkDirectory()
    @OutputFile abstract RegularFileProperty getLocalPropertiesFile()

    @TaskAction void install() {
        if (!acceptLicense.get()) {
            throw new GradleException("Read third_party/sharc/README.md and NVIDIA's license first, then run installSharcSdk -PacceptSharcLicense=true.")
        }
        def pin = new JsonSlurper().parse(provenanceFile.get().asFile)
        File sdk = sdkDirectory.get().asFile
        pin.sha256.each { relative, expected ->
            File destination = new File(sdk, relative.toString())
            if (!destination.isFile() || InstallSharcSdk.sha256(destination) != expected) {
                destination.parentFile.mkdirs()
                def temporary = Files.createTempFile(destination.parentFile.toPath(), ".sharc-", ".download")
                try {
                    def connection = URI.create("https://raw.githubusercontent.com/NVIDIA-RTX/SHARC/${pin.commit}/${relative}").toURL().openConnection()
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000
                    connection.getInputStream().withCloseable { input -> Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING) }
                    if (InstallSharcSdk.sha256(temporary.toFile()) != expected) {
                        throw new GradleException("NVIDIA SHARC hash mismatch for ${relative}; installation stopped.")
                    }
                    Files.move(temporary, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } finally {
                    Files.deleteIfExists(temporary)
                }
            }
        }
        // Keep acceptance and NVIDIA's source outside the MIT repository.
        new File(sdk, "license-acceptance.txt").setText(
            "NVIDIA RTX SDKs license explicitly accepted during installSharcSdk.\nRevision: ${pin.commit}\nLicense SHA-256: ${pin.sha256['License.md']}\n", "UTF-8")
        File settings = localPropertiesFile.get().asFile
        Properties values = new Properties()
        if (settings.isFile()) settings.withInputStream { values.load(it) }
        values.setProperty("sharcSdkDir", sdk.absolutePath)
        settings.withOutputStream { values.store(it, "Local AdventureCraft development settings; do not commit") }
        logger.lifecycle("Installed NVIDIA SHARC ${pin.version} at ${sdk} and configured ${settings.name}.")
        logger.lifecycle("Subsequent ./gradlew build and ./gradlew runClient invocations include SHARC automatically.")
    }

    static String sha256(File source) {
        MessageDigest.getInstance("SHA-256").digest(source.bytes).encodeHex().toString()
    }
}
