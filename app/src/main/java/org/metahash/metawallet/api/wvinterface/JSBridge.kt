package org.metahash.metawallet.api.wvinterface

import android.webkit.JavascriptInterface

class JSBridge(
        private val onAuth: (String, String) -> Unit,
        private val onGetLogin: () -> String) {

    //method to login
    @JavascriptInterface
    fun getAuthRequest(login: String, password: String) {
        onAuth.invoke(login, password)
    }

    //method to return user login to js
    @JavascriptInterface
    fun getAuthData(): String = onGetLogin.invoke()
}