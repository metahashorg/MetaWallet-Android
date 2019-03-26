package org.metahash.metawallet.presentation.base.deeplink.queryparsers

import org.metahash.metawallet.presentation.base.deeplink.LinkParam

interface QueryParser {

    fun parse(query: String): List<LinkParam>
}