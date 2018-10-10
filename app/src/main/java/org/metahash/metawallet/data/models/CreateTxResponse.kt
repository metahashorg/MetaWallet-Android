package org.metahash.metawallet.data.models

import java.util.*

data class CreateTxResponse(
        val error: String?,
        val params: String?,
        val status: TXSTATUS = TXSTATUS.WAIT
) : BaseResponse() {

    fun isSuccessful() = params != null && error == null

    constructor() : this(null, null, TXSTATUS.OK)
    constructor(status: TXSTATUS) : this(null, null, status)

    companion object {

        fun error() = CreateTxResponse(TXSTATUS.ERROR)

        fun wait() = CreateTxResponse(TXSTATUS.WAIT)
    }
}

data class CreateTxResult(
        val id: String,
        val stage: Int,
        val proxy: Array<String>,
        val torrent: Array<String>) {

    constructor(stage: Int) : this ("", stage, arrayOf("wait", "wait", "wait"), arrayOf("wait", "wait", "wait"))

    fun isProxyReady() = proxy.count { it.equals(TXSTATUS.WAIT.toString(), true) } == 0

    fun isTorrentReady() = torrent.count { it.equals(TXSTATUS.WAIT.toString(), true) } == 0

    fun isTorrentSuccessful() = torrent.count { it.equals(TXSTATUS.OK.toString(), true) } == torrent.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateTxResult

        if (id != other.id) return false
        if (stage != other.stage) return false
        if (!Arrays.equals(proxy, other.proxy)) return false
        if (!Arrays.equals(torrent, other.torrent)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + stage
        result = 31 * result + Arrays.hashCode(proxy)
        result = 31 * result + Arrays.hashCode(torrent)
        return result
    }

}

enum class TXSTATUS {
    WAIT,
    OK,
    ERROR
}