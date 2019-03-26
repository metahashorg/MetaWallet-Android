package org.metahash.metawallet.presentation.base.deeplink.queryparsers

import org.metahash.metawallet.presentation.base.deeplink.LinkParam

class PaymentQueryParser : QueryParser {

    override fun parse(query: String): List<LinkParam> {
        val params = query.split("&")
        return params
                .map { it.split("=") }
                .filter { it.size == 2 }
                .map { param ->
                    LinkParam(param[0], param[1])
                }
    }
}