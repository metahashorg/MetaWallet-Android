package org.metahash.metawallet.presentation.base.deeplink

import android.content.Intent
import org.metahash.metawallet.presentation.base.deeplink.queryparsers.PaymentQueryParser
import org.metahash.metawallet.presentation.base.deeplink.queryparsers.QueryParser

object DeepLinkResolver {

    private const val HOST_PAYMENT = "pay.metahash.org"

    fun parseDeepLink(intent: Intent): List<LinkParam> {
        val data = intent.data ?: return listOf()
        val host = data.host ?: return listOf()
        val query = data.query ?: ""
        return parseInternal(host, query)
    }

    private fun parseInternal(host: String, query: String): List<LinkParam> {
        return when(host) {
            HOST_PAYMENT -> parsePaymentLink(query, PaymentQueryParser())
            else -> listOf()
        }
    }

    private fun parsePaymentLink(query: String, parser: QueryParser): List<LinkParam> {
        return parser.parse(query)
    }
}