package org.metahash.metawallet.data.models

import java.io.Serializable

data class ProxyData(
        val proxy: MutableList<Proxy>,
        val torrent: MutableList<Proxy>,
        var lastUpdateTime: Long
) : Serializable {

    constructor() : this(mutableListOf(), mutableListOf(), 0L)
}