package util

enum class Platform {
    WINDOWS,MACOS,LINUX
}

val platform by lazy {
    val prop = System.getProperty("os.name")
    when {
        prop.startsWith("Mac") -> Platform.MACOS
        prop.startsWith("Linux") -> Platform.LINUX
        prop.startsWith("Win") -> Platform.WINDOWS
        else -> error("unsupported platform: $prop")
    }
}

val Platform.isWindows get() = this == Platform.WINDOWS
val Platform.isLinux get() = this == Platform.LINUX
val Platform.isMacOS get() = this == Platform.MACOS
