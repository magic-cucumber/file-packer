package top.kagg886.filepacker.command

import okio.Path
import okio.buffer
import okio.use
import top.kagg886.filepacker.util.copyTo
import top.kagg886.filepacker.util.create
import top.kagg886.filepacker.util.current
import top.kagg886.filepacker.util.sink

actual fun Path.writeShell() {
    current().copyTo(this / "extract.kexe")
    val path = this / "unpack.sh"
    path.create()
    path.sink().buffer().use { o ->
        o.writeUtf8(
            $$"""
                        #!/bin/bash

                        SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
                        EXE_PATH="$SCRIPT_DIR/extract.kexe"
                        
                        if [ ! -f "$EXE_PATH" ]; then
                            echo "[Error] extract.kexe not found at: $EXE_PATH"
                            exit 1
                        fi
                        
                        chmod +x "$EXE_PATH"
                        
                        "$EXE_PATH" decrypt "$SCRIPT_DIR" --output="$$name-decrypted"
                        
                        echo "[Info] unpack success."
                    """.trimIndent()
        )
        o.flush()
    }
}
