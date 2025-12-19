package top.kagg886.filepacker.util

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import okio.Buffer
import okio.Path
import okio.Path.Companion.toPath
import top.kagg886.filepacker.util.AES.SALT_SIZE
import kotlin.random.Random

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 21:00
 * ================================================
 */


object PathSerializer : KSerializer<Path> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Path) {
        val keychain = Random.nextBytes(16)
        val (salt, iv, data) = AES.encrypt(data = value.toString(), keychain = keychain)

        val buf = Buffer()

        buf.write(keychain)
        buf.write(salt)
        buf.write(iv)
        buf.write(data)

        encoder.encodeString(buf.readByteArray().toHexString())
    }

    override fun deserialize(decoder: Decoder): Path {
        val buf = Buffer().write(decoder.decodeString().hexToByteArray())

        val keychain = buf.readByteArray(16)
        val salt = buf.readByteArray(SALT_SIZE.toLong())
        val iv = buf.readByteArray(16)
        val data = buf.readByteArray()
        return AES.decrypt(data = data, keychain = keychain, salt = salt, iv = iv).toPath()
    }
}
