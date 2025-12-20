package top.kagg886.filepacker.util

import kotlinx.cinterop.*
import okio.Path.Companion.toPath
import platform.windows.GetModuleFileNameW
import platform.windows.MAX_PATH

@OptIn(ExperimentalForeignApi::class)
actual fun current(): okio.Path = memScoped {
    val buffer = allocArray<UShortVar>(MAX_PATH)
    GetModuleFileNameW(null, buffer, MAX_PATH.convert())
    return buffer.toKStringFromUtf16().toPath()
}
