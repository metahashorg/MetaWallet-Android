package org.metahash.metawallet.api

import android.os.Build
import android.webkit.WebView

object JsFunctionCaller {

    fun callFunction(view: WebView, type: FUNCTION, vararg args: Any) {
        val function = buildFunction(type.fName, args)
        callInternal(view, function)
    }

    private fun buildFunction(name: String, args: Array<out Any>): String {
        val builder = StringBuilder(name)
        builder.append("(")
        args.withIndex().forEach {
            val needBracket = it.value is String
            if (needBracket) {
                builder.append("'")
            }
            builder.append(it.value)
            if (needBracket) {
                builder.append("'")
            }
            if (it.index < args.size - 1) {
                builder.append(",")
            }
        }
        builder.append(");")
        return builder.toString()
    }

    private fun callInternal(view: WebView, function: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(function, null)
        } else {
            view.loadUrl("javascript:$function")
        }
    }

    enum class FUNCTION(val fName: String) {
        LOGINRESULT("getAuthRequestResult"),
        NOINTERNET("onConnectionError"),
        ONIPREADY("onConnectionReady"),
        WALLETSRESULT("getWalletsDataResult"),
        HISTORYRESULT("getWalletsHistoryResult"),
        NEWWALLERRESULT("createAddressResult"),
        TRASACTIONRESULT("sendTMHTxResult"),
        TXINFORESULT("onTxChecked")
    }
}