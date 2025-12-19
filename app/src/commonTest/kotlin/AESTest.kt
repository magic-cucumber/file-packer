import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.modules.SerializersModule
import okio.Path
import okio.Path.Companion.toPath
import top.kagg886.filepacker.util.AES
import top.kagg886.filepacker.util.PathSerializer
import top.kagg886.filepacker.util.decrypt
import top.kagg886.filepacker.util.encrypt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 23:14
 * ================================================
 */

class AESTest {
    @Test
    fun testAES() {
        val payload = "Hello,World!!!"
        val keychain = Random.nextBytes(16)
        val (salt, iv, data) = AES.encrypt(data = payload, keychain = keychain)

        assertEquals(payload, AES.decrypt(data, keychain, salt, iv))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testAESPath() {
        val path = "/Users/886kagg/IdeaProjects/file-packer/app/src/commonMain".toPath()

        val cbor = Cbor {
            serializersModule = SerializersModule {
                contextual(Path::class, PathSerializer)
            }
        }

        val data = cbor.encodeToHexString(path)
        println(data)
        val decode = cbor.decodeFromHexString<Path>(data)

        assertEquals(path, decode)
    }
}
