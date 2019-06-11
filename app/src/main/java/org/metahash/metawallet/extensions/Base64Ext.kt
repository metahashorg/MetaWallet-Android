package org.metahash.metawallet.extensions

import android.util.Base64

fun String.toBase64(): String {
    return String(Base64.encode(toByteArray(Charsets.UTF_8), Base64.DEFAULT))
}

fun String.fromBase64(): String {
    return String(Base64.decode(toByteArray(Charsets.UTF_8), Base64.DEFAULT))
}