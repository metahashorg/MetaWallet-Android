package org.metahash.metawallet.presentation.base.deeplink.queryparams

import org.metahash.metawallet.presentation.base.deeplink.LinkParam

private const val PARAM_TO = "to"
private const val PARAM_VALUE = "value"

data class TransactionParams(val params: List<LinkParam>) : BaseParams {

    fun getTo() = params.firstOrNull { it.name == PARAM_TO }?.value ?: ""

    fun getValue() = params.firstOrNull { it.name == PARAM_VALUE }?.value ?: ""
}