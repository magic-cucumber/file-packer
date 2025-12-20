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
                    #!/bin/bash
                    if [ ! -f "extract.kexe" ]; then
                        echo "[Error] extract.kexe not found."
                        exit 1
                    fi
                    chmod +x extract.kexe
                    ./extract.kexe decrypt "../${name}" --output="${name}-decrypted"
                    echo "[Info] unpack success."
                """.trimIndent()
        )
        o.flush()
    }
}
