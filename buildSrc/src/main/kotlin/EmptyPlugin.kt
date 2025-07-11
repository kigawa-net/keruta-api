import org.gradle.api.Plugin
import org.gradle.api.Project

class EmptyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // This is an empty plugin that does nothing
        // It's only here to satisfy Gradle's plugin descriptor requirements
    }
}
