package org.metahash.metawallet.extensions

import org.metahash.metawallet.WalletApplication

object JsResultHelper {

    fun importPrivateWalletResult(
        address: String,
        status: String
    ): String {
        return WalletApplication.gson.toJson(ImportPrivateWalletResult(address, status))
    }

    fun getPrivateKeyDecryptedResult(
        key: String,
        status: String
    ): String {
        return WalletApplication.gson.toJson(PrivateKeyDecryptedResult(key, status))
    }
}

data class ImportPrivateWalletResult(
    private val address: String,
    private val status: String
)

data class PrivateKeyDecryptedResult(
    private val key: String,
    private val status: String
)