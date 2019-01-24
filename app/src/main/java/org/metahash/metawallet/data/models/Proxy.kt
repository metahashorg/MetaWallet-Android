package org.metahash.metawallet.data.models

import java.io.Serializable

data class Proxy(
        val ip: String,
        val ping: Double,
        val type: TYPE) : Serializable {

    companion object {
        fun getDefault() = Proxy("0.0.0.0", 0.0, TYPE.DEV)
    }

    enum class TYPE {
        DEV,
        PROD
    }
}