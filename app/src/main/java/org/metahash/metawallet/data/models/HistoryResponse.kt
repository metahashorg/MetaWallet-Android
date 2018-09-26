package org.metahash.metawallet.data.models

import java.io.Serializable

data class HistoryResponse(
        val result: List<HistoryData>

) : BaseDecenterResponse(), Serializable

data class HistoryData(
        val from: String,
        val to: String,
        val transaction: String,
        val value: Float,
        val timestamp: Long,
        var currency: String) : Serializable {

    constructor() : this("", "", "", 0F, 0L, "")
}
