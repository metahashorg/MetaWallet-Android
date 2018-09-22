package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.LoginResponse

class LoginCmd(
        private val api: Api
) : BaseCommand<LoginResponse>() {

    var login: String = ""
    var password: String = ""

    override fun serviceRequest(): Observable<LoginResponse> {
        return api
                .login(Constants.BASE_URL, ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.LOGIN,
                        ServiceRequestFactory.getLoginParams(login, password)))
    }
}