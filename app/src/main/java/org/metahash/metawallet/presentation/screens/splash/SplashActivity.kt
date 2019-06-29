package org.metahash.metawallet.presentation.screens.splash

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.BuildConfig
import org.metahash.metawallet.Constants
import org.metahash.metawallet.R
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.JsFunctionCaller
import org.metahash.metawallet.api.ServiceApi
import org.metahash.metawallet.api.wvinterface.JSBridge
import org.metahash.metawallet.data.models.*
import org.metahash.metawallet.extensions.*
import org.metahash.metawallet.presentation.base.BaseActivity
import org.metahash.metawallet.presentation.base.deeplink.DeepLinkResolver
import org.metahash.metawallet.presentation.base.deeplink.linkbuilder.TransactionLinkBuilder
import org.metahash.metawallet.presentation.base.deeplink.queryparams.TransactionParams
import org.metahash.metawallet.presentation.screens.qrreader.QrReaderActivity
import org.metahash.metawallet.presentation.views.TouchWebView
import java.util.concurrent.TimeUnit

class SplashActivity : BaseActivity() {

    private val MAX_INFO_TRY_COUNT = 5L
    private val REQUEST_READ_KEY = 112
    private val REQUEST_CAMERA_PERM = 113

    private val webView by bind<TouchWebView>(R.id.wv)
    private val vLoading by bind<View>(R.id.vLoading)
    private val tvLoading by bind<TextView>(R.id.tv1)
    private val tvError by bind<View>(R.id.tv2)

