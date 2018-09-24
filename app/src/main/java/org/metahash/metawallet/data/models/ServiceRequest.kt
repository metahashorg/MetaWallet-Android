package org.metahash.metawallet.data.models

import com.google.gson.JsonArray

private val DEFAULT_ID = 1
private val DEFAULT_UID = ""
private val DEFAULT_VERSION = "1.0.0"
private val DEFAULT_TOKEN = ""

data class ServiceRequest(
        val id: Int = DEFAULT_ID,
        val uid: String = DEFAULT_UID,
        val version: String = DEFAULT_VERSION,
        val token: String = DEFAULT_TOKEN,
        val method: String? = null,
        val params: Any? = null
)