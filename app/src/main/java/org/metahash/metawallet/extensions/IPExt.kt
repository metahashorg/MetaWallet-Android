package org.metahash.metawallet.extensions

import org.metahash.metawallet.Constants
import org.metahash.metawallet.Constants.TORRENT_PORT

fun String.formatTorrent(): String {
    val address = "$this:$TORRENT_PORT"
    return if (address.startsWith("http")) {
        address
    } else {
        "http://$address"
    }
}

fun String.formatProxy(): String {
    val address = "$this:${Constants.PROXY_PORT}"
    return if (address.startsWith("http")) {
        address
    } else {
        "http://$address"
    }
}