package org.metahash.metawallet.presentation.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent

class AppLifecycleListener(private val appProvider: AppLifecycleProvider)
    : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun onMoveToForeground() {
        appProvider.onAppStart()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun onMoveToBackground() {
        appProvider.onAppStop()
    }
}