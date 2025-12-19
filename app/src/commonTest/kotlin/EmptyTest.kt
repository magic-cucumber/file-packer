import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.command.test
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import kotlinx.coroutines.test.runTest
import top.kagg886.filepacker.command.DecryptCommand
import top.kagg886.filepacker.command.EncryptCommand
import kotlin.test.Test

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 22:42
 * ================================================
 */


class EmptyTest {
    @Test
    fun test() = runTest {
        Main().subcommands(EncryptCommand(), DecryptCommand()).test("").output.apply {
            println(this)
        }
    }
}
