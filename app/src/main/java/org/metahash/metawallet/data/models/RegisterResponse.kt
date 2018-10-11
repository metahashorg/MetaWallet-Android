package org.metahash.metawallet.data.models


data class RegisterResponse(
        val data: RegisterData

) : BaseResponse()

data class RegisterData(
        val id: String
)
