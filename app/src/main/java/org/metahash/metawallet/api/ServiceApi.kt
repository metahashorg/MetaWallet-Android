package org.metahash.metawallet.api

import io.reactivex.Observable
import io.reactivex.functions.Function3
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.commands.*
import org.metahash.metawallet.data.ProxyTorrentResolver
import org.metahash.metawallet.data.models.*
import org.metahash.metawallet.extensions.formatProxy
import org.metahash.metawallet.extensions.formatTorrent
import java.util.concurrent.TimeUnit

class ServiceApi(private val api: Api) {

    private val loginCmd by lazy {
        LoginCmd(api)
    }
    private val registerCmd by lazy {
        RegisterCmd(api)
    }
    private val pingCmd by lazy {
        GetProxyCmd(WalletApplication.gson, pingProxyCmd, pingTorrentCmd)
    }
    private val walletsCmd by lazy {
        AllBalancesCmd(allWalletsCmd, balanceCmd)
    }
    private val balanceCmd by lazy {
        WalletBalanceCmd(api)
    }
    private val refreshTokenCmd by lazy {
        RefreshTokenCmd(api)
    }
    private val allHistoryCmd by lazy {
        WalletHistoryCmd(allWalletsCmd, historyCmd)
    }
    private val createTxCmd by lazy {
        MakeTransactionCmd(api)
    }
    private val getTxInfoCmd by lazy {
        GetTxInfoCmd(api)
    }
    private val syncWalletCmd by lazy {
        SyncWalletCmd(api)
    }
    private val allWalletsCmd by lazy {
        AllWalletsCmd(api)
    }
    private val checkBalanceCmd by lazy {
        BalanceChangedCmd(walletsCmd)
    }
    private val historyCmd by lazy {
        HistoryCmd(api)
    }
    private val txParamsCmd by lazy {
        GetTxParamsCmd(api)
    }
    private val pingTorrentCmd by lazy {
        PingTorrentAddressCmd(api)
    }
    private val pingProxyCmd by lazy {
        PingProxyAddressCmd(api)
    }


    fun login(login: String, password: String): Observable<LoginResponse> {
        loginCmd.login = login
        loginCmd.password = password
        return loginCmd.execute()
    }

    fun register(login: String, password: String): Observable<RegisterResponse> {
        registerCmd.login = login
        registerCmd.password = password
        return registerCmd.execute()
    }

    //get wallets by currency and balance for each wallet address
    fun getAllWalletsAndBalance(currency: String, localOnly: Boolean): Observable<String> {
        walletsCmd.currency = currency.toInt()
        walletsCmd.isOnlyLocal = localOnly
        return walletsCmd.executeWithCache()
    }

    fun getBalance(address: String, currencyId: Int): Observable<BalanceResponse> {
        balanceCmd.address = address
        balanceCmd.currency = currencyId
        return balanceCmd.execute()
    }

    fun getHistory(currency: String, localOnly: Boolean): Observable<String> {
        allHistoryCmd.currency = currency.toInt()
        allHistoryCmd.isOnlyLocal = localOnly
        return allHistoryCmd.executeWithCache()
    }

    fun createTransaction(tx: Transaction, currencyId: Int): Observable<CreateTxResult> {
        createTxCmd.to = tx.to
        createTxCmd.value = tx.value
        createTxCmd.fee = tx.fee
        createTxCmd.nonce = tx.nonce
        createTxCmd.data = tx.data
        createTxCmd.pubKey = tx.pubKey
        createTxCmd.sign = tx.sign

        //to be sure proxy list is 3 size exactly
        val proxyList = ProxyTorrentResolver.proxyListResolver(currencyId).toMutableList()
        while (proxyList.size < 3) {
            proxyList.add(proxyList[0])
        }

        return Observable.combineLatest(
                createTxCmd
                        .apply { baseProxyUrl = proxyList[0].ip.formatProxy() }
                        .execute()
                        .startWith(CreateTxResponse.wait())
                        .onErrorReturnItem(CreateTxResponse.error()),
                createTxCmd
                        .apply { baseProxyUrl = proxyList[1].ip.formatProxy() }
                        .execute()
                        .startWith(CreateTxResponse.wait())
                        .onErrorReturnItem(CreateTxResponse.error()),
                createTxCmd
                        .apply { baseProxyUrl = proxyList[2].ip.formatProxy() }
                        .execute()
                        .startWith(CreateTxResponse.wait())
                        .onErrorReturnItem(CreateTxResponse.error()),
                Function3<CreateTxResponse, CreateTxResponse, CreateTxResponse, CreateTxResult>
                { r1, r2, r3 ->
                    val id = when {
                        r1.status == TXSTATUS.OK -> r1.params ?: ""
                        r2.status == TXSTATUS.OK -> r2.params ?: ""
                        r3.status == TXSTATUS.OK -> r3.params ?: ""
                        else -> ""
                    }
                    val proxy = arrayOf(
                            r1.status.toString().toLowerCase(),
                            r2.status.toString().toLowerCase(),
                            r3.status.toString().toLowerCase())

                    val torrent = arrayOf(
                            TXSTATUS.WAIT.toString().toLowerCase(),
                            TXSTATUS.WAIT.toString().toLowerCase(),
                            TXSTATUS.WAIT.toString().toLowerCase())
                    CreateTxResult(id, 2, proxy, torrent)
                }
        )
    }