    private var mMHCWalletsDisposable = Disposables.disposed()
    private var mTMHWalletsDisposable = Disposables.disposed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        showLoadingOrError()
        initWebView()
        webView.loadUrl(Constants.WEB_URL)
        fromUI({
            //if need to update - delete proxy and torrent, then ping
            if (WalletApplication.dbHelper.needUpdateProxy(Constants.MAX_PROXY_UPDATE)) {
                WalletApplication.dbHelper.deleteProxyTorrent()
                ping(Proxy.TYPE.DEV)
            } else {
                //refresh token directly
                refreshToken()
            }
        }, 1500)
        /*val path = EthereumExt.createETHWallet("123")
        if (path.isNotEmpty()) {
            val address = EthereumExt.getWalletAddress("123", path)
            if (address.isNotEmpty()) {
                WalletApplication.api.getTxParams(address, 3)
            }
        }*/
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkDeepLink()
    }

    private fun setActionListener() {
/*        WalletApplication.activityHandler.onMaxTimeExceeded = {
            //show pincode here
        }
        webView.onActionUp = {
            WalletApplication.activityHandler.handleActivity()
        }*/
    }

    private fun removeActionListener() {
        //webView.clearActionListener()
        //WalletApplication.activityHandler.clearExceedHandler()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.enableInspection()
        webView.webViewClient = ChromeClient()
        webView.webChromeClient = WebChromeClient()
        registerJSCallbacks()
    }

    private fun openQrScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startActivityForResult(Intent(this, QrReaderActivity::class.java), REQUEST_READ_KEY)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERM
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openQrScanner()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_READ_KEY) {
                val resultData = data?.getStringExtra("data") ?: ""
                processQRReaderResult(resultData)
            }
        }
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
                { name, pas, cur, code ->
                    createNewAddress(name, pas, cur, code)
                },
                //create transaction
                { p1, p2, p3, p4, p5, p6 ->
                    createTransaction(p1, p2, p3, p4, p5, p6, Constants.TYPE_TMH.toString())
                },
                //create transaction new
                { p1, p2, p3, p4, p5, p6, p7 ->
                    createTransaction(p1, p2, p3, p4, p5, p6, p7)
                },
                //logout
                { logout() },
                // register
                { login, pass -> register(login, pass) },
                //set only local wallets
                { setOnlyLocal(it) },
                //get only local parameter
                { WalletApplication.dbHelper.isOnlyLocalWallets() },
                //method to get private key
                { address, password -> getPrivateKeyByAddress(address, password, true) },
                //get app version
                { BuildConfig.VERSION_NAME },
                //start reading qr code
                { fromUI({ openQrScanner() }) },
                //create wallet from import
                { a, k, p, c, code, n ->
                    importWalletFromParams(k, p, c, code, n)
                },
                //clear cache
                {
                    fromUI({
                        webView.clearCache(true)
                        fromUI({ webView.reload() }, 200)
                    })
                },
                { p, c, cc, n -> importPrivateWallet(p, c, cc, n) },
                { address, password -> getPrivateKeyByAddress(address, password, false) },
                { WalletApplication.dbHelper.getLanguage() },
                { saveLanguage(it) }
            ),
            Constants.JS_BRIDGE)
    }

    private fun checkDeepLink() {
        val params = DeepLinkResolver.parseDeepLink(intent) ?: return
        when (params) {
            is TransactionParams -> webView.loadUrl(TransactionLinkBuilder.createLink(params))
        }
    }

    private fun logout() {
        unsubscribe()
        removeActionListener()
        WalletApplication.dbHelper.clearAll()
    }

    private fun setOnlyLocal(onlyLocal: Boolean) {
        WalletApplication.dbHelper.setOnlyLocalWallets(onlyLocal)
    }

    private fun saveLanguage(language: String) {
        WalletApplication.dbHelper.saveLanguage(language)
    }

    private fun login(login: String, password: String) {
        addSubscription(WalletApplication.api.login(login, password)
            .subscribe(
                {
                    if (it.isOk()) {
                        setActionListener()
                        WalletApplication.dbHelper
                            .setToken(it.data.token)
                        WalletApplication.dbHelper
                            .setRefreshToken(it.data.refreshToken)
                        WalletApplication.dbHelper
                            .setLogin(login)
                        JsFunctionCaller.callFunction(
                            webView,
                            JsFunctionCaller.FUNCTION.LOGINRESULT,
                            "", ""
                        )
                        checkUnsynchronizedWallets()
                        startBalancesObserving()
                    }
                },
                {
                    handleLoginResponseError(it, webView)
                }
            ))
    }

    private fun register(login: String, password: String) {
        addSubscription(WalletApplication.api.register(login, password)
            .subscribe(
                {
                    if (it.isOk()) {
                        JsFunctionCaller.callFunction(
                            webView,
                            JsFunctionCaller.FUNCTION.REGISTERRESULT,
                            "", ""
                        )
                        login(login, password)
                    }
                },
                {
                    handleRegisterResponseError(it, webView)
                }
            ))
    }

    private fun ping(type: Proxy.TYPE) {
        val obs = object : DisposableObserver<String>() {
            override fun onComplete() {
                if (type == Proxy.TYPE.PROD) {
                    val info = WalletApplication.gson.toJson(ResolvingResult(type, ResolvingInfo(3)))
                    JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.UPDATERESOLVING, info
                    )
                    WalletApplication.api.saveProxy()
                    refreshToken()
                } else {
                    WalletApplication.api.saveProxy()
                    ping(Proxy.TYPE.PROD)
                }
            }

            override fun onNext(t: String) {
                JsFunctionCaller.callFunction(
                    webView,
                    JsFunctionCaller.FUNCTION.UPDATERESOLVING, t
                )
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
                JsFunctionCaller.callFunction(
                    webView,
                    JsFunctionCaller.FUNCTION.ONIPREADY, false
                )
            }
        }
        addSubscription(
            WalletApplication.api.ping(type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(obs)
        )
    }

    private fun refreshToken() {
        if (WalletApplication.dbHelper.hasToken()) {
            //refresh token and return token
            addSubscription(WalletApplication.api.refreshToken()
                .subscribe(
                    {
                        if (it.isOk()) {
                            setActionListener()
                            WalletApplication.dbHelper
                                .setToken(it.data.token)
                            WalletApplication.dbHelper
                                .setRefreshToken(it.data.refreshToken)

                            JsFunctionCaller.callFunction(
                                webView,
                                JsFunctionCaller.FUNCTION.ONIPREADY, true
                            )
                            checkUnsynchronizedWallets()
                            startBalancesObserving()
                            checkDeepLink()
                            val w = WalletApplication.dbHelper.getUserWallets().first()
                            deleteWalletFromLocal(w.address, w.password)
                        } else {
                            JsFunctionCaller.callFunction(
                                webView,
                                JsFunctionCaller.FUNCTION.ONIPREADY, false
                            )
                        }
                    },
                    {
                        JsFunctionCaller.callFunction(
                            webView,
                            JsFunctionCaller.FUNCTION.ONIPREADY, false
                        )
                        it.printStackTrace()
                    }
                ))
        } else {
            JsFunctionCaller.callFunction(
                webView,
                JsFunctionCaller.FUNCTION.ONIPREADY, false
            )
        }
    }

    private fun getWallets(currency: String) {
        if (currency == Constants.TYPE_MHC.toString()) {
            mMHCWalletsDisposable.dispose()
        } else {
            mTMHWalletsDisposable.dispose()
        }
        WalletApplication.api.getAllWalletsAndBalance(
            currency, WalletApplication.dbHelper.isOnlyLocalWallets()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.WALLETSRESULT, it, currency
                    )
                },
                {
                    handleCommonError(it, webView)
                }
            )
            .apply {
                if (currency == Constants.TYPE_MHC.toString()) {
                    mMHCWalletsDisposable = this
                } else {
                    mTMHWalletsDisposable = this
                }
                addSubscription(this)
            }
    }

    private fun getHistory(currency: String) {
        addSubscription(WalletApplication.api.getHistory(
            currency, WalletApplication.dbHelper.isOnlyLocalWallets()
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.HISTORYRESULT, it, currency
                    )
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
            wallet.userLogin = WalletApplication.dbHelper.getLogin()
            wallet.password = password
            //save wallet
            WalletApplication.dbHelper.setUserWallet(wallet)
            //sync wallet
            syncWallet(
                wallet.address,
                CryptoExt.publicKeyToHex(wallet.publicKey),
                wallet.currency.toInt(),
                wallet.name
            )
            fromUI({
                JsFunctionCaller.callFunction(webView, JsFunctionCaller.FUNCTION.NEWWALLERRESULT, wallet.address)
            })
        } else {
            fromUI({
                JsFunctionCaller.callFunction(webView, JsFunctionCaller.FUNCTION.NEWWALLERRESULT, "CREATE_WALLET_ERROR")
            })
        }
    }

    private fun createTransaction(
        from: String, password: String, to: String,
        amount: String, fee: String, data: String, currency: String
    ) {
        val wallet = WalletApplication.dbHelper.getUserWalletByAddress(from, WalletApplication.dbHelper.getLogin())
        if (wallet == null) {
            //send error here
            JsFunctionCaller.callFunction(
                webView,
                JsFunctionCaller.FUNCTION.TRASACTIONRESULT, "NO_PRIVATE_KEY_FOUND"
            )
            return
        }
        if (wallet.password != password) {
            JsFunctionCaller.callFunction(
                webView,
                JsFunctionCaller.FUNCTION.TRASACTIONRESULT, "WRONG_PASSWORD"
            )
            return
        }
        addSubscription(WalletApplication.api.getBalance(wallet.address, wallet.currency.toInt())
            .observeOn(Schedulers.computation())
            .flatMap {
                val nonce = it.result.countSpent + 1
                val tx = CryptoExt.createTransaction(wallet, to, password, nonce.toString(), amount, fee, data)
                fromUI({
                    val res = WalletApplication.api.mapTxResultToString(CreateTxResult(1))
                    JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.TXINFORESULT, res
                    )
                })
                val string = WalletApplication.gson.toJson(tx)
                Log.d("MIINE", "tx: $string")
                WalletApplication.api.createTransaction(tx, wallet.currency.toInt())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val res = WalletApplication.api.mapTxResultToString(it)
                    JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.TXINFORESULT, res
                    )
                    if (it.isProxyReady()) {
                        startTrxCheck(it, wallet.currency.toInt())
                    }

                },
                {
                    handleCommonError(it, webView)
                }
            ))
    }

    private fun startTrxCheck(result: CreateTxResult, currencyId: Int) {
        val obs = object : DisposableObserver<CreateTxResult>() {
            override fun onComplete() {}

            override fun onNext(result: CreateTxResult) {
                //update status
                val res = WalletApplication.api.mapTxResultToString(result)
                JsFunctionCaller.callFunction(
                    webView,
                    JsFunctionCaller.FUNCTION.TXINFORESULT, res
                )
                if (result.isTorrentSuccessful()) {
                    dispose()
                }
            }

            override fun onError(e: Throwable) {
                handleCommonError(e, webView)
            }
        }
        addSubscription(
            WalletApplication.api.getTxInfo(result, MAX_INFO_TRY_COUNT, currencyId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(obs)
        )
    }

    private fun checkUnsynchronizedWallets() {
        val list = WalletApplication.dbHelper.getUnsynchonizedWallets(WalletApplication.dbHelper.getLogin())
        list.forEach {
            syncWallet(
                it.address,
                CryptoExt.publicKeyToHex(it.publicKey),
                it.currency.toInt(),
                it.name
            )
        }
    }

    private fun syncWallet(
        address: String,
        pubKey: String,
        currency: Int,
        walletName: String
    ) {
        addSubscription(WalletApplication.api.syncWallet(address, pubKey, currency, walletName)
            .subscribe(
                {
                    if (it.isOk()) {
                        //update wallet
                        WalletApplication.dbHelper.setWalletSynchronized(
                            address,
                            WalletApplication.dbHelper.getLogin()
                        )
                    }
                },
                {
                    if (it is ResponseError) {
                        handleCommonError(it, webView)
                        if (it.code.contains("exists", true) ||
                            it.message?.contains("exists", true) == true
                        ) {
                            //update wallet
                            WalletApplication.dbHelper.setWalletSynchronized(
                                address,
                                WalletApplication.dbHelper.getLogin()
                            )
                        }
                    }
                    it.printStackTrace()
                }
            ))
    }

    private fun startBalancesObserving() {
/*        addSubscription(
            WalletApplication.api.startBalanceObserving(Constants.TYPE_MHC)
                .subscribe(
                    {
                        JsFunctionCaller.callFunction(
                            webView,
                            JsFunctionCaller.FUNCTION.ONDATACHANGED
                        )
                    },
                    {
                        it.printStackTrace()
                    }
                ))
        addSubscription(
            WalletApplication.api.startBalanceObserving(Constants.TYPE_TMH)
                .subscribe(
                    {
                        JsFunctionCaller.callFunction(
                            webView,
                            JsFunctionCaller.FUNCTION.ONDATACHANGED
                        )
                    },
                    {
                        it.printStackTrace()
                    }
                ))*/
    }

    private fun getPrivateKeyByAddress(address: String, password: String, encrypted: Boolean): String {
        val wallet = WalletApplication.dbHelper.getUserWalletByAddress(
            address,
            WalletApplication.dbHelper.getLogin()
        )
        return if (encrypted) {
            val result = when {
                wallet == null -> ""
                wallet.password != password -> ""
                else -> PrivateWalletHelper.encryptWalletPrivateKey(wallet.privateKeyPKCS1, password)
            }
            result
        } else {
            getPrivateKeyDecrypted(wallet, password)
        }
    }

    private fun getPrivateKeyDecrypted(wallet: Wallet?, password: String): String {
        return when {
            wallet == null -> JsResultHelper.getPrivateKeyDecryptedResult("", "NO_WALLET")
            wallet.password != password -> JsResultHelper.getPrivateKeyDecryptedResult("", "ERROR_INCORRECT_PASSWORD")
            else -> JsResultHelper.getPrivateKeyDecryptedResult(CryptoExt.publicKeyToHex(wallet.privateKeyPKCS1), "OK")
        }
    }

    private fun importWalletByPrivateKey(key: String) {
        val bytes = key.toUpperCase().hexStringToByteArray()
        val wallet = CryptoExt.createWalletFromPrivateKey(bytes)
        if (wallet != null) {
            val pub = CryptoExt.publicKeyToHex(wallet.publicKey)
            val priv = CryptoExt.publicKeyToHex(wallet.privateKeyPKCS1)
            JsFunctionCaller.callFunction(
                webView,
                JsFunctionCaller.FUNCTION.SAVEIMPORTEDWALLET, key, wallet.address, ""
            )
        }
    }

    private fun importWalletFromParams(
        privKey: String,
        password: String, currency: String,
        code: String, name: String
    ) {
        val bytes = privKey.toUpperCase().hexStringToByteArray()
        val wallet = CryptoExt.createWalletFromPrivateKey(bytes)
        if (wallet != null) {
            wallet.name = name
            wallet.currency = currency
            wallet.code = code
            wallet.userLogin = WalletApplication.dbHelper.getLogin()
            wallet.password = password

            saveOrUpdateWallet(wallet)

            fromUI({
                JsFunctionCaller.callFunction(webView, JsFunctionCaller.FUNCTION.IMPORTRESULT, wallet.address)
            })
        }
    }

    private fun renameWallet(
        address: String,
        newName: String,
        password: String
    ) {
        val userWallet =
            WalletApplication.dbHelper.getUserWalletByAddress(
                address,
                WalletApplication.dbHelper.getLogin()
            )
        if (userWallet != null) {
            if (password == userWallet.password) {
                userWallet.name = newName
                WalletApplication.dbHelper.updateUserWallet(
                    userWallet,
                    WalletApplication.dbHelper.getLogin()
                )
                setWalletName(address, userWallet.currency.toInt(), newName)
            }
        }
    }

    private fun deleteWalletFromLocal(
        address: String,
        password: String
    ) {
        val userWallet =
            WalletApplication.dbHelper.getUserWalletByAddress(
                address,
                WalletApplication.dbHelper.getLogin()
            )
        if (userWallet != null) {
            if (password == userWallet.password) {
                WalletApplication.dbHelper.deleteUserWalletByAddress(
                    address,
                    WalletApplication.dbHelper.getLogin()
                )
                setWalletSync(address, userWallet.currency.toInt(), false)
            }
        }
    }

    private fun setWalletSync(
        address: String,
        currency: Int,
        sync: Boolean
    ) {
        addSubscription(WalletApplication.api.setWalletSync(
            address, currency, sync
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.isOk()) {

                    }
                },
                {
                    it.printStackTrace()
                }
            )
        )
    }

    private fun importPrivateWallet(
        password: String, currency: String,
        currencyCode: String, name: String
    ): String {
        val privateWallet = PrivateWalletHelper.createWalletFromPrivateKey(password)
        return if (privateWallet != null) {
            privateWallet.name = name
            privateWallet.currency = currency
            privateWallet.code = currencyCode
            privateWallet.userLogin = WalletApplication.dbHelper.getLogin()
            privateWallet.password = password

            saveOrUpdateWallet(privateWallet)
            JsResultHelper.importPrivateWalletResult(privateWallet.address, "OK")
        } else {
            JsResultHelper.importPrivateWalletResult("", "INCORRECT_KEY")
        }
    }

    private fun saveOrUpdateWallet(wallet: Wallet) {
        //check if such a wallet exists
        val userWallet =
            WalletApplication.dbHelper.getUserWalletByAddress(
                wallet.address,
                WalletApplication.dbHelper.getLogin()
            )
        if (userWallet == null) {
            //save wallet
            WalletApplication.dbHelper.setUserWallet(wallet)
            //sync wallet
            syncWallet(
                wallet.address,
                CryptoExt.publicKeyToHex(wallet.publicKey),
                wallet.currency.toInt(),
                wallet.name
            )
        } else {
            WalletApplication.dbHelper.updateUserWallet(wallet, WalletApplication.dbHelper.getLogin())
        }
    }

    private fun showLoadingOrError(show: Boolean = true, loading: Boolean = true) {
        if (!show) {
            vLoading.makeGone()
            return
        }

        vLoading.makeVisible()
        if (loading) {
            tvLoading.text = getString(R.string.loading)
            tvError.makeGone()
        } else {
            tvLoading.text = getString(R.string.loading_error)
            tvError.makeVisible()
        }
    }

    private fun processQRReaderResult(resultData: String) {
        if (resultData.isEmpty()) {
            //error while reading qr
            JsFunctionCaller.callFunction(
                webView,
                JsFunctionCaller.FUNCTION.SAVEIMPORTEDWALLET,
                "",
                "",
                getString(R.string.error_rq_code_read)
            )
            return
        }
        val result = KeyFormatter.formatKey(resultData)
        when {
            KeyFormatter.isEncryptedFormat(result) -> {
                PrivateWalletHelper.privateKeyInfo = result
                JsFunctionCaller.callFunction(
                    webView,
                    JsFunctionCaller.FUNCTION.SAVEIMPORTEDWALLET,
                    "",
                    "",
                    getString(R.string.error_encrypted_private_key)
                )
            }
            !KeyFormatter.isKeyFormat(result) -> {
                JsFunctionCaller.callFunction(
                    webView,
                    JsFunctionCaller.FUNCTION.SAVEIMPORTEDWALLET,
                    "",
                    "",
                    getString(R.string.error_invalid_private_key)
                )
            }
            KeyFormatter.isSECP256k1(result) -> importWalletByPrivateKey(result)
            else -> {
                JsFunctionCaller.callFunction(
                    webView,
                    JsFunctionCaller.FUNCTION.SAVEIMPORTEDWALLET,
                    "",
                    "",
                    getString(R.string.error_wrong_rq_code_format)
                )
            }
        }
    }

    private fun setWalletName(
        address: String,
        currency: Int,
        walletName: String
    ) {
        addSubscription(WalletApplication.api.setWalletName(
            address, currency, walletName
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.isOk()) {

                    }
                },
                {
                    it.printStackTrace()
                }
            )
        )
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private inner class ChromeClient : WebViewClient() {

        private var mHasErrors = false
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (!mHasErrors) {
                fromUI({ showLoadingOrError(show = false) })
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mHasErrors = false
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            mHasErrors = true
            if (isConnected().not()) {
                fromUI({ showLoadingOrError(loading = false) })
            }
        }

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            mHasErrors = true
            if (isConnected().not()) {
                fromUI({ showLoadingOrError(loading = false) })
            }
        }
    }
}