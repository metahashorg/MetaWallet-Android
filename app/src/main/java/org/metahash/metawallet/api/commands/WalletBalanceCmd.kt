package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.BalanceResponse
import retrofit2.Response

class WalletBalanceCmd(
        private val api: Api
) : BaseCommand<BalanceResponse>() {

    var address: String = ""
    var currency = 0

    override fun serviceRequest(): Observable<BalanceResponse> {
        return api
                .getWalletBalance(getTorrentAddress(currency),
                        ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.WALLETBALANCE,
                        ServiceRequestFactory.getBalanceParams(address)))
    }
}