package org.metahash.metawallet.data.models

open class BaseResponse(
        private val result: String = "",
        val version: String = "",
        val id: Int = 0
) {

    fun isOk() = result.equals("ok", true)
}