package org.metahash.metawallet.data

import com.orhanobut.hawk.Hawk
import org.metahash.metawallet.data.models.*

class DBHelper {

    private val KEY_PROXY = "key_proxy"
    private val KEY_TORRENT = "key_torrent"
    private val KEY_TOKEN = "key_token"
    private val KEY_LOGIN = "key_login"
    private val KEY_REFRESH_TOKEN = "key_refresh_token"
    private val KEY_WALLETS = "key_wallets"
    private val KEY_WALLET_HISTORY = "key_wallet_history"

    //PROXY AND TORRENT IP
    fun setProxy(proxy: Proxy) {
        Hawk.put(KEY_PROXY, proxy)
    }

    fun getProxy() = Hawk.get<Proxy>(KEY_PROXY, Proxy.getDefault())

    fun setTorrent(proxy: Proxy) {
        Hawk.put(KEY_TORRENT, proxy)
    }

    //TOKEN AND LOGIN
    fun getTorrent() = Hawk.get<Proxy>(KEY_TORRENT, Proxy.getDefault())

    fun setToken(token: String) {
        Hawk.put(KEY_TOKEN, token)
    }

    fun getToken() = Hawk.get<String>(KEY_TOKEN, "")

    fun setRefreshToken(token: String) {
        Hawk.put(KEY_REFRESH_TOKEN, token)
    }

    fun getRefreshToken() = Hawk.get<String>(KEY_REFRESH_TOKEN, "")

    fun setLogin(login: String) {
        Hawk.put(KEY_LOGIN, login)
    }

    fun getLogin() = Hawk.get<String>(KEY_LOGIN, "")

    fun hasToken() = getToken().isNotEmpty() && getRefreshToken().isNotEmpty()

    //WALLETS WITH BALANCE
    fun setWallets(wallets: List<WalletsData>, currency: String) {
        val data = getAllWallets()
        data.removeAll { it.currencyCode.equals(currency, true) }
        data.addAll(wallets)
        Hawk.put(KEY_WALLETS, wallets)
    }

    private fun getAllWallets() = Hawk.get<MutableList<WalletsData>>(KEY_WALLETS, mutableListOf())

    fun getWallets(currency: String?): List<WalletsData> {
        val data = getAllWallets()
        if (currency == null) {
            return data
        }
        return data.filter {
            it.currencyCode.equals(currency, true)
        }
    }

    //WALLETS HISTORY
    private fun getAllHistory() = Hawk.get<MutableList<HistoryData>>(KEY_WALLET_HISTORY, mutableListOf())

    fun setWalletHistory(currency: String, list: List<HistoryData>) {
        val data = getAllHistory()
        data.removeAll { it.currency.equals(currency, true) }
        data.addAll(list)
        Hawk.put(KEY_WALLET_HISTORY, data)
    }

    fun getWalletHistory(currency: String): List<HistoryData> {
        val data = getAllHistory()
        return data.filter {
            it.currency.equals(currency, true)
        }
    }
}