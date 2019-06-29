package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.BaseResponse

class SetWalletSyncCmd(
        private val api: Api
) : BaseCommand<BaseResponse>() {

    var currency = -1
    var address = ""
    var sync = false

    override fun serviceRequest(): Observable<BaseResponse> {
        return api
                .setWalletSync(Constants.BASE_URL_WALLET, ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.SETWALLETSYNC,
                        ServiceRequestFactory.getSetWalletSyncParams(address, currency, sync)))
    }
}