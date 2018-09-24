package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import retrofit2.Response

class WalletHistoryCmd(
        private val api: Api
) : BaseCommand<Response<ResponseBody>>() {

    var address: String = ""

    override fun serviceRequest(): Observable<Response<ResponseBody>> {
        return api
                .getWalletHistory(getTorrentAddress(),
                        ServiceRequestFactory.getRequestData(
                                ServiceRequestFactory.REQUESTTYPE.WALLETHISTORY,
                                ServiceRequestFactory.getHistoryParams(address)))
    }

    fun executeWithCache() = execute()
            .map {
                it.body()?.string() ?: ""
            }
            .doOnNext {
                if (it.isNotEmpty()) {
                    WalletApplication.dbHelper.setRawWalletHistory(address, it)
                }
            }
            .startWith(WalletApplication.dbHelper.getRawWalletHistory(address))
}