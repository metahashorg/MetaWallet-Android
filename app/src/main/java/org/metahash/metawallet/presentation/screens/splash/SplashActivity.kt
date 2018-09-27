package org.metahash.metawallet.presentation.screens.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.orhanobut.hawk.Hawk
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.metahash.metawallet.Constants
import org.metahash.metawallet.R
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.JsFunctionCaller
import org.metahash.metawallet.api.wvinterface.JSBridge
import org.metahash.metawallet.extensions.CryptoExt
import org.metahash.metawallet.extensions.enableInspection
import org.metahash.metawallet.extensions.fromUI
import org.metahash.metawallet.presentation.base.BaseActivity
import java.util.concurrent.TimeUnit

class SplashActivity : BaseActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        webView = findViewById(R.id.wv)
        initWebView()
        webView.loadUrl(Constants.WEB_URL)
        ping()
        //WalletApplication.api.getBalance("0x00a09cec7588af57ac9e42e5b6a30a392d81b02855814301aa")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.enableInspection()
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        registerJSCallbacks()
    }

    @SuppressLint("AddJavascriptInterface")
    private fun registerJSCallbacks() {
        webView.addJavascriptInterface(
                JSBridge(
                        //sign in
                        { l, p -> login(l, p) },
                        //get user login
                        { WalletApplication.dbHelper.getLogin() },
                        //get wallets by currency
                        { getWallets(it) },
                        //get history by currency
                        { getHistory(it) },
                        //create address
                        {name, pas -> createNewAddress(name, pas) }
                ),
                Constants.JS_BRIDGE)
    }

    private fun login(login: String, password: String) {
        addSubscription(WalletApplication.api.login(login, password)
                .subscribe(
                        {
                            if (it.isOk()) {
                                WalletApplication.dbHelper
                                        .setToken(it.data.token)
                                WalletApplication.dbHelper
                                        .setRefreshToken(it.data.refreshToken)
                                WalletApplication.dbHelper
                                        .setLogin(login)
                                JsFunctionCaller.callFunction(
                                        webView,
                                        JsFunctionCaller.FUNCTION.LOGINRESULT,
                                        "", "")
                            }
                        },
                        {
                            processResponseError(it, webView)
                        }
                ))
    }

    private fun ping() {
        addSubscription(WalletApplication.api.ping()
                .flatMap {
                    if (it.not()) {
                        return@flatMap Observable.just("")
                    }
                    if (WalletApplication.dbHelper.hasToken()) {
                        //refresh token and return token
                        WalletApplication.api.refreshToken()
                                .map {
                                    //if refresh is ok, return new token
                                    if (it.isOk()) {
                                        WalletApplication.dbHelper
                                                .setToken(it.data.token)
                                        WalletApplication.dbHelper
                                                .setRefreshToken(it.data.refreshToken)
                                        it.data.token
                                    } else {
                                        //else return empty one
                                        ""
                                    }
                                }
                    } else {
                        //no user token, return empty
                        Observable.just("")
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (it.isEmpty()) {
                                JsFunctionCaller.callFunction(webView,
                                        JsFunctionCaller.FUNCTION.ONIPREADY, false)
                            } else {
                                JsFunctionCaller.callFunction(webView,
                                        JsFunctionCaller.FUNCTION.ONIPREADY, true)
                            }
                        },
                        {
                            processResponseError(it, webView)
                        }
                ))
    }

    private fun getWallets(currency: String) {
        WalletApplication.api.getAllWalletsAndBalance(currency)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            JsFunctionCaller.callFunction(webView,
                                    JsFunctionCaller.FUNCTION.WALLETSRESULT, it)
                        },
                        {
                            processResponseError(it, webView)
                        }
                )
    }

    private fun getHistory(currency: String) {
        WalletApplication.api.getHistory(currency)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            JsFunctionCaller.callFunction(webView,
                                    JsFunctionCaller.FUNCTION.HISTORYRESULT, it)
                        },
                        {
                            processResponseError(it, webView)
                        }
                )
    }

    private fun createNewAddress(name: String, password: String) {
        val wallet = CryptoExt.generateWallet()
        if (wallet != null) {
            WalletApplication.dbHelper.setUserWallet(wallet)
            fromUI({
                JsFunctionCaller.callFunction(webView, JsFunctionCaller.FUNCTION.NEWWALLERRESULT, wallet.address, "")
            })
        } else {

        }
    }
}