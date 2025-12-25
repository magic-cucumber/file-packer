package top.kagg886.filepacker.command

import okio.Path
import okio.buffer
import top.kagg886.filepacker.util.copyTo
import top.kagg886.filepacker.util.create
import top.kagg886.filepacker.util.current
import top.kagg886.filepacker.util.sink

actual fun Path.writeShell() = when (System.getProperty("os.name").contains("Windows")) {
    true -> {
        current().copyTo(this / "extract.jar")

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
                    set "JAR_PATH=%~dp0extract.jar"
                    if not exist "%JAR_PATH%" (
                        echo [Error] extract.jar not found at: %JAR_PATH%
                        pause
                        exit /b
                    )
                    java -jar "%JAR_PATH%" decrypt "../${name}" --output="${name}-decrypted"
                    echo [Info] Task finished.
                """.trimIndent(), Charsets.UTF_8
            )
            o.flush()
        }
    }

    false -> {
        current().copyTo(this / "extract.jar")
        val path = this / "unpack.sh"
        path.create()
        path.sink().buffer().use { o ->
            o.writeUtf8(
                $$"""
                    #!/bin/bash
                    if ! command -v java &> /dev/null; then
                        echo "[Error] can't find java path. Please install Java first."
                        exit 1
                    fi
                    
                    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
                    JAR_PATH="$SCRIPT_DIR/extract.jar"
                    
                    if [ ! -f "$JAR_PATH" ]; then
                        echo "[Error] extract.jar not found at: $JAR_PATH"
                        exit 1
                    fi
                    
                    java -jar "$JAR_PATH" decrypt "${SCRIPT_DIR}" --output="$${name}-decrypted"
                    
                    echo "[Info] unpack success."
                    echo "[Current Root] $(pwd)"
                """.trimIndent()
            )
            o.flush()
        }
    }
}
