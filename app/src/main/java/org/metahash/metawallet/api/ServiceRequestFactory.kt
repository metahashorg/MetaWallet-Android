package org.metahash.metawallet.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.ServiceRequest

object ServiceRequestFactory {

    //methods
    private const val METHOD_LOGIN = "user.auth"
    private const val METHOD_REGISTER = "user.register"
    private const val METHOD_ALL_WALLETS = "address.list"
    private const val METHOD_REFRESH_TOKEN = "user.token.refresh"
    private const val METHOD_WALLET_BALANCE = "fetch-balance"
    private const val METHOD_WALLET_HISTORY = "fetch-history"
    private const val METHOD_CREATE_TX = "mhc_send"
    private const val METHOD_TX_INFO = "get-tx"
    private const val METHOD_SYNC_WALLET = "address.create"

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
            REQUESTTYPE.REGISTER -> createRegisterRequest(params!!)
            REQUESTTYPE.ALLWALLETS -> createWalletsRequest(params!!)
            REQUESTTYPE.WALLETBALANCE -> createBalanceRequest(params!!)
            REQUESTTYPE.REFRESHTOKEN -> createRefreshRequest()
            REQUESTTYPE.WALLETHISTORY -> createHistoryRequest(params!!)
            REQUESTTYPE.MAKETRANSACTION -> createTransactionRequest(params!!)
            REQUESTTYPE.TXINFO -> createTxInfoRequest(params!!)
            REQUESTTYPE.SYNCWALLET -> createSyncWalletRequest(params!!)
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

    fun getRegisterParams(login: String, password: String): JsonArray {
        return JsonArray().apply {
            add(JsonObject().apply {
                addProperty(KEY_LOGIN, login)
                addProperty(KEY_PASSWORD, password)
            })
        }
    }

    fun getAllWalletsParams(currency: Int?): JsonArray {
        return if (currency == -1 || currency == null) {
            JsonArray()
        } else {
            JsonArray().apply {
                add(JsonObject().apply {
                    addProperty(KEY_CURRENCY, currency)
                })
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

    fun getSyncWalletParams(address: String, pubKey: String, currency: Int): JsonArray {
        return JsonArray().apply {
            add(JsonObject().apply {
                addProperty(KEY_CURRENCY, currency)
                addProperty(KEY_ADDRESS, address)
                addProperty(KEY_PUBKEY, pubKey)
            })
        }
    }

    fun getHistoryParams(address: String): JsonObject = getBalanceParams(address)

    private fun createLoginRequest(params: Any) = ServiceRequest(
            method = METHOD_LOGIN, params = params,
            uid = WalletApplication.deviceId)

    private fun createRegisterRequest(params: Any) = ServiceRequest(
            method = METHOD_REGISTER, params = params,
            uid = WalletApplication.deviceId)

    private fun createWalletsRequest(params: Any) = ServiceRequest(
            method = METHOD_ALL_WALLETS,
            params = params,
            token = WalletApplication.dbHelper.getToken(),
            uid = WalletApplication.deviceId)

    private fun createBalanceRequest(params: Any) = ServiceRequest(
            method = METHOD_WALLET_BALANCE,
            params = params,
            uid = WalletApplication.deviceId)

    private fun createHistoryRequest(params: Any) = ServiceRequest(
            method = METHOD_WALLET_HISTORY,
            params = params,
            uid = WalletApplication.deviceId
    )

    private fun createRefreshRequest() = ServiceRequest(
            method = METHOD_REFRESH_TOKEN,
            token = WalletApplication.dbHelper.getRefreshToken(),
            uid = WalletApplication.deviceId)

    private fun createTransactionRequest(params: Any) = ServiceRequest(
            method = METHOD_CREATE_TX,
            params = params,
            jsonrpc = "2.0",
            uid = WalletApplication.deviceId)

    private fun createTxInfoRequest(params: Any) = ServiceRequest(
            method = METHOD_TX_INFO,
            params = params,
            uid = WalletApplication.deviceId
    )

    private fun createSyncWalletRequest(params: Any) = ServiceRequest(
            method = METHOD_SYNC_WALLET,
            params = params,
            token = WalletApplication.dbHelper.getToken(),
            uid = WalletApplication.deviceId
    )

    enum class REQUESTTYPE {
        LOGIN,
        REGISTER,
        ALLWALLETS,
        WALLETBALANCE,
        REFRESHTOKEN,
        WALLETHISTORY,
        MAKETRANSACTION,
        TXINFO,
        SYNCWALLET
    }
}