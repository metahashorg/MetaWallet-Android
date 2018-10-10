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
    private const val METHOD_WALLET_HISTORY = "fetch-history"
    private const val METHOD_CREATE_TX = "mhc_send"
    private const val METHOD_TX_INFO = "get-tx"

    //params
    private const val KEY_CURRENCY = "currency"
    private const val KEY_ADDRESS = "address"
    private const val KEY_LOGIN = "login"
    private const val KEY_PASSWORD = "password"
    private const val KEY_TO = "to"
    private const val KEY_VALUE = "value"
    private const val KEY_FEE = "fee"
    private const val KEY_NONCE = "nonce"
    private const val KEY_DATA = "data"
    private const val KEY_PUBKEY = "pubkey"
    private const val KEY_SIGN = "sign"
    private const val KEY_HASH = "hash"

    fun getRequestData(type: REQUESTTYPE, params: Any?): ServiceRequest {
        return when (type) {
            REQUESTTYPE.LOGIN -> createLoginRequest(params!!)
            REQUESTTYPE.ALLWALLETS -> createWalletsRequest(params!!)
            REQUESTTYPE.WALLETBALANCE -> createBalanceRequest(params!!)
            REQUESTTYPE.REFRESHTOKEN -> createRefreshRequest()
            REQUESTTYPE.WALLETHISTORY -> createHistoryRequest(params!!)
            REQUESTTYPE.MAKETRANSACTION -> createTransactionRequest(params!!)
            REQUESTTYPE.TXINFO -> createTxInfoRequest(params!!)
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

    fun getAllWalletsParams(currency: String?): JsonObject {
        return if (currency.isNullOrEmpty()) {
            JsonObject()
        } else {
            JsonObject().apply {
                addProperty(KEY_CURRENCY, currency)
            }
        }
    }

    fun getBalanceParams(address: String): JsonObject {
        return JsonObject().apply {
            addProperty(KEY_ADDRESS, address)
        }
    }

    fun getTransactionParams(to: String, value: String,
                             fee: String, nonce: String,
                             data: String, pubKey: String,
                             sign: String): JsonObject {
        return JsonObject().apply {
            addProperty(KEY_TO, to)
            addProperty(KEY_VALUE, value)
            addProperty(KEY_FEE, fee)
            addProperty(KEY_NONCE, nonce)
            addProperty(KEY_DATA, data)
            addProperty(KEY_PUBKEY, pubKey)
            addProperty(KEY_SIGN, sign)
        }
    }

    fun getTxInfoParams(hash: String): JsonObject {
        return JsonObject().apply {
            addProperty(KEY_HASH, hash)
        }
    }

    fun getHistoryParams(address: String): JsonObject = getBalanceParams(address)

    private fun createLoginRequest(params: Any) = ServiceRequest(method = METHOD_LOGIN, params = params)

    private fun createWalletsRequest(params: Any) = ServiceRequest(
            method = METHOD_ALL_WALLETS,
            params = params,
            token = WalletApplication.dbHelper.getToken())

    private fun createBalanceRequest(params: Any) = ServiceRequest(
            method = METHOD_WALLET_BALANCE,
            params = params)

    private fun createHistoryRequest(params: Any) = ServiceRequest(
            method = METHOD_WALLET_HISTORY,
            params = params
    )

    private fun createRefreshRequest() = ServiceRequest(
            method = METHOD_REFRESH_TOKEN,
            token = WalletApplication.dbHelper.getRefreshToken())

    private fun createTransactionRequest(params: Any) = ServiceRequest(
            method = METHOD_CREATE_TX,
            params = params,
            jsonrpc = "2.0")

    private fun createTxInfoRequest(params: Any) = ServiceRequest(
            method = METHOD_TX_INFO,
            params = params
    )

    enum class REQUESTTYPE {
        LOGIN,
        ALLWALLETS,
        WALLETBALANCE,
        REFRESHTOKEN,
        WALLETHISTORY,
        MAKETRANSACTION,
        TXINFO
    }
}