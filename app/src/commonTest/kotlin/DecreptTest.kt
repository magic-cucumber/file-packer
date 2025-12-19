import com.github.ajalt.clikt.command.test
import kotlinx.coroutines.test.runTest
import okio.Buffer
import okio.HashingSink
import okio.HashingSource
import okio.Path
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer
import okio.use
import top.kagg886.filepacker.command.DecryptCommand
import top.kagg886.filepacker.command.EncryptCommand
import top.kagg886.filepacker.util.delete
import top.kagg886.filepacker.util.exists
import top.kagg886.filepacker.util.size
import top.kagg886.filepacker.util.source
import top.kagg886.filepacker.util.walk
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 21:39
 * ================================================
 */
class DecryptTest {
    @Test
    fun testHelp() = runTest {
        println(DecryptCommand().test("--help").output)
    }
    @Test
    fun testDecrypt() = runTest {
        "test-decrypt".toPath().run {
            if (exists()) delete()
        }
        val tester = DecryptCommand().test("--debug test-encrypt --output test-decrypt")
        println(tester.output)

        "test-decrypt".toPath().walk {
            val md5New = it.md5()
            val md5Old = ("test".toPath() / it.relativeTo("test-decrypt".toPath())).md5()

            assertEquals(md5New, md5Old, "File $it is not equal: $it's md5 is $md5New,${("test".toPath() / it)}'s md5 is $md5Old")
        }
    }
}

private fun Path.md5(): String = HashingSink.md5(blackholeSink()).apply {
    val buffer = Buffer()

    source().use {
        var len: Long
        while (it.read(buffer, 2048).also { len = it } > 0) {
            write(buffer, len)
        }
    }

}.hash.hex()
