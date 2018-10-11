package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.RegisterResponse

class RegisterCmd(
        private val api: Api
) : BaseCommand<RegisterResponse>() {

    var login: String = ""
    var password: String = ""

    override fun serviceRequest(): Observable<RegisterResponse> {
        return api
                .register(Constants.BASE_URL, ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.REGISTER,
                        ServiceRequestFactory.getRegisterParams(login, password)))
    }
}