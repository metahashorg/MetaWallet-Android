package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.GetNodesListResponse

class GetNodesListCmd(
    private val api: Api
) : BaseCommand<GetNodesListResponse>() {

    var net = "main" // default net
    var short = true

    override fun serviceRequest(): Observable<GetNodesListResponse> {
        return api.getNodesList(
            Constants.BASE_NODES_URL,
            ServiceRequestFactory.getRequestData(
                ServiceRequestFactory.REQUESTTYPE.NODES_LIST,
                ServiceRequestFactory.getNodesListParams(net, short)
            )
        )
    }
}