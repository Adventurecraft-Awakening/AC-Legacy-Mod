import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.internal.Pair
import org.gradle.process.ProcessExecutionException

import javax.inject.Inject
import java.util.regex.Pattern

abstract class GitService implements BuildService<Parameters> {

    interface Parameters extends BuildServiceParameters {
        DirectoryProperty getRootProjectDir();

        DirectoryProperty getRootProjectBuildDir();
    }

    static final TAG_PATTERN = Pattern.compile("(.*)-([0-9]+)-g.?[0-9a-fA-F]{3,}")
    static final PLAIN_TAG_PATTERN = Pattern.compile(".*g.?[0-9a-fA-F]{3,}")

    Provider<String> describe = git("describe", "--tags", "--always", "--first-parent", "--match=*", "HEAD")
    Provider<String> commitId = git("rev-parse", "HEAD")
    Provider<String> branch = git("rev-parse", "--abbrev-ref", "HEAD")
    Provider<String> originUrl = git("config", "remote.origin.url")
    private Provider<String> status = git("status", "--porcelain")

    Provider<Boolean> isDescribePlainTag = describe.map(it -> matchesPlainTag(it))

    Provider<Boolean> isCleanTag = status.zip(isDescribePlainTag) { status, isPlain ->
        status.isEmpty() && isPlain
    }

    Provider<String> version = describe.zip(isCleanTag) { desc, isClean ->
        if (desc == null) {
            return null
        }
        return desc + (isClean ? "" : ".dirty")
    }

    Provider<Long> commitDistance = describe.map {
        if (matchesPlainTag(it)) {
            return 0L
        }
        def m = TAG_PATTERN.matcher(it)
        return m.matches() ? Long.parseLong(m.group(2)) : null
    }

    Provider<String> lastTag = describe.map {
        if (matchesPlainTag(it)) {
            return it
        }
        def m = TAG_PATTERN.matcher(it)
        return m.matches() ? m.group(1) : null
    }

    @Inject
    abstract ProviderFactory getProviders();

    private Provider<String> git(String... args) {
        var argList = new ArrayList()
        argList.addAll(args)
        argList.add(0, "git")

        var projectDir = parameters.rootProjectDir.asFile.get()
        var output = providers.exec {
            it.workingDir = projectDir
            it.ignoreExitValue = true
            it.commandLine = argList
        }
        return output.standardOutput.asText
            .zip(output.standardError.asText) { a, b -> Pair.of(a, b) }
            .zip(output.result) { text, result ->
                try {
                    result.assertNormalExitValue()
                    text.left().strip()
                } catch (ProcessExecutionException ex) {
                    String message = String.format(
                        "Process '%s' finished with non-zero exit value %d:\nError: %s\nOutput: %s",
                        String.join(" ", argList), result.exitValue, text.right(), text.left())
                    throw new ProcessExecutionException(message, ex)
                }
            }
    }

    static final boolean matchesPlainTag(String value) {
        return !PLAIN_TAG_PATTERN.matcher(value).matches()
    }
}