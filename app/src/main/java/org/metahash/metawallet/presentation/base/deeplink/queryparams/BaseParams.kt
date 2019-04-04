package org.metahash.metawallet.presentation.base.deeplink.queryparams

import org.metahash.metawallet.presentation.base.deeplink.LinkParam

abstract class BaseParams(val params: List<LinkParam>) {

    fun getByName(name: String) = params.firstOrNull { it.name == name }
}