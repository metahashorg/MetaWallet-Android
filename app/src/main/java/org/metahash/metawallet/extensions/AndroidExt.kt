package org.metahash.metawallet.extensions

import android.app.Activity
import android.os.Handler
import android.os.Looper

fun Activity.fromUI(action: () -> Unit, delay: Long = 0) {
    if (delay == 0L) {
        Handler(Looper.getMainLooper()).post(action)
    } else if (delay > 0) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }
}