import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import top.kagg886.filepacker.command.DecryptCommand
import top.kagg886.filepacker.command.EncryptCommand

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 13:29
 * ================================================
 */
fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    Main().subcommands(EncryptCommand(), DecryptCommand()).main(args)
}

class Main : SuspendingCliktCommand(name = "file-packer") {
    override fun help(context: Context): String = """
        a tool to pack and unpack files.
        
        some example command:
        
        - encrypt: ./file-packer encrypt test
        
        - decrypt: ./file-packer decrypt test-encrypt --output test-decrypt
    """.trimIndent()
    override suspend fun run() = Unit
}
