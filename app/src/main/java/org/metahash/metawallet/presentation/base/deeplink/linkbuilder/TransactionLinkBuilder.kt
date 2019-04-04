package org.metahash.metawallet.presentation.base.deeplink.linkbuilder

import android.util.Log
import org.metahash.metawallet.Constants
import org.metahash.metawallet.presentation.base.deeplink.queryparams.TransactionParams

object TransactionLinkBuilder {

    fun createLink(params: TransactionParams): String {
        val builder = StringBuilder("${Constants.WEB_URL}#/transfer-request")
        params.params.forEachIndexed { index, value ->
            if (index == 0) {
                builder.append("?")
            } else {
                builder.append("&")
            }
            builder.append("${value.name}=${value.value}")
        }
        val a = builder.toString()
        Log.d("MIINE", "link: $a")
        return builder.toString()
    }
}