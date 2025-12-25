import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import platform.posix.LC_ALL
import platform.posix.setlocale
import platform.windows.CommandLineToArgvW
import platform.windows.GetCommandLineW

@OptIn(ExperimentalForeignApi::class)
actual fun main(args: Array<String>) {
    setlocale(LC_ALL, ".UTF8")
    val cmdLineW = GetCommandLineW()
    val cmdLineKString = cmdLineW?.toKStringFromUtf16() ?: ""

    val args = memScoped {
        val size = alloc<IntVar>()
        val argv = CommandLineToArgvW(cmdLineKString, size.ptr)

        List(size.value) {
            argv?.get(it)?.toKStringFromUtf16() ?: ""
        }
    }
    execute(args.drop(1).toTypedArray())
}
