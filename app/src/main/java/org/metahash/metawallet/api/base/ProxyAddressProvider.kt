package org.metahash.metawallet.api.base

import org.metahash.metawallet.Constants.PROXY_PORT
import org.metahash.metawallet.Constants.TORRENT_PORT
import org.metahash.metawallet.WalletApplication

interface ProxyAddressProvider {

    fun getTorrentAddress(): String {
        val address = WalletApplication.dbHelper.getTorrent().ip + ":$TORRENT_PORT"
        return if (address.startsWith("http")) {
            address
        } else {
            "http://$address"
        }
    }

    fun getProxyAddress(): String {
        val address = WalletApplication.dbHelper.getProxy().ip + ":$PROXY_PORT"
        return if (address.startsWith("http")) {
            address
        } else {
            "http://$address"
        }
    }

    fun formatProxy(ip: String): String {
        val address = "$ip:$PROXY_PORT"
        return if (address.startsWith("http")) {
            address
        } else {
            "http://$address"
        }
    }

    fun formatTorrent(ip: String): String {
        val address = "$ip:$TORRENT_PORT"
        return if (address.startsWith("http")) {
            address
        } else {
            "http://$address"
        }
    }
}