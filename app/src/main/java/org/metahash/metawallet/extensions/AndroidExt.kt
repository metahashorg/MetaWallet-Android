package org.metahash.metawallet.extensions

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager


fun Activity.fromUI(action: () -> Unit, delay: Long = 0) {
    if (delay == 0L) {
        Handler(Looper.getMainLooper()).post(action)
    } else if (delay > 0) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }
}

fun Context.isConnected(): Boolean {

    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = cm.activeNetworkInfo
    return info != null && info.isConnected
}