    fun getTxInfo(prevResult: CreateTxResult, maxTryCount: Long, currencyId: Int): Observable<CreateTxResult> {
        getTxInfoCmd.txHash = prevResult.id

        //to be sure torrent list is 3 size exactly
        val torrentList = ProxyTorrentResolver.torrentListResolver(currencyId).toMutableList()
        while (torrentList.size < 3) {
            torrentList.add(torrentList[0])
        }

        return Observable.combineLatest(
                obsToIntervalWithCount(getTxInfoCmd
                        .apply { baseTorrentUrl = torrentList[0].ip.formatTorrent() }
                        .execute(), maxTryCount = maxTryCount)
                        .startWith(GetTxInfoResponse.wait())
                        .onErrorReturnItem(GetTxInfoResponse.error()),
                obsToIntervalWithCount(getTxInfoCmd
                        .apply { baseTorrentUrl = torrentList[1].ip.formatTorrent() }
                        .execute(), maxTryCount = maxTryCount)
                        .startWith(GetTxInfoResponse.wait())
                        .onErrorReturnItem(GetTxInfoResponse.error()),
                obsToIntervalWithCount(getTxInfoCmd
                        .apply { baseTorrentUrl = torrentList[2].ip.formatTorrent() }
                        .execute(), maxTryCount = maxTryCount)
                        .startWith(GetTxInfoResponse.wait())
                        .onErrorReturnItem(GetTxInfoResponse.error()),
                Function3<GetTxInfoResponse, GetTxInfoResponse, GetTxInfoResponse, CreateTxResult>
                { r1, r2, r3 ->
                    val torrent = arrayOf(
                            r1.status.toString().toLowerCase(),
                            r2.status.toString().toLowerCase(),
                            r3.status.toString().toLowerCase())
                    prevResult.copy(stage = 3, torrent = torrent)
                }
        ).distinctUntilChanged()
    }

    fun ping(type: Proxy.TYPE): Observable<String> {
        pingCmd.type = type
        return pingCmd.execute()
    }

    fun saveProxy() {
        pingCmd.saveProxy()
    }

    fun refreshToken() = refreshTokenCmd.execute()

    fun mapTxResultToString(result: CreateTxResult) = WalletApplication.gson.toJson(result)

    fun syncWallet(address: String, pubKey: String, currency: Int): Observable<SyncWalletResponse> {
        syncWalletCmd.address = address
        syncWalletCmd.currency = currency
        syncWalletCmd.pubKey = pubKey
        return syncWalletCmd.execute()
    }

    fun isBalanceChanged(currency: String): Observable<Boolean> {
        checkBalanceCmd.cur = currency.toInt()
        return checkBalanceCmd.execute()
    }

    fun getTxParams(address: String, currency: Int) {
        txParamsCmd.address = address
        txParamsCmd.currency = currency
        txParamsCmd.execute()
                .subscribe(
                        {
                            if (it != null) {

                            }
                        },
                        {
                            it.printStackTrace()
                        }
                )
    }

    private fun <R> obsToIntervalWithCount(
            obs: Observable<R>,
            delayInSec: Long = 2,
            maxTryCount: Long = 3): Observable<R> {
        return Observable.interval(delayInSec, TimeUnit.SECONDS)
                .take(maxTryCount)
                .switchMap { obs }
    }
}