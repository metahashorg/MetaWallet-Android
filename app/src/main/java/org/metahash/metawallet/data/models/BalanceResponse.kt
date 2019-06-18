package org.metahash.metawallet.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BalanceResponse(
    val result: BalanceData
) : BaseDecenterResponse(), Serializable

data class BalanceData(
    val address: String,
    val received: Float,
    val spent: Float,
    @SerializedName("count_received") val countReceived: Int,
    @SerializedName("count_spent") val countSpent: Int,
    @SerializedName("block_number") val blockNumber: Int,
    val currentBlock: Int,
    val delegate: Double?,
    val undelegate: Double?
) : Serializable {

    val balance: Float
        get() = received - spent

    val delegatedBy: Double
        get() = ((delegate ?: 0.0) - (undelegate ?: 0.0))/1000

    constructor() : this(
        "", 0F, 0F,
        0, 0, 0,
        0, 0.0, 0.0
    )
}
