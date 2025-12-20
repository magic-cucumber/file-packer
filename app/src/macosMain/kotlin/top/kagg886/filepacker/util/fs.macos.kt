package top.kagg886.filepacker.util

import current._NSGetExecutablePath
import kotlinx.cinterop.*
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

@OptIn(ExperimentalForeignApi::class)
actual fun current(): Path = memScoped {
    val bufSize = alloc<UIntVar>()
    bufSize.value = 0u // 先传 0
    _NSGetExecutablePath(null, bufSize.ptr)

    val path = allocArray<ByteVar>(bufSize.value.toInt())
    val errno = _NSGetExecutablePath(path, bufSize.ptr)

    if (errno != 0) {
        throw IOException("")
    }

    path.toKString().toPath().absolute()
}
