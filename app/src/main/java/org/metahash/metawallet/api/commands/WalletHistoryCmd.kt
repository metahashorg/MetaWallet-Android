package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.HistoryData
import org.metahash.metawallet.data.models.HistoryResponse
import org.metahash.metawallet.data.models.WalletsResponse
import retrofit2.Response

class WalletHistoryCmd(
        private val api: Api,
        private val walletsCmd: AllWalletsCmd
) : BaseCommand<List<HistoryData>>() {

    var currency: String = ""

    override fun serviceRequest(): Observable<List<HistoryData>> {
        return getWalletsByCurrency()
                .flatMap { getHistoryRequest(it.data.map { it.address }) }
                .map { list ->
                    list.forEach {
                        it.currency = currency
                    }
                    list
                }
    }

    private fun getWalletsByCurrency(): Observable<WalletsResponse> {
        walletsCmd.currency = currency
        return walletsCmd.getWalletsRequest()
    }

    private fun createHistoryRequest(address: String): Observable<HistoryResponse> {
        return api
                .getWalletHistory(getTorrentAddress(),
                        ServiceRequestFactory.getRequestData(
                                ServiceRequestFactory.REQUESTTYPE.WALLETHISTORY,
                                ServiceRequestFactory.getHistoryParams(address)))
    }

    private fun getHistoryRequest(addresses: List<String>): Observable<List<HistoryData>> {
        val requests = mutableListOf<Observable<HistoryResponse>>()
        addresses.forEach {
            requests.add(createHistoryRequest(it))
        }

        return Observable.combineLatest(
                requests
        ) { histories ->
            val result = mutableListOf<HistoryData>()
            histories.forEach {
                it as HistoryResponse
                result.addAll(it.result)
            }
            result
        }
    }

    fun executeWithCache() = execute()
            .observeOn(Schedulers.computation())
            .doOnNext {
                if (it.isNotEmpty()) {
                    WalletApplication.dbHelper.setWalletHistory(currency, it)
                }
            }
            .startWith(Observable.fromCallable { WalletApplication.dbHelper.getWalletHistory(currency) }
                    .subscribeOn(Schedulers.computation())
                    .filter { it.isNotEmpty() }
            )
            .map { WalletApplication.gson.toJson(it) }
}