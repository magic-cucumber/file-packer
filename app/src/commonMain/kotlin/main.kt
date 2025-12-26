import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import top.kagg886.filepacker.BuildConfig
import top.kagg886.filepacker.command.DecryptCommand
import top.kagg886.filepacker.command.EncryptCommand

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 13:29
 * ================================================
 */
expect fun main(args: Array<String>)

fun execute(args: Array<String>) = runBlocking(Dispatchers.IO) {
    Main().subcommands(EncryptCommand(), DecryptCommand()).main(args)
}

class Main : SuspendingCliktCommand(name = "file-packer") {
    override fun help(context: Context): String = """
        File Packer ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}), created by kagg886.
        
        a tool to pack and unpack files.
        
        here are some example command:
        
        - encrypt: ./file-packer encrypt test
        
        - decrypt: ./file-packer decrypt test-encrypt --output test-decrypt
        
        Github link: https://github/magic-cucumber/file-packer
    """.trimIndent()

    override suspend fun run() = Unit
}
