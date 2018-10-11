package org.metahash.metawallet.presentation.base

import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.metahash.metawallet.api.JsFunctionCaller
import org.metahash.metawallet.data.models.ResponseError

abstract class BaseActivity : AppCompatActivity() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    fun addSubscription(subscription: Disposable) {
        disposable.add(subscription)
    }

    fun unsubscribe() {
        disposable.clear()
    }

    override fun onDestroy() {
        unsubscribe()
        super.onDestroy()
    }

    override fun onPause() {
        if (isFinishing) {
            unsubscribe()
        }
        super.onPause()
    }

    fun handleCommonError(error: Throwable, webView: WebView) {
        if (error is ResponseError && error.isNetworkError()) {
            handleNetworkError(webView)
        }
    }

    fun handleLoginResponseError(error: Throwable, webView: WebView) {
        if (error is ResponseError) {
            if (error.isNetworkError()) {
                handleNetworkError(webView)
            } else {
                JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.LOGINRESULT,
                        error.code, error.msg)
            }
        }
    }

    fun handleRegisterResponseError(error: Throwable, webView: WebView) {
        if (error is ResponseError) {
            if (error.isNetworkError()) {
                handleNetworkError(webView)
            } else {
                JsFunctionCaller.callFunction(
                        webView,
                        JsFunctionCaller.FUNCTION.REGISTERRESULT,
                        error.code, error.msg)
            }
        }
    }

    private fun handleNetworkError(webView: WebView) {
        JsFunctionCaller.callFunction(
                webView,
                JsFunctionCaller.FUNCTION.NOINTERNET)
    }
}