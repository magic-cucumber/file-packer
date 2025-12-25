package top.kagg886.filepacker.command

import okio.Path
import okio.buffer
import okio.use
import top.kagg886.filepacker.util.copyTo
import top.kagg886.filepacker.util.create
import top.kagg886.filepacker.util.current
import top.kagg886.filepacker.util.sink

actual fun Path.writeShell() {
    current().copyTo(this / "extract.exe")
    val path = this / "unpack.bat"
    path.create()
    path.sink().buffer().use { o ->
        o.writeUtf8(
            """
                    @echo off
                    set "BIN_DIR=%~dp0"
                    if not exist "%BIN_DIR%extract.exe" (
                        echo [Error] Cannot find extract.exe
                        pause
                        exit /b
                    )
                    "%BIN_DIR%extract.exe decrypt "../${name}" --output="${name}-decrypted"
                    echo [Info] Task finished.
                """.trimIndent()
        )
        o.flush()
    }
}
