package top.kagg886.filepacker.util

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.operations.Cipher
import kotlin.random.Random

import dev.whyoleg.cryptography.algorithms.AES as InternalAES

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/12/19 23:01
 * ================================================
 */

object AES {
    const val SALT_SIZE = 8
    val cipher by lazy {
        CryptographyProvider.Default.get(InternalAES.CBC)
    }
}

/**
 * 使用rsa进行加密
 * @param data 明文
 * @param keychain 密钥
 * @return 返回值，可以解构。第一个为salt,第二个为iv，第三个为密文
 */
@OptIn(DelicateCryptographyApi::class)
internal fun AES.encrypt(data: String, keychain: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
    val salt = Random.nextBytes(SALT_SIZE)
    val iv = Random.nextBytes(16)


    val n = salt + data.encodeToByteArray()
    val secretKey =
        cipher.keyDecoder().decodeFromByteArrayBlocking(InternalAES.Key.Format.RAW, keychain)

    val result = secretKey.cipher(padding = true)
        .encryptWithIvBlocking(
            iv = iv,
            plaintext = n
        )

    return Triple(salt, iv, result)
}

/**
 * 使用rsa进行解密
 * @param data 密文
 * @param salt salt值
 * @param iv iv值
 * @param keychain 密钥
 * @return 返回值，可以解构。第一个为salt,第二个为iv，第三个为密文
 */
@OptIn(DelicateCryptographyApi::class)
internal fun AES.decrypt(data: ByteArray, keychain: ByteArray, salt: ByteArray, iv: ByteArray): String {
    val secretKey =
        cipher.keyDecoder().decodeFromByteArrayBlocking(InternalAES.Key.Format.RAW, keychain)

    val result = secretKey.cipher(padding = true)
        .decryptWithIvBlocking(
            iv = iv,
            ciphertext = data,
        )

    return result.drop(SALT_SIZE).toByteArray().decodeToString()
}


internal val chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678".toCharArray()
internal fun random(length: Int) = (1..length).map { chars.random() }.joinToString("")
