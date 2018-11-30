package org.metahash.metawallet.presentation.base

interface AppLifecycleProvider {
    fun onAppStart()
    fun onAppStop()
}