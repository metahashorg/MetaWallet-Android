package org.metahash.metawallet.data

import com.orhanobut.hawk.Hawk
import org.metahash.metawallet.data.models.*
import java.util.concurrent.TimeUnit

class DBHelper {

    private val KEY_PROXY_TORRENT = "key_proxy_torrent"
    private val KEY_TOKEN = "key_token"
    private val KEY_LOGIN = "key_login"
    private val KEY_REFRESH_TOKEN = "key_refresh_token"
    private val KEY_WALLETS = "key_wallets"
    private val KEY_WALLET_HISTORY = "key_wallet_history"
    private val KEY_USER_WALLETS = "key_user_wallets"
    private val KEY_ONLY_LOCAL_WALLETS = "key_only_local_wallets"
    private val KEY_USER_PINCODE = "key_user_pincode"
    private val KEY_LAST_ACTION_TIME = "key_last_action_time"
    private val KEY_SAVED_LANGUAGE = "key_saved_language"

    fun clearAll() {
        Hawk.delete(KEY_TOKEN)
        deleteUserPincode(getLogin())
        Hawk.delete(KEY_LOGIN)
        Hawk.delete(KEY_REFRESH_TOKEN)
        Hawk.delete(KEY_WALLETS)
        Hawk.delete(KEY_WALLET_HISTORY)
        Hawk.delete(KEY_ONLY_LOCAL_WALLETS)
    }

    //region PROXY AND TORRENT IP
    @Synchronized
    fun setProxy(proxy: List<Proxy>) {
        val data = Hawk.get<ProxyData>(KEY_PROXY_TORRENT, ProxyData())
        data.proxy.addAll(proxy)
        data.lastUpdateTime = System.currentTimeMillis()
        Hawk.put(KEY_PROXY_TORRENT, data)
    }

    @Synchronized
    fun getAllProxy(type: Proxy.TYPE? = Proxy.TYPE.DEV): List<Proxy> {
        val list = Hawk.get<ProxyData>(KEY_PROXY_TORRENT, ProxyData()).proxy
        return when (type) {
            null -> list
            else -> list.filter { it.type == type }
        }
    }

    @Synchronized
    fun getProxy(type: Proxy.TYPE? = Proxy.TYPE.DEV): Proxy {
        val list = getAllProxy(type)
        return if (list.isEmpty()) {
            Proxy.getDefault()
        } else {
            list[0]
        }
    }

    @Synchronized
    fun setTorrent(proxy: List<Proxy>) {
        val data = Hawk.get<ProxyData>(KEY_PROXY_TORRENT, ProxyData())
        data.torrent.addAll(proxy)
        data.lastUpdateTime = System.currentTimeMillis()
        Hawk.put(KEY_PROXY_TORRENT, data)
    }

    @Synchronized
    fun getAllTorrent(type: Proxy.TYPE? = Proxy.TYPE.DEV): List<Proxy> {
        val list = Hawk.get<ProxyData>(KEY_PROXY_TORRENT, ProxyData()).torrent
        return when (type) {
            null -> list
            else -> list.filter { it.type == type }
        }
    }

    @Synchronized
    fun getTorrent(type: Proxy.TYPE? = Proxy.TYPE.DEV): Proxy {
        val list = getAllTorrent(type)
        return if (list.isEmpty()) {
            Proxy.getDefault()
        } else {
            list[0]
        }
    }

    @Synchronized
    fun needUpdateProxy(maxDiffDays: Long): Boolean {
        val data = Hawk.get<ProxyData>(KEY_PROXY_TORRENT, ProxyData())
        val diff = System.currentTimeMillis() - data.lastUpdateTime
        return (TimeUnit.MILLISECONDS.toDays(diff) >= maxDiffDays) ||
                data.torrent.isEmpty() ||
                data.proxy.isEmpty()
    }

    @Synchronized
    fun deleteProxyTorrent() {
        Hawk.delete(KEY_PROXY_TORRENT)
    }
    //endregion

    //region TOKEN AND LOGIN
    @Synchronized
    fun setToken(token: String) {
        Hawk.put(KEY_TOKEN, token)
    }

    @Synchronized
    fun getToken() = Hawk.get<String>(KEY_TOKEN, "")

    @Synchronized
    fun setRefreshToken(token: String) {
        Hawk.put(KEY_REFRESH_TOKEN, token)
    }

    @Synchronized
    fun getRefreshToken() = Hawk.get<String>(KEY_REFRESH_TOKEN, "")

    @Synchronized
    fun setLogin(login: String) {
        Hawk.put(KEY_LOGIN, login)
    }

    @Synchronized
    fun getLogin() = Hawk.get<String>(KEY_LOGIN, "")

    @Synchronized
    fun hasToken() = getToken().isNotEmpty() && getRefreshToken().isNotEmpty()

    //WALLETS WITH BALANCE
    @Synchronized
    private fun getAllWalletsData() = Hawk.get<MutableList<WalletsData>>(KEY_WALLETS, mutableListOf())

    @Synchronized
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

    @Synchronized
    fun getWalletsDataByCurrency(currency: String?, userLogin: String): List<WalletsData> {
        val data = getAllWalletsData()
        if (currency == null) {
            return data
        }
        return data.filter {
            it.currency.equals(currency, true) &&
                    it.userLogin == userLogin
        }
    }
    //endregion


    //region WALLETS HISTORY
    @Synchronized
    private fun getAllWalletsHistory() = Hawk.get<MutableList<HistoryData>>(KEY_WALLET_HISTORY, mutableListOf())

