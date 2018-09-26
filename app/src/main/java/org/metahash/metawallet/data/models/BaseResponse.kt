package org.metahash.metawallet.data.models

import java.io.Serializable

open class BaseResponse(
        private val result: String = "",
        val version: String = "",
        val id: Int = 0
) : Serializable {

    fun isOk() = result.equals("ok", true)
}

open class BaseDecenterResponse(
        val id: Int = 0
) : Serializable