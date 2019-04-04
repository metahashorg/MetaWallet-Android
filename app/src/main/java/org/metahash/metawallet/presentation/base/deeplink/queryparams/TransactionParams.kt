package org.metahash.metawallet.presentation.base.deeplink.queryparams

import org.metahash.metawallet.presentation.base.deeplink.LinkParam

private const val PARAM_TO = "to"
private const val PARAM_VALUE = "value"
private const val PARAM_CURRENCY = "currency"

class TransactionParams(params: List<LinkParam>) : BaseParams(params)