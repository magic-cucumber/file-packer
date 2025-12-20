import com.github.ajalt.clikt.command.test
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import top.kagg886.filepacker.command.EncryptCommand
import top.kagg886.filepacker.util.absolute
import top.kagg886.filepacker.util.delete
import top.kagg886.filepacker.util.exists
import kotlin.test.Test

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 14:57
 * ================================================
 */
class EncryptTest {
    @Test
    fun testHelp() = runTest {
        println(EncryptCommand().test("--help").output)
    }
    @Test
    fun testEncrypt() = runTest {
        val path = "test"
        "test-encrypt".toPath().run {
            if (exists()) delete()
        }
        val tester = EncryptCommand().test("--debug test")
        println(tester.output)
    }

    @Test
    fun testEncryptHelper() = runTest {
        val path = "test"
        "test-encrypt".toPath().run {
            if (exists()) delete()
        }
        val tester = EncryptCommand().test("--debug --helper test")
        println(tester.output)
    }
}
