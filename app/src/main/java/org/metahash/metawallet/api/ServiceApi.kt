package org.metahash.metawallet.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.commands.*
import org.metahash.metawallet.data.models.*
import retrofit2.HttpException

class ServiceApi(
        private val api: Api) {

    private val proxyUrl: String by lazy {
        "http://${WalletApplication.dbHelper.getProxy().ip}:80"
    }
    private val torrentUrl: String by lazy {
        WalletApplication.dbHelper.getTorrent().ip + "/api/"
    }

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

    fun ping() = pingCmd.execute()

    fun refreshToken() = refreshTokenCmd.execute()
}