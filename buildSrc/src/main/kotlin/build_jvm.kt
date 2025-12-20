import org.gradle.api.Project
import util.props

val Project.build_jvm
    get() = props("build", "jvm")?.lowercase()?.toBooleanStrictOrNull() ?: error("build jvm is not set or illegal")

