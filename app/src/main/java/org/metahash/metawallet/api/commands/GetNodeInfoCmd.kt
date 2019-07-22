package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.GetNodeInfoResponse

class GetNodeInfoCmd(
    private val api: Api
) : BaseCommand<GetNodeInfoResponse>() {

    var net = "main"
    var address = ""

    override fun serviceRequest(): Observable<GetNodeInfoResponse> {
        return api.getNodeInfo(
            Constants.BASE_NODES_URL,
            ServiceRequestFactory.getRequestData(
                ServiceRequestFactory.REQUESTTYPE.NODE_INFO,
                ServiceRequestFactory.getNodeInfoParams(net, address)
            )
        )
    }
}