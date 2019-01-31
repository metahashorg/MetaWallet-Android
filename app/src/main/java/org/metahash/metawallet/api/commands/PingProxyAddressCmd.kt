package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.extensions.formatProxy
import retrofit2.Response

class PingProxyAddressCmd(
        private val api: Api
) : BaseCommand<Response<ResponseBody>>() {

    var address = ""

    override fun serviceRequest(): Observable<Response<ResponseBody>> {
        return api.pingProxyAddress(address.formatProxy(),
                ServiceRequestFactory.getRequestData(ServiceRequestFactory.REQUESTTYPE.PINGPROXY, null))
    }
}