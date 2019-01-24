package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.GetTxParamsResponse

class GetTxParamsCmd(private val api: Api) : BaseCommand<GetTxParamsResponse>() {

    var address = ""
    var currency = -1

    override fun serviceRequest(): Observable<GetTxParamsResponse> {
        return api.getTxParams(Constants.BASE_URL_WALLET, ServiceRequestFactory.getRequestData(
                ServiceRequestFactory.REQUESTTYPE.TXPARAMS,
                ServiceRequestFactory.getTxParamsParams(address, currency)
        ))
    }
}