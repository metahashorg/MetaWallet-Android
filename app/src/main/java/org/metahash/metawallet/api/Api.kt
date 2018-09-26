package org.metahash.metawallet.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import org.metahash.metawallet.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface Api {

    @POST
    fun login(@Url url: String, @Body body: ServiceRequest): Observable<LoginResponse>

    @POST
    fun getUserWallets(@Url url: String, @Body body: ServiceRequest): Observable<WalletsResponse>

    @POST
    fun getWalletBalance(@Url url: String, @Body body: ServiceRequest): Observable<BalanceResponse>

    @POST
    fun refreshToken(@Url url: String, @Body body: ServiceRequest): Observable<RefreshResponse>

    @POST
    fun getWalletHistory(@Url url: String, @Body body: ServiceRequest): Observable<HistoryResponse>
}