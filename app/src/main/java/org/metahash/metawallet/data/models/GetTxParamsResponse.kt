package org.metahash.metawallet.data.models

import com.google.gson.annotations.SerializedName

data class GetTxParamsResponse(
        val data: TxParamsData
) : BaseResponse()

data class TxParamsData(
        val nonce: String,
        @SerializedName("gas_price") val gasPrice: String,
        @SerializedName("gas_limit") val gasLimit: String)