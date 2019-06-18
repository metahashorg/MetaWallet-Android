package org.metahash.metawallet.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WalletsResponse(
    val data: List<WalletsData>
) : BaseResponse(), Serializable

data class WalletsData(
    val address: String,
    val currency: String,
    val password: String,
    @SerializedName("ext_address") val extAddress: String,
    @SerializedName("currency_code") val currencyCode: String,
    @SerializedName("public_key") val publicKey: String,
    var balance: BalanceData = BalanceData(),
    val hasPrivateKey: Boolean = false,
    var name: String?,
    var userLogin: String
) : Serializable
