package org.metahash.metawallet.extensions

import org.metahash.metawallet.data.models.Wallet

object PrivateWalletHelper {

    var privateKeyInfo: String = ""

    fun createWalletFromPrivateKey(password: String): Wallet? {
        val decryptedPrivateKey =
                prepareKey(
                        decryptPrivateKey(privateKeyInfo.toByteArray(Charsets.US_ASCII), password) ?: ""
                )
        if (decryptedPrivateKey.isEmpty()) {
            return null
        }
        return CryptoExt.createWalletFromPrivateKey(
                decryptedPrivateKey.toUpperCase().hexStringToByteArray()
        )
    }

    fun encryptWalletPrivateKey(
            privateKey: ByteArray,
            password: String
    ): String {
        return encryptPrivateKey(privateKey, password) ?: ""
    }

    private fun prepareKey(key: String): String {
        return key.replace(":", "")
    }

    private external fun decryptPrivateKey(
            pemInfo: ByteArray,
            password: String
    ): String?

    private external fun encryptPrivateKey(
            privateKey: ByteArray,
            password: String
    ): String?
}