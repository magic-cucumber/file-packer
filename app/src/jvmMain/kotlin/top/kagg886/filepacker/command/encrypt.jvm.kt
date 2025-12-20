package top.kagg886.filepacker.command

import okio.Path
import okio.buffer
import top.kagg886.filepacker.util.create
import top.kagg886.filepacker.util.current
import top.kagg886.filepacker.util.moveTo
import top.kagg886.filepacker.util.sink

actual fun Path.writeShell() = when (System.getProperty("os.name").contains("Windows")) {
    true -> {
        current().moveTo(this / "extract.jar")

        val path = this / "unpack.bat"
        path.create()
        path.sink().buffer().use { o ->
            o.writeString(
                """
                    @echo off
                    java -version >nul 2>&1
                    if %ERRORLEVEL% NEQ 0 (
                        echo [Error] can't find java path.
                        pause
                        exit /b
                    )
                    
                    if not exist "extract.jar" (
                        echo [Error] extract.kexe not found.
                        pause
                        exit /b
                    )
                    java -jar extract.jar decrypt "../" --output="${name}-decrypted"
                    echo [Info] unpack success.
                """.trimIndent(), Charsets.UTF_8
            )
            o.flush()
        }
    }

    false -> {
        current().moveTo(this / "extract.jar")
        val path = this / "unpack.sh"
        path.create()
        path.sink().buffer().use { o ->
            o.writeString(
                """
                    #!/bin/bash
                    if ! command -v java &> /dev/null; then
                        echo "[Error] can't find java path. Please install Java first."
                        exit 1
                    fi
                    if [ ! -f "extract.jar" ]; then
                        echo "[Error] extract.jar not found."
                        exit 1
                    fi
                    java -jar extract.jar decrypt "../${name}" --output="${name}-decrypted"
                    echo "[Info] unpack success."
                """.trimIndent(), Charsets.UTF_8
            )
            o.flush()
        }
    }
}
