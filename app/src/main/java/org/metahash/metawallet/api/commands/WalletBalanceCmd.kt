package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import retrofit2.Response

class WalletBalanceCmd(
        private val api: Api
) : BaseCommand<Response<ResponseBody>>() {

    var address: String = ""

    override fun serviceRequest(): Observable<Response<ResponseBody>> {
        return api
                .getWalletBalance(getTorrentAddress(),
                        ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.WALLETBALANCE,
                        ServiceRequestFactory.getBalanceParams(address)))
    }
}