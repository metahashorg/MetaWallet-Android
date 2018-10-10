package org.metahash.metawallet.presentation.screens.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.orhanobut.hawk.Hawk
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.Constants
import org.metahash.metawallet.R
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.JsFunctionCaller
import org.metahash.metawallet.api.wvinterface.JSBridge
import org.metahash.metawallet.data.models.CreateTxResult
import org.metahash.metawallet.data.models.GetTxInfoResponse
import org.metahash.metawallet.extensions.CryptoExt
import org.metahash.metawallet.extensions.enableInspection
import org.metahash.metawallet.extensions.fromUI
import org.metahash.metawallet.presentation.base.BaseActivity
import java.util.concurrent.TimeUnit


class SplashActivity : BaseActivity() {

    private val MAX_INFO_TRY_COUNT = 5L

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
                        { name, pas, cur, code -> createNewAddress(name, pas, cur, code) },
                        //create transaction
                        { p1, p2, p3, p4, p5, p6 ->
                            createTransaction(p1, p2, p3, p4, p5, p6)
                        },
                        //logout
                        {
                            logout()
                        }
                ),
                Constants.JS_BRIDGE)
    }

    private fun logout() {
        WalletApplication.dbHelper.clearAll()
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
                            handleLoginResponseError(it, webView)
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
                            handleLoginResponseError(it, webView)
                        }
                ))
    }

    private fun getWallets(currency: String) {
        Toast.makeText(this, "getWallets", Toast.LENGTH_LONG).show()
        addSubscription(WalletApplication.api.getAllWalletsAndBalance(currency)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            JsFunctionCaller.callFunction(webView,
                                    JsFunctionCaller.FUNCTION.WALLETSRESULT, it)
                        },
                        {
                            Toast.makeText(this, "getWallets error: ${it.message}", Toast.LENGTH_LONG).show()
                            handleCommonError(it, webView)
                        }
                ))
    }

    private fun getHistory(currency: String) {
        addSubscription(WalletApplication.api.getHistory(currency)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            JsFunctionCaller.callFunction(webView,
                                    JsFunctionCaller.FUNCTION.HISTORYRESULT, it)
                        },
                        {
                            handleCommonError(it, webView)
                        }
                ))
    }

    private fun createNewAddress(name: String, password: String, currency: String, code: String) {
        val wallet = CryptoExt.createWallet()
        if (wallet != null) {
            wallet.currency = currency
            wallet.code = code
            wallet.name = name
            WalletApplication.dbHelper.setUserWallet(wallet)
            fromUI({
                JsFunctionCaller.callFunction(webView, JsFunctionCaller.FUNCTION.NEWWALLERRESULT, wallet.address)
            })
        } else {
            fromUI({
                JsFunctionCaller.callFunction(webView, JsFunctionCaller.FUNCTION.NEWWALLERRESULT, "CREATE_WALLET_ERROR")
            })
        }
    }

    private fun createTransaction(from: String, password: String, to: String,
                                  amount: String, fee: String, data: String) {
        val wallet = WalletApplication.dbHelper.getUserWalletByAddress(from)
        if (wallet == null) {
            //send error here
            JsFunctionCaller.callFunction(webView,
                    JsFunctionCaller.FUNCTION.TRASACTIONRESULT, "NO_PRIVATE_KEY_FOUND")
            return
        }
        addSubscription(WalletApplication.api.getBalance(wallet.address)
                .observeOn(Schedulers.computation())
                .flatMap {
                    val nonce = it.result.countSpent + 1
                    val tx = CryptoExt.createTransaction(wallet, to, password, nonce.toString(), amount, fee, data)
                    fromUI({
                        val res = WalletApplication.api.mapTxResultToString(CreateTxResult(1))
                        Log.d("MIINE", "stage 1: $res")
                        JsFunctionCaller.callFunction(webView,
                                JsFunctionCaller.FUNCTION.TXINFORESULT, res)
                    })
                    WalletApplication.api.createTransaction(tx)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            val res = WalletApplication.api.mapTxResultToString(it)
                            Log.d("MIINE", "stage 2: $res")
                            JsFunctionCaller.callFunction(webView,
                                    JsFunctionCaller.FUNCTION.TXINFORESULT, res)
                            if (it.isProxyReady()) {
                                startTrxCheck(it)
                            }

                        },
                        {
                            handleCommonError(it, webView)
                        }
                ))
    }

    private fun startTrxCheck(result: CreateTxResult) {
        val obs = object : DisposableObserver<CreateTxResult>() {
            override fun onComplete() {}

            override fun onNext(result: CreateTxResult) {
                //update status
                /*JsFunctionCaller.callFunction(webView,
                        JsFunctionCaller.FUNCTION.TXINFORESULT, "")*/
                val res = WalletApplication.api.mapTxResultToString(result)
                Log.d("MIINE", "stage 3: $res")
                if (result.isTorrentSuccessful()) {
                    dispose()
                }
            }

            override fun onError(e: Throwable) {
                handleCommonError(e, webView)
            }
        }
        addSubscription(
                WalletApplication.api.getTxInfo(result, MAX_INFO_TRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(obs))
    }
}