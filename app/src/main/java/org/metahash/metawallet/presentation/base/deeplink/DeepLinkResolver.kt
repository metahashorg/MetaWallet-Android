package org.metahash.metawallet.presentation.base.deeplink

import android.content.Intent
import org.metahash.metawallet.presentation.base.deeplink.queryparams.BaseParams
import org.metahash.metawallet.presentation.base.deeplink.queryparams.TransactionParams
import org.metahash.metawallet.presentation.base.deeplink.queryparsers.PaymentQueryParser
import org.metahash.metawallet.presentation.base.deeplink.queryparsers.QueryParser

object DeepLinkResolver {

    private const val HOST_PAYMENT = "pay.metahash.org"

    fun parseDeepLink(intent: Intent): BaseParams? {
        val data = intent.data ?: return null
        val host = data.host ?: return null
        val query = data.query ?: ""
        return parseInternal(host, query)
    }

    private fun parseInternal(host: String, query: String): BaseParams? {
        return when(host) {
            HOST_PAYMENT -> parsePaymentLink(query, PaymentQueryParser())
            else -> null
        }
    }

    private fun parsePaymentLink(query: String, parser: QueryParser): TransactionParams {
        return TransactionParams(parser.parse(query))
    }
}