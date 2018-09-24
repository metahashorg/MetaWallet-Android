package org.metahash.metawallet.data.models

import java.io.Serializable
import java.util.*

data class WalletHistoryRaw(
        val address: String,
        val raw: String
): Serializable {
    override fun equals(other: Any?): Boolean {
        return this.address.equals((other as WalletHistoryRaw?)?.address ?: "", true)
    }
}