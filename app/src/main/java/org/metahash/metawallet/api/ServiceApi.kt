package org.metahash.metawallet.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.json.JSONObject
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.commands.*
import org.metahash.metawallet.data.models.*
import retrofit2.HttpException
import retrofit2.Response

class ServiceApi(
        private val api: Api) {

    private val loginCmd: LoginCmd by lazy {
        LoginCmd(api)
    }
    private val pingCmd by lazy {
        GetProxyCommand()
    }
    private val walletsCmd by lazy {
        AllWalletsCmd(api, balanceCmd)
    }
    private val balanceCmd by lazy {
        WalletBalanceCmd(api)
    }
    private val refreshTokenCmd by lazy {
        RefreshTokenCmd(api)
    }
    private val historyCmd by lazy {
        WalletHistoryCmd(api, walletsCmd)
    }

    private val createTrxCmd by lazy {
        MakeTransactionCmd(api)
    }


    fun login(login: String, password: String): Observable<LoginResponse> {
        loginCmd.login = login
        loginCmd.password = password
        return loginCmd.execute()
    }

    //get wallets by currency and balance for each wallet address
    fun getAllWalletsAndBalance(currency: String): Observable<String> {
        walletsCmd.currency = currency
        return walletsCmd.executeWithCache()
    }

    fun getBalance(address: String): Observable<BalanceResponse> {
        balanceCmd.address = address
        return balanceCmd.execute()
    }

    fun getHistory(currency: String): Observable<String> {
        historyCmd.currency = currency
        return historyCmd.executeWithCache()
    }

    fun createTransaction(trx: Transaction): Observable<Response<ResponseBody>> {
        createTrxCmd.to = trx.to
        createTrxCmd.value = trx.value
        createTrxCmd.fee = trx.fee
        createTrxCmd.nonce = trx.nonce
        createTrxCmd.data = trx.data
        createTrxCmd.pubKey = trx.pubKey
        createTrxCmd.sign = trx.sign
        return createTrxCmd.execute()
    }

    fun ping() = pingCmd.execute()

    fun refreshToken() = refreshTokenCmd.execute()
}