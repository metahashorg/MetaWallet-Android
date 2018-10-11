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
    private val KEY_USER_WALLETS = "key_user_wallets"

    fun clearAll() {
        Hawk.deleteAll()
    }

    //PROXY AND TORRENT IP
    fun setProxy(proxy: List<Proxy>) {
        Hawk.put(KEY_PROXY, proxy)
    }

    fun getAllProxy() = Hawk.get<List<Proxy>>(KEY_PROXY, listOf())

    fun getProxy(): Proxy {
        val list = getAllProxy()
        return if (list.isEmpty()) {
            Proxy.getDefault()
        } else {
            list[0]
        }
    }

    fun setTorrent(proxy: List<Proxy>) {
        Hawk.put(KEY_TORRENT, proxy)
    }

    //TOKEN AND LOGIN
    fun getAllTorrent() = Hawk.get<List<Proxy>>(KEY_TORRENT, listOf())

    fun getTorrent(): Proxy {
        val list = getAllTorrent()
        return if (list.isEmpty()) {
            Proxy.getDefault()
        } else {
            list[0]
        }
    }

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
    private fun getAllWallets() = Hawk.get<MutableList<WalletsData>>(KEY_WALLETS, mutableListOf())

    fun setWallets(wallets: List<WalletsData>, currency: String) {
        val data = getAllWallets()
        data.removeAll { it.currency.equals(currency, true) }
        data.addAll(wallets)
        Hawk.put(KEY_WALLETS, data)
    }

    fun getWallets(currency: String?): List<WalletsData> {
        val data = getAllWallets()
        if (currency == null) {
            return data
        }
        return data.filter {
            it.currency.equals(currency, true)
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

    //user wallets
    private fun getUserWallets() = Hawk.get<MutableList<Wallet>>(KEY_USER_WALLETS, mutableListOf())

    fun setUserWallet(wallet: Wallet) {
        val data = getUserWallets()
        data.add(wallet)
        Hawk.put(KEY_USER_WALLETS, data)
    }

    private fun updateUserWallet(wallet: Wallet) {
        val data = getUserWallets()
        data.removeAll { it.address == wallet.address }
        data.add(wallet)
        Hawk.put(KEY_USER_WALLETS, data)
    }

    fun getUserWalletByAddress(address: String): Wallet? {
        return getUserWallets()
                ?.firstOrNull { it.address == address }
    }

    fun getUserWalletsByCurrency(currency: String): List<Wallet> {
        return getUserWallets().filter {
            it.currency.equals(currency, true)
        }
    }

    fun setWalletSynchronized(address: String) {
        val wallet = getUserWalletByAddress(address) ?: return
        wallet.isSynchronized = true
        updateUserWallet(wallet)
    }

    fun getUnsynchonizedWallets(): List<Wallet> {
        val data = getUserWallets()
        return data.filter { it.isSynchronized.not() }
    }
}