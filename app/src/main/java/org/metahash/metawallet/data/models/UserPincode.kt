package org.metahash.metawallet.data.models

import java.io.Serializable

data class UserPincode(
        var username: String,
        var pincode: String?
) : Serializable {

    constructor(username: String) : this(username, "")

    fun hasPincode() = pincode.isNullOrEmpty().not()
}