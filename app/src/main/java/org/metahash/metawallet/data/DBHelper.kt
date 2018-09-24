package org.metahash.metawallet.data

import com.orhanobut.hawk.Hawk
import org.metahash.metawallet.data.models.Proxy
import org.metahash.metawallet.data.models.WalletHistoryRaw

class DBHelper {

    private val KEY_PROXY = "key_proxy"
    private val KEY_TORRENT = "key_torrent"
    private val KEY_TOKEN = "key_token"
    private val KEY_LOGIN = "key_login"
    private val KEY_REFRESH_TOKEN = "key_refresh_token"
    private val KEY_WALLETS_RAW = "key_wallets_raw"
    private val KEY_WALLET_HISTORY_RAW = "key_wallet_history_raw"

    fun setProxy(proxy: Proxy) {
        Hawk.put(KEY_PROXY, proxy)
    }

    fun getProxy() = Hawk.get<Proxy>(KEY_PROXY, Proxy.getDefault())

    fun setTorrent(proxy: Proxy) {
        Hawk.put(KEY_TORRENT, proxy)
    }

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

    fun setRawWallets(wallets: String) {
        Hawk.put(KEY_WALLETS_RAW, wallets)
    }

    fun getRawWallets() = Hawk.get<String>(KEY_WALLETS_RAW, "")

    private fun getAllHistory() = Hawk.get<MutableList<WalletHistoryRaw>>(KEY_WALLET_HISTORY_RAW, mutableListOf())

    fun setRawWalletHistory(address: String, rawData: String) {
        val data = getAllHistory()
        data.removeAll { it.address == address }
        data.add(WalletHistoryRaw(address, rawData))
        Hawk.put(KEY_WALLET_HISTORY_RAW, data)
    }

    fun getRawWalletHistory(address: String): String {
        val data = getAllHistory()
        return data.firstOrNull {
            it.address == address
        }?.raw ?: ""
    }
}