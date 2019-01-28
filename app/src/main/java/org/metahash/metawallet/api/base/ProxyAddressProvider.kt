package org.metahash.metawallet.api.base

import org.metahash.metawallet.Constants.PROXY_PORT
import org.metahash.metawallet.Constants.TORRENT_PORT
import org.metahash.metawallet.data.ProxyTorrentResolver

interface ProxyAddressProvider {

    fun getTorrentAddress(currencyId: Int): String {
        val address = ProxyTorrentResolver.torrentResolver(currencyId).ip + ":$TORRENT_PORT"
        return if (address.startsWith("http")) {
            address
        } else {
            "http://$address"
        }
    }

    fun getProxyAddress(currencyId: Int): String {
        val address = ProxyTorrentResolver.proxyResolver(currencyId).ip + ":$PROXY_PORT"
        return if (address.startsWith("http")) {
            address
        } else {
            "http://$address"
        }
    }
}