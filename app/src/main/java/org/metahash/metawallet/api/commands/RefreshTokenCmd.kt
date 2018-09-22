package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.RefreshResponse
import retrofit2.Response

class RefreshTokenCmd(
        private val api: Api
) : BaseCommand<RefreshResponse>() {

    override fun serviceRequest(): Observable<RefreshResponse> {
        return api
                .refreshToken(Constants.BASE_URL, ServiceRequestFactory.getRequestData(
                        ServiceRequestFactory.REQUESTTYPE.REFRESHTOKEN, null))
    }
}