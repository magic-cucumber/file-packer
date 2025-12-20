package top.kagg886.filepacker.command

import okio.Path
import okio.buffer
import okio.use
import top.kagg886.filepacker.util.create
import top.kagg886.filepacker.util.current
import top.kagg886.filepacker.util.moveTo
import top.kagg886.filepacker.util.sink

actual fun Path.writeShell() {
    current().moveTo(this / "extract.exe")
    val path = this / "unpack.bat"
    path.create()
    path.sink().buffer().use { o ->
        o.writeUtf8(
            """
                    @echo off
                    cd /d "%~dp0"
                    if not exist "extract.exe" (
                        echo [Error] extract.exe not found.
                        pause
                        exit /b
                    )
                    extract.exe decrypt "../${name}" --output="${name}-decrypted"
                    echo [Info] unpack success.
                """.trimIndent()
        )
        o.flush()
    }
}
