package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.api.base.BaseCommandWithMapping
import org.metahash.metawallet.data.models.GetTxInfoResponse
import org.metahash.metawallet.data.models.TXSTATUS

class GetTxInfoCmd(
        private val api: Api
) : BaseCommandWithMapping<GetTxInfoResponse, GetTxInfoResponse>() {

    var txHash = ""

    var baseTorrentUrl = ""

    override fun serviceRequest(): Observable<GetTxInfoResponse> {
        return api
                .getTxInfo(baseTorrentUrl,
                        ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.TXINFO,
                        ServiceRequestFactory.getTxInfoParams(txHash)))
    }

    override fun afterResponse(response: Observable<GetTxInfoResponse>): Observable<GetTxInfoResponse> {
        return response
                .map {
                    val result = if (it.isSuccessful()) {
                        it.copy(status = TXSTATUS.OK)
                    } else {
                        it.copy(status = TXSTATUS.ERROR)
                    }
                    result
                }
    }
}