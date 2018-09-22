package org.metahash.metawallet.presentation.screens.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.metahash.metawallet.Constants
import org.metahash.metawallet.R
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.JsFunctionCaller
import org.metahash.metawallet.api.wvinterface.JSBridge
import org.metahash.metawallet.extensions.enableInspection
import org.metahash.metawallet.presentation.base.BaseActivity

class SplashActivity : BaseActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        webView = findViewById(R.id.wv)
        initWebView()
        webView.loadUrl(Constants.WEB_URL)
        ping()
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
                        { WalletApplication.dbHelper.getLogin() }
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
                        //no user toke, return empty
                        Observable.just("")
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (it.isEmpty()) {
                                JsFunctionCaller.callFunction(WebView(this),
                                        JsFunctionCaller.FUNCTION.ONIPREADY, "false")
                            } else {
                                JsFunctionCaller.callFunction(WebView(this),
                                        JsFunctionCaller.FUNCTION.ONIPREADY, "true")
                            }
                        },
                        {
                            processResponseError(it, webView)
                        }
                ))
    }
}