package org.metahash.metawallet.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.ServiceRequest

object ServiceRequestFactory {

    //methods
    private const val METHOD_LOGIN = "user.auth"
    private const val METHOD_ALL_WALLETS = "address.list"
    private const val METHOD_REFRESH_TOKEN = "user.token.refresh"
    private const val METHOD_WALLET_BALANCE = "fetch-balance"

    //params
    private const val KEY_CURRENCY = "currency"
    private const val KEY_ADDRESS = "address"
    private const val KEY_LOGIN = "login"
    private const val KEY_PASSWORD = "password"

    fun getRequestData(type: REQUESTTYPE, params: JsonArray?): ServiceRequest {
        return when (type) {
            REQUESTTYPE.LOGIN -> createLoginRequest(params!!)
            REQUESTTYPE.ALLWALLETS -> createWalletsRequest(params!!)
            REQUESTTYPE.WALLETBALANCE -> createBalanceRequest(params!!)
            REQUESTTYPE.REFRESHTOKEN -> createRefreshRequest()
        }
    }

    fun getLoginParams(login: String, password: String): JsonArray {
        return JsonArray().apply {
            add(JsonObject().apply {
                addProperty(KEY_LOGIN, login)
                addProperty(KEY_PASSWORD, password)
                addProperty("ttl", 60)
            })
        }
    }

    fun getAllWalletsParams(currency: String?): JsonArray {
        return if (currency.isNullOrEmpty()) {
            JsonArray()
        } else {
            JsonArray().apply {
                add(JsonObject().apply {
                    addProperty(KEY_CURRENCY, currency)
                })
            }
        }
    }

    fun getBalanceParams(address: String): JsonArray {
        return JsonArray().apply {
            add(JsonObject().apply {
                addProperty(KEY_ADDRESS, address)
            })
        }
    }

    private fun createLoginRequest(params: JsonArray) = ServiceRequest(method = METHOD_LOGIN, params = params)

    private fun createWalletsRequest(params: JsonArray) = ServiceRequest(
            method = METHOD_ALL_WALLETS,
            params = params,
            token = WalletApplication.dbHelper.getToken())

    private fun createBalanceRequest(params: JsonArray) = ServiceRequest(
            method = METHOD_WALLET_BALANCE,
            params = params)

    private fun createRefreshRequest() = ServiceRequest(
            method = METHOD_REFRESH_TOKEN,
            token = WalletApplication.dbHelper.getRefreshToken())

    enum class REQUESTTYPE {
        LOGIN,
        ALLWALLETS,
        WALLETBALANCE,
        REFRESHTOKEN
    }
}