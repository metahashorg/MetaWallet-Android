package org.metahash.metawallet.data.models

data class ResponseError(
        val code: String,
        val msg: String
) : Throwable() {
    companion object {
        const val CODE_NETWORK = "network"
    }

    fun isNetworkError() = code.equals(CODE_NETWORK, true)
}