package org.metahash.metawallet.data.models

import com.google.gson.annotations.SerializedName

data class RefreshResponse(
        val data: RefreshData

) : BaseResponse()

data class RefreshData(
        @SerializedName("access") val token: String,
        @SerializedName("refresh") val refreshToken: String
)
