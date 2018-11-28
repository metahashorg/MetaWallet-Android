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
    private val KEY_ONLY_LOCAL_WALLETS = "key_only_local_wallets"
    private val KEY_USER_PINCODE = "key_user_pincode"

    fun clearAll() {
        Hawk.delete(KEY_TOKEN)
        deleteUserPincode(getLogin())
        Hawk.delete(KEY_LOGIN)
        Hawk.delete(KEY_REFRESH_TOKEN)
        Hawk.delete(KEY_WALLETS)
        Hawk.delete(KEY_WALLET_HISTORY)
        Hawk.delete(KEY_ONLY_LOCAL_WALLETS)
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
    private fun getAllWalletsData() = Hawk.get<MutableList<WalletsData>>(KEY_WALLETS, mutableListOf())

    fun setWalletsData(wallets: List<WalletsData>) {
        val data = getAllWalletsData()
        wallets.forEach { wallet ->
            val pos = data.indexOfFirst { it.address == wallet.address }
            if (pos != -1) {
                data.removeAt(pos)
            }
        }
        data.addAll(wallets)
        Hawk.put(KEY_WALLETS, data)
    }

    fun getWalletsDataByCurrency(currency: String?): List<WalletsData> {
        val data = getAllWalletsData()
        if (currency == null) {
            return data
        }
        return data.filter { it.currency.equals(currency, true) }
    }

    //WALLETS HISTORY
    private fun getAllWalletsHistory() = Hawk.get<MutableList<HistoryData>>(KEY_WALLET_HISTORY, mutableListOf())

    fun setWalletHistory(currency: String, list: List<HistoryData>) {
        val data = getAllWalletsHistory()
        data.removeAll { it.currency.equals(currency, true) }
        data.addAll(list)
        Hawk.put(KEY_WALLET_HISTORY, data)
    }

    fun getWalletHistoryByCurrency(currency: String): List<HistoryData> {
        val data = getAllWalletsHistory()
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

    fun updateUserWallet(wallet: Wallet, userLogin: String) {
        val data = getUserWallets()
        data.removeAll { it.address == wallet.address && it.userLogin == userLogin}
        data.add(wallet)
        Hawk.put(KEY_USER_WALLETS, data)
    }

    fun getUserWalletByAddress(address: String, userLogin: String): Wallet? {
        return getUserWallets()
                .firstOrNull { it.address == address && it.userLogin == userLogin }
    }

    fun getUserWalletsByCurrency(currency: String, userLogin: String): List<Wallet> {
        return getUserWallets()
                .filter { it.currency.equals(currency, true) }
                .filter { it.userLogin == userLogin }
    }

    fun setWalletSynchronized(address: String, userLogin: String) {
        val wallet = getUserWalletByAddress(address, userLogin) ?: return
        wallet.isSynchronized = true
        updateUserWallet(wallet, userLogin)
    }

    fun getUnsynchonizedWallets(userLogin: String): List<Wallet> {
        val data = getUserWallets()
        return data
                .filter { it.userLogin == userLogin }
                .filter { it.isSynchronized.not() }
    }

    fun setOnlyLocalWallets(onlyLocal: Boolean) {
        Hawk.put(KEY_ONLY_LOCAL_WALLETS, onlyLocal)
    }

    fun isOnlyLocalWallets() = Hawk.get<Boolean>(KEY_ONLY_LOCAL_WALLETS, false)

    private fun getAllPincodes() = Hawk.get<MutableList<UserPincode>>(KEY_USER_PINCODE, mutableListOf())

    private fun getUserPincode(username: String): UserPincode? {
        val list = getAllPincodes()
        return list.firstOrNull { it.username == username }
    }

    fun checkPincode(code: String, username: String): Boolean {
        val userPin = getUserPincode(username) ?: return false
        return code == userPin.pincode
    }

    fun hasPincode(username: String): Boolean {
        val userPin = getUserPincode(username) ?: return false
        return userPin.hasPincode()
    }

    fun setPincode(code: String, username: String) {
        //get or create new
        val userPin = getUserPincode(username) ?: UserPincode(username)
        userPin.pincode = code
        //remove old object
        val list = getAllPincodes()
        list.removeAll { it.username == username }
        list.add(userPin)
        Hawk.put(KEY_USER_PINCODE, list)
    }

    fun deleteUserPincode(username: String) {
        val list = getAllPincodes()
        list.removeAll { it.username == username }
        Hawk.put(KEY_USER_PINCODE, list)
    }
}