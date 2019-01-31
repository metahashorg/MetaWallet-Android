package org.metahash.metawallet.data.models

private const val DEFAULT_ID = 1
private const val DEFAULT_UID = ""
private const val DEFAULT_VERSION = "1.0.0"
private const val DEFAULT_TOKEN = ""

data class ServiceRequest(
        val id: Int = DEFAULT_ID,
        val uid: String = DEFAULT_UID,
        val version: String = DEFAULT_VERSION,
        val token: String = DEFAULT_TOKEN,
        val method: String? = null,
        val params: Any? = null,
        val jsonrpc: String? = null
)