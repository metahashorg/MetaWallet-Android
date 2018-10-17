package org.metahash.metawallet.data.models

import java.io.Serializable
import java.util.*

data class Wallet(
        val address: String,
        val publicKey: ByteArray,
        val privateKeyPKCS1: ByteArray,
        val privateKey: WalletPrivateKey) : Serializable {
    var currency = ""
    var code = ""
    var name = ""
    var isSynchronized = false
    var userLogin = ""
}

data class WalletPrivateKey(
        val algorithm: String,
        val format: String,
        val encoded: ByteArray
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletPrivateKey

        if (algorithm != other.algorithm) return false
        if (format != other.format) return false
        if (!Arrays.equals(encoded, other.encoded)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = algorithm.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + Arrays.hashCode(encoded)
        return result
    }
}