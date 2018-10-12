package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.api.mappers.LocalWalletToWalletMapper
import org.metahash.metawallet.data.models.HistoryData
import org.metahash.metawallet.data.models.HistoryResponse

class WalletHistoryCmd(
        private val api: Api,
        private val allWalletsCmd: AllWalletsCmd
) : BaseCommand<List<HistoryData>>() {

    var currency: String = ""

    override fun serviceRequest(): Observable<List<HistoryData>> {
        allWalletsCmd.currency = currency
        return allWalletsCmd.execute()
                .flatMap { getHistoryRequest(it.map { it.address }) }
                .map { list ->
                    list.forEach { it.currency = currency }
                    list
                }
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

        if (addresses.isEmpty()) {
            return Observable.just(listOf())
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