package org.metahash.metawallet

object Constants {

    const val WEB_URL = "https://mgapp.metahash.io/"

    //prod for decenter ip resolving
    const val URL_PROXY = "proxy.net-main.metahashnetwork.com"
    const val URL_TORRENT = "tor.net-main.metahashnetwork.com"

    //dev for decenter ip resolving
    const val URL_PROXY_DEV = "proxy.net-dev.metahashnetwork.com"
    const val URL_TORRENT_DEV = "tor.net-dev.metahashnetwork.com"

    //for login
    const val BASE_URL = "https://id.metahash.org/api/"
    const val BASE_URL_DEV = "http://id-dev.metahash.local/api/"

    //for wallet operations
    const val BASE_URL_WALLET = "https://wallet.metahash.org/api/"

    const val JS_BRIDGE = "androidJsBridge"

    const val TORRENT_PORT = "5795"
    const val PROXY_PORT = "9999"

    const val TYPE_TMH = 1
    const val TYPE_MHC = 4
}