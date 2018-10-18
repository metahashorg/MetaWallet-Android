package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.data.models.HistoryData
import org.metahash.metawallet.data.models.HistoryResponse
import java.util.concurrent.Executors

class WalletHistoryCmd(
        private val allWalletsCmd: AllWalletsCmd,
        private val historyCmd: HistoryCmd
) : BaseCommand<List<HistoryData>>() {

    var currency = -1
    var isOnlyLocal = false
    private val executor = Executors.newFixedThreadPool(3)

    override fun serviceRequest(): Observable<List<HistoryData>> {
        allWalletsCmd.currency = currency
        allWalletsCmd.isLocalOnly = isOnlyLocal
        return allWalletsCmd.execute()
                .flatMap { getHistoryRequest(it.map { it.address }) }
                .map { list ->
                    list.forEach { it.currency = currency.toString() }
                    list
                }
    }

    private fun getHistoryRequest(addresses: List<String>): Observable<List<HistoryData>> {
        val requests = mutableListOf<Observable<HistoryResponse>>()
        addresses.forEach {
            historyCmd.address = it
            historyCmd.subscribeScheduler = Schedulers.from(executor)
            requests.add(historyCmd.execute())
        }

        if (addresses.isEmpty()) {
            return Observable.just(listOf())
        }
        return Observable.combineLatest(requests)
        { histories ->
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
                    WalletApplication.dbHelper.setWalletHistory(currency.toString(), it)
                }
            }
            .startWith(Observable.fromCallable { WalletApplication.dbHelper.getWalletHistoryByCurrency(currency.toString()) }
                    .subscribeOn(Schedulers.computation())
                    .filter { it.isNotEmpty() }
            )
            .map { WalletApplication.gson.toJson(it) }
}