    @Synchronized
    fun setWalletHistory(currency: String, list: List<HistoryData>) {
        val currentUserLogin = getLogin()
        val data = getAllWalletsHistory()
        data.removeAll {
            it.currency.equals(currency, true) &&
                    it.userLogin == currentUserLogin
        }
        data.addAll(list)
        Hawk.put(KEY_WALLET_HISTORY, data)
    }

    @Synchronized
    fun getWalletHistoryByCurrency(currency: String): List<HistoryData> {
        val currentUserLogin = getLogin()
        val data = getAllWalletsHistory()
        return data.filter {
            it.currency.equals(currency, true) &&
                    it.userLogin == currentUserLogin
        }
    }
    //endregion


    //region USER WALLETS
    @Synchronized
    private fun getUserWallets() = Hawk.get<MutableList<Wallet>>(KEY_USER_WALLETS, mutableListOf())

    @Synchronized
    private fun setUserWallets(wallets: List<Wallet>) {
        Hawk.put(KEY_USER_WALLETS, wallets)
    }

    @Synchronized
    fun deleteUserWalletByAddress(
        address: String,
        userLogin: String
    ) {
        val allWallets = getUserWallets()
        val index = allWallets.indexOfFirst { it.address == address && it.userLogin == userLogin }
        if (index != -1) {
            allWallets.removeAt(index)
            setUserWallets(allWallets)
        }
        val walletsData = getAllWalletsData()
        val walletDataIndex = walletsData.indexOfFirst { it.address == address && it.userLogin == userLogin }
        if (walletDataIndex != -1) {
            walletsData.removeAt(walletDataIndex)
            Hawk.put(KEY_WALLETS, walletsData)
        }
    }

    @Synchronized
    fun setUserWallet(wallet: Wallet) {
        val data = getUserWallets()
        data.add(wallet)
        setUserWallets(data)
    }

    @Synchronized
    fun updateUserWallet(wallet: Wallet, userLogin: String) {
        val data = getUserWallets()
        data.removeAll { it.address == wallet.address && it.userLogin == userLogin }
        data.add(wallet)
        setUserWallets(data)
    }

    @Synchronized
    fun getUserWalletByAddress(address: String, userLogin: String): Wallet? {
        return getUserWallets()
            .firstOrNull { it.address == address && it.userLogin == userLogin }
    }

    @Synchronized
    fun getUserWalletByLogin(userLogin: String): List<Wallet> {
        return getUserWallets()
            .filter { it.userLogin == userLogin }
    }

    @Synchronized
    fun getUserWalletsByCurrency(currency: String, userLogin: String): List<Wallet> {
        return getUserWallets()
            .filter { it.currency.equals(currency, true) }
            .filter { it.userLogin == userLogin }
    }

    @Synchronized
    fun setWalletSynchronized(address: String, userLogin: String) {
        val wallet = getUserWalletByAddress(address, userLogin) ?: return
        wallet.isSynchronized = true
        updateUserWallet(wallet, userLogin)
    }

    @Synchronized
    fun getUnsynchonizedWallets(userLogin: String): List<Wallet> {
        val data = getUserWallets()
        return data
            .filter { it.userLogin == userLogin }
            .filter { it.isSynchronized.not() }
    }

    @Synchronized
    fun setOnlyLocalWallets(onlyLocal: Boolean) {
        Hawk.put(KEY_ONLY_LOCAL_WALLETS, onlyLocal)
    }

    @Synchronized
    fun isOnlyLocalWallets() = Hawk.get<Boolean>(KEY_ONLY_LOCAL_WALLETS, false)

    //endregion


    //region PINCODES
    @Synchronized
    private fun getAllPincodes() = Hawk.get<MutableList<UserPincode>>(KEY_USER_PINCODE, mutableListOf())

    @Synchronized
    private fun getUserPincode(username: String): UserPincode? {
        val list = getAllPincodes()
        return list.firstOrNull { it.username == username }
    }

    @Synchronized
    fun checkPincode(code: String, username: String): Boolean {
        val userPin = getUserPincode(username) ?: return false
        return code == userPin.pincode
    }

    @Synchronized
    fun hasPincode(username: String): Boolean {
        val userPin = getUserPincode(username) ?: return false
        return userPin.hasPincode()
    }

    @Synchronized
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

    @Synchronized
    fun deleteUserPincode(username: String) {
        val list = getAllPincodes()
        list.removeAll { it.username == username }
        Hawk.put(KEY_USER_PINCODE, list)
    }

    @Synchronized
    fun setLastActionTime(time: Long) {
        Hawk.put(KEY_LAST_ACTION_TIME, time)
    }

    @Synchronized
    fun getLastActionTime() = Hawk.get<Long>(KEY_LAST_ACTION_TIME, System.currentTimeMillis())

    @Synchronized
    fun clearLastActionTime() {
        Hawk.delete(KEY_LAST_ACTION_TIME)
    }

    @Synchronized
    fun getCurrencyIdByAddress(address: String): Int {
        val wallet = getUserWalletByAddress(address, getLogin())
        return wallet?.currency?.toInt() ?: -1
    }
    //endregion

    //region LANGUAGE
    fun getLanguage(): String = Hawk.get<String>(KEY_SAVED_LANGUAGE, "")

    fun saveLanguage(language: String) {
        Hawk.put(KEY_SAVED_LANGUAGE, language)
    }
    //endregion

}