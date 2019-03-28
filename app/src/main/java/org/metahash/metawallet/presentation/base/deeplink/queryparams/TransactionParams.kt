package org.metahash.metawallet.presentation.base.deeplink.queryparams

import org.metahash.metawallet.presentation.base.deeplink.LinkParam

private const val PARAM_TO = "to"
private const val PARAM_VALUE = "value"
private const val PARAM_CURRENCY = "currency"

class TransactionParams(params: List<LinkParam>) : BaseParams(params) {

    fun getTo() = getByName(PARAM_TO)?.value ?: ""

    fun getValue() = getByName(PARAM_VALUE )?.value ?: ""

    fun getCurrency() = getByName(PARAM_CURRENCY)?.value ?: "tmh"
}