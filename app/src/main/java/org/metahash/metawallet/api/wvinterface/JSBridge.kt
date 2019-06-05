package org.metahash.metawallet.api.wvinterface

import android.webkit.JavascriptInterface

class JSBridge(
        private val onAuth: (String, String) -> Unit,
        private val onGetLogin: () -> String,
        private val onGetWallets: (String) -> Unit,
        private val onGetHistory: (String) -> Unit,
        private val onGenerateAddress: (String, String, String, String) -> Unit,
        private val onCreateTransaction: (String, String, String, String, String, String) -> Unit,
        private val onCreateTransactionNew: (String, String, String, String, String, String, String) -> Unit,
        private val onLogOut: () -> Unit,
        private val onSignUp: (String, String) -> Unit,
        private val setOnlyLocal: (Boolean) -> Unit,
        private val getOnlyLocal: () -> Boolean,
        private val onGetPrivateKey: (String, String) -> String,
        private val onGetAppVersion: () -> String,
        private val onStartQr: () -> Unit,
        private val onImport: (String, String, String, String, String, String) -> Unit,
        private val onClearCache: () -> Unit,
        private val onImportPrivateWallet: (String, String, String, String) -> Unit) {

    //method to login
    @JavascriptInterface
    fun getAuthRequest(login: String, password: String) {
        onAuth.invoke(login, password)
    }

    //method to return user login to js
    @JavascriptInterface
    fun getAuthData(): String = onGetLogin.invoke()

    @JavascriptInterface
    fun getWalletsData(currency: String) {
        onGetWallets.invoke(currency)
    }

    @JavascriptInterface
    fun getWalletsHistory(currency: String) {
        onGetHistory.invoke(currency)
    }

    @JavascriptInterface
    fun createAddress(name: String, password: String, code: String, currency: String) {
        onGenerateAddress.invoke(name, password, currency, code)
    }

    @JavascriptInterface
    fun sendTMHTx(from: String, password: String, to: String,
                  amount: String, fee: String, data: String) {
        onCreateTransaction.invoke(from, password, to, amount, fee, data)
    }

    @JavascriptInterface
    fun sendTx(from: String, password: String, to: String,
               amount: String, fee: String, data: String, currency: String) {
        onCreateTransactionNew.invoke(from, password, to, amount, fee, data, currency)
    }

    @JavascriptInterface
    fun logOut() {
        onLogOut.invoke()
    }

    @JavascriptInterface
    fun signUp(login: String, password: String) {
        onSignUp.invoke(login, password)
    }

    @JavascriptInterface
    fun setOnlyLocalAddresses(onlyLocal: Boolean) {
        setOnlyLocal.invoke(onlyLocal)
    }

    @JavascriptInterface
    fun getOnlyLocalAddresses(): Boolean = getOnlyLocal.invoke()

    @JavascriptInterface
    fun getPrivateKey(address: String, password: String): String = onGetPrivateKey.invoke(address, password)

    @JavascriptInterface
    fun getAppVersion(): String = onGetAppVersion.invoke()

    @JavascriptInterface
    fun startQRImport() {
        onStartQr.invoke()
    }

    @JavascriptInterface
    fun importWallet(address: String, privKey: String, password: String,
                     currency: String, currency_code: String, name: String) {
        onImport.invoke(address, privKey, password, currency, currency_code, name)
    }

    @JavascriptInterface
    fun clearCache() {
        onClearCache.invoke()
    }

    @JavascriptInterface
    fun importPrivateWallet(password: String, currency: String, currencyCode: String, name: String) {
        onImportPrivateWallet.invoke(password, currency, currencyCode, name)
    }
}