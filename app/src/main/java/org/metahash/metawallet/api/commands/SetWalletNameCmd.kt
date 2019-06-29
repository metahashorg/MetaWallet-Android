package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.BaseResponse

class SetWalletNameCmd(
        private val api: Api
) : BaseCommand<BaseResponse>() {

    var currency = -1
    var address = ""
    var name = ""

    override fun serviceRequest(): Observable<BaseResponse> {
        return api
                .setWalletName(Constants.BASE_URL_WALLET, ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.SETWALLETNAME,
                        ServiceRequestFactory.getSetWalletNameParams(address, currency, name)))
    }
}