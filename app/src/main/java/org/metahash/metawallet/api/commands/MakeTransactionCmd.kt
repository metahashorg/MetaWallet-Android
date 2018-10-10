package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.BalanceResponse
import retrofit2.Response

class MakeTransactionCmd(
        private val api: Api
) : BaseCommand<Response<ResponseBody>>() {

    var to = ""
    var value = ""
    var fee = ""
    var nonce = ""
    var data = ""
    var pubKey = ""
    var sign = ""

    override fun serviceRequest(): Observable<Response<ResponseBody>> {
        return api
                .makeTransaction(getProxyAddress(),
                        ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.MAKETRANSACTION,
                        ServiceRequestFactory.getTransactionParams(to, value, fee, nonce, data, pubKey, sign)))
    }
}