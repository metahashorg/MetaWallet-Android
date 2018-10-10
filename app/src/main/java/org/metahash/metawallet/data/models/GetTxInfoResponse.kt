package org.metahash.metawallet.data.models

data class GetTxInfoResponse(
    val error: TxError?,
    val result: TxResult?,
    val status: TXSTATUS = TXSTATUS.WAIT
) {

    fun isSuccessful() = result != null && error == null

    constructor(status: TXSTATUS) : this(null, null, status)

    companion object {

        fun error() = GetTxInfoResponse(TXSTATUS.ERROR)

        fun wait() = GetTxInfoResponse(TXSTATUS.WAIT)
    }
}

data class TxError(
        val code: Int,
        val message: String
)

data class TxResult(
        val transaction: TxInfo
)

data class TxInfo(
        val from: String,
        val to: String,
        val value: Double,
        val data: String,
        val timestamp: Long,
        val type: String,
        val signature: String
)