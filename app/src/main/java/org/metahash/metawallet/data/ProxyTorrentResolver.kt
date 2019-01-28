package org.metahash.metawallet.data

import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.Proxy

class ProxyTorrentResolver {

    companion object {
        //for torrents
        fun torrentResolver(currencyId: Int): Proxy {
            return if (currencyId == Constants.TYPE_MHC) {
                WalletApplication.dbHelper.getTorrent(Proxy.TYPE.PROD)
            } else {
                WalletApplication.dbHelper.getTorrent()
            }
        }

        fun torrentListResolver(currencyId: Int): List<Proxy> {
            return if (currencyId == Constants.TYPE_MHC) {
                WalletApplication.dbHelper.getAllTorrent(Proxy.TYPE.PROD)
            } else {
                WalletApplication.dbHelper.getAllTorrent()
            }
        }

        //for proxy
        fun proxyResolver(currencyId: Int): Proxy {
            return if (currencyId == Constants.TYPE_MHC) {
                WalletApplication.dbHelper.getProxy(Proxy.TYPE.PROD)
            } else {
                WalletApplication.dbHelper.getProxy()
            }
        }

        fun proxyListResolver(currencyId: Int): List<Proxy> {
            return if (currencyId == Constants.TYPE_MHC) {
                WalletApplication.dbHelper.getAllProxy(Proxy.TYPE.PROD)
            } else {
                WalletApplication.dbHelper.getAllProxy()
            }
        }

    }
}