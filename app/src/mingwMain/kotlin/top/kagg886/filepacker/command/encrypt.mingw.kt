package top.kagg886.filepacker.command

import okio.Path
import okio.buffer
import okio.use
import top.kagg886.filepacker.util.create
import top.kagg886.filepacker.util.current
import top.kagg886.filepacker.util.moveTo
import top.kagg886.filepacker.util.sink

actual fun Path.writeShell() {
    current().moveTo(this / "extract.kexe")
    val path = this / "unpack.sh"
    path.create()
    path.sink().buffer().use { o ->
        o.writeUtf8(
            """
                    @echo off
                    if not exist "extract.kexe" (
                        echo [Error] extract.kexe not found.
                        pause
                        exit /b
                    )
                    extract.kexe decrypt "../${name}" --output="${name}-decrypted"
                    echo [Info] unpack success.
                """.trimIndent()
        )
        o.flush()
    }
}
