package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.LoginResponse
import org.metahash.metawallet.data.models.SyncWalletResponse

class SyncWalletCmd(
        private val api: Api
) : BaseCommand<SyncWalletResponse>() {

    var currency = -1
    var address = ""
    var pubKey = ""
    var name = ""

    override fun serviceRequest(): Observable<SyncWalletResponse> {
        return api
                .syncWallet(Constants.BASE_URL_WALLET, ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.SYNCWALLET,
                        ServiceRequestFactory.getSyncWalletParams(address, pubKey, currency, name)))
    }
}