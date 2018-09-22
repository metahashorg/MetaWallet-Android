package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import retrofit2.Response

class AllWalletsCmd(
        private val api: Api
) : BaseCommand<Response<ResponseBody>>() {

    var currency: String? = null

    override fun serviceRequest(): Observable<Response<ResponseBody>> {
        return api
                .getUserWallets(Constants.BASE_URL_WALLET,
                        ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.ALLWALLETS,
                        ServiceRequestFactory.getAllWalletsParams(currency)))
    }
}