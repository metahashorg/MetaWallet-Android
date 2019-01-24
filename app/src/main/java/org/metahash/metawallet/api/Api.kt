package org.metahash.metawallet.api

import io.reactivex.Observable
import org.metahash.metawallet.data.models.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface Api {

    @POST
    fun login(@Url url: String, @Body body: ServiceRequest): Observable<LoginResponse>

    @POST
    fun register(@Url url: String, @Body body: ServiceRequest): Observable<RegisterResponse>

    @POST
    fun syncWallet(@Url url: String, @Body body: ServiceRequest): Observable<SyncWalletResponse>

    @POST
    fun getUserWallets(@Url url: String, @Body body: ServiceRequest): Observable<WalletsResponse>

    @POST
    fun getWalletBalance(@Url url: String, @Body body: ServiceRequest): Observable<BalanceResponse>

    @POST
    fun refreshToken(@Url url: String, @Body body: ServiceRequest): Observable<RefreshResponse>

    @POST
    fun getWalletHistory(@Url url: String, @Body body: ServiceRequest): Observable<HistoryResponse>

    @POST
    fun makeTransaction(@Url url: String, @Body body: ServiceRequest): Observable<CreateTxResponse>

    @POST
    fun getTxInfo(@Url url: String, @Body body: ServiceRequest): Observable<GetTxInfoResponse>

    @POST
    fun getTxParams(@Url url: String, @Body body: ServiceRequest): Observable<GetTxParamsResponse>
}