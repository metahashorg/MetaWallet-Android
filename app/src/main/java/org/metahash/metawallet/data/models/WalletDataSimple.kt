package org.metahash.metawallet.data.models

import java.io.Serializable

data class WalletDataSimple(
        val address: String,
        val name: String,
        val balance: String,
        val hasPrivateKey: Boolean
) : Serializable