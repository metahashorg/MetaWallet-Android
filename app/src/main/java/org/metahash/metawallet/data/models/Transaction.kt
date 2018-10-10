package org.metahash.metawallet.data.models

data class Transaction(
        val to: String, val value: String,
        val fee: String, val nonce: String,
        val data: String, val pubKey: String,
        val sign: String) {

    constructor() : this("", "", "", "", "", "", "")
}