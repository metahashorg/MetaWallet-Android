package org.metahash.metawallet.extensions

import org.metahash.metawallet.data.models.Wallet

object PrivateWalletHelper {

    var privateKeyInfo: String = ""

    fun createWalletFromPrivateKey(password: String): Wallet? {
        val decryptedPrivateKey =
                prepareKey(
                        decryptPrivateKey(privateKeyInfo.toByteArray(Charsets.US_ASCII), password)
                )
        if (decryptedPrivateKey.isEmpty()) {
            return null
        }
        val wallet = CryptoExt.createWalletFromPrivateKey(
                decryptedPrivateKey.toUpperCase().hexStringToByteArray()
        )
        return wallet
    }

    private fun prepareKey(key: String): String {
        return key.replace(":", "")
    }

    private external fun decryptPrivateKey(
            pemInfo: ByteArray,
            password: String
    ): String
}