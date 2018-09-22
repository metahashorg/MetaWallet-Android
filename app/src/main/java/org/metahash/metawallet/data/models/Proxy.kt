package org.metahash.metawallet.data.models

import java.io.Serializable

data class Proxy(
        val ip: String,
        val ping: Double) : Serializable {

    companion object {
        fun getDefault() = Proxy("0.0.0.0", 0.0)
    }
}