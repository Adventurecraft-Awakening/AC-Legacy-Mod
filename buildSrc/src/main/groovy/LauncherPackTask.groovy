import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.Attributes
import groovy.xml.slurpersupport.NodeChild
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJSON

import javax.inject.Inject

abstract class LauncherPackTask extends DefaultTask {

    @InputDirectory
    abstract DirectoryProperty getRootDirectory();

    @InputFiles
    abstract ConfigurableFileCollection getTemplateFiles();

    @Inject
    abstract ProviderFactory getProviders();

    @TaskAction
    void pack() {
        def parser = new XmlSlurper()
        try (def cx = Context.enter()) {
            for (def templateFile : templateFiles.files) {
                def scope = cx.initStandardObjects()

                def template = parser.parse(templateFile)
                for (def script in template["head"]["script"]) {
                    if (script instanceof NodeChild) {
                        def src = script["@src"] as Attributes
                        if (src.isEmpty()) {
                            def srcText = script.text()
                            cx.evaluateString(scope, srcText, templateFile.toString(), 0, null)
                        } else {
                            def srcFile = rootDirectory.file(src.text()).get()
                            def srcText = providers.fileContents(srcFile).asText.get()
                            cx.evaluateString(scope, srcText, srcFile.toString(), 1, null)
                        }
                    }
                }

                def json = NativeJSON.stringify(cx, scope, scope.get("PACK", null), null, 2)
                println "${templateFile} PACK=${json}"
            }
        }
    }
}
