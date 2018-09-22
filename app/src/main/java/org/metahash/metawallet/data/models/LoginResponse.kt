package org.metahash.metawallet.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
        val data: LoginData

) : BaseResponse()

data class LoginData(
        val token: String,
        @SerializedName("refresh_token") val refreshToken: String,
        @SerializedName("is_mhc_allowed") val isMhc: Boolean
)
