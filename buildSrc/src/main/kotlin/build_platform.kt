import org.gradle.api.Project
import util.props

val Project.build_platform
    get() = props("build", "platform")?.uppercase()?.let { BuildPlatform.valueOf(it) } ?: error("build platform is not set or illegal")

enum class BuildPlatform {
    ALL,PLATFORM
}


val BuildPlatform.isAll get() = this == BuildPlatform.ALL
val BuildPlatform.isPlatform get() = this == BuildPlatform.PLATFORM
