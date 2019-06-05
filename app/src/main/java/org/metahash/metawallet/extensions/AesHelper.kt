package org.metahash.metawallet.extensions

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object AesHelper {

    const val DEFAULT_KEY_LENGTH = 16
    const val DEFAULT_IV_LENGTH = 16

    /**
     * AES Шифрование
     *
     * @param password строковый пароль
     * @param dataToEncrypt строка для шифрования
     */
    fun encrypt(
            password: String,
            dataToEncrypt: String
    ): EncryptCipherData? {
        try {
            val inputBytes = dataToEncrypt.toByteArray(Charsets.UTF_8)
            val keyAndIV = createKeyAndIVFromPassword(
                    DEFAULT_KEY_LENGTH,
                    DEFAULT_IV_LENGTH,
                    password.toByteArray(Charsets.UTF_8)
            )
            val key = createAesKeyFromBytes(keyAndIV[0])
            val iv = getIvParamSpecFromBytes(keyAndIV[1])

            val cipher = getAesCipher()
            cipher.init(Cipher.ENCRYPT_MODE, key, iv)
            val encrypted = cipher.doFinal(inputBytes)

            return EncryptCipherData(encrypted, iv.iv)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     * AES Расшифровка
     *
     * @param password строковый пароль
     * @param inputBytes байты для расшифровки
     * @param ivParamsBytes параметр IV, которй использовался для шифрования
     */
    fun decrypt(
            password: String,
            inputBytes: ByteArray,
            ivParamsBytes: ByteArray
    ): DecryptCipherData? {
        try {
            val keyAndIV = createKeyAndIVFromPassword(
                    DEFAULT_KEY_LENGTH,
                    DEFAULT_IV_LENGTH,
                    password.toByteArray(Charsets.UTF_8)
            )
            val key = createAesKeyFromBytes(keyAndIV[0])
            val iv = getIvParamSpecFromBytes(ivParamsBytes)

            val cipher = getAesCipher()
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            val decrypted = String(cipher.doFinal(inputBytes))

            return DecryptCipherData(decrypted)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    private fun getAesCipher(): Cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "SC")

    private fun getIvParamSpecFromBytes(ivBytes: ByteArray): IvParameterSpec {
        return IvParameterSpec(ivBytes)
    }

    private fun getIvParamSpec(): IvParameterSpec {
        val secureRandom = SecureRandom()
        val ivBytes = ByteArray(16)
        secureRandom.nextBytes(ivBytes)
        return IvParameterSpec(ivBytes)
    }

    private fun createAesKey(password: String): SecretKeySpec {
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = PBEKeySpec(password.toCharArray(), byteArrayOf(0), 65536, 128)
        val pbeSecretKey = secretKeyFactory.generateSecret(keySpec)
        return SecretKeySpec(pbeSecretKey.encoded, "AES")
    }

    private fun createAesKeyFromBytes(keyBytes: ByteArray): SecretKeySpec {
        return SecretKeySpec(keyBytes, "AES")
    }

    fun createKeyAndIVFromPassword(
            keySize: Int,
            ivSize: Int,
            data: ByteArray?,
            salt: ByteArray? = null,
            count: Int = 500): Array<ByteArray> {

        val md = MessageDigest.getInstance("MD5")

        val both = arrayOf(ByteArray(keySize), ByteArray(ivSize))
        val key = ByteArray(keySize)
        var key_ix = 0
        val iv = ByteArray(ivSize)
        var iv_ix = 0
        both[0] = key
        both[1] = iv
        var md_buf: ByteArray? = null
        var nkey = keySize
        var niv = ivSize
        var i = 0
        if (data == null) {
            return both
        }
        var addmd = 0
        while (true) {
            md.reset()
            if (addmd++ > 0) {
                md.update(md_buf)
            }
            md.update(data)
            if (null != salt) {
                md.update(salt, 0, 8)
            }
            md_buf = md.digest()
            i = 1
            while (i < count) {
                md.reset()
                md.update(md_buf)
                md_buf = md.digest()
                i++
            }
            i = 0
            if (nkey > 0) {
                while (true) {
                    if (nkey == 0)
                        break
                    if (i == md_buf!!.size)
                        break
                    key[key_ix++] = md_buf[i]
                    nkey--
                    i++
                }
            }
            if (niv > 0 && i != md_buf!!.size) {
                while (true) {
                    if (niv == 0)
                        break
                    if (i == md_buf.size)
                        break
                    iv[iv_ix++] = md_buf[i]
                    niv--
                    i++
                }
            }
            if (nkey == 0 && niv == 0) {
                break
            }
        }
        i = 0
        while (i < md_buf!!.size) {
            md_buf[i] = 0
            i++
        }
        return both
    }
}

data class EncryptCipherData(
        val outputData: ByteArray,
        val ivParams: ByteArray
)

data class DecryptCipherData(
        val outputString: String
)