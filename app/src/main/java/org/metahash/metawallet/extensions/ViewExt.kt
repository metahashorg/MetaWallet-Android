package org.metahash.metawallet.extensions

import android.os.Build
import android.view.View
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import org.metahash.metawallet.BuildConfig

//enable chrome inspection
fun WebView.enableInspection(debugOnly: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        if (BuildConfig.DEBUG || debugOnly.not()) {
            //enable for debug only
            setWebContentsDebuggingEnabled(true)
        }
    }
}

fun View.makeVisible() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.makeGone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}