package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.api.mappers.WalletWithBalanceMapper
import org.metahash.metawallet.data.models.BalanceData
import org.metahash.metawallet.data.models.BalanceResponse
import org.metahash.metawallet.data.models.WalletsData
import java.util.concurrent.Executors

class AllBalancesCmd(
        private val allWalletsCmd: AllWalletsCmd,
        private val balanceCmd: WalletBalanceCmd
) : BaseCommand<List<WalletsData>>() {

    private val executor = Executors.newFixedThreadPool(3)
    private val toSimpleWalletMapper = WalletWithBalanceMapper()

    var currency = -1
    var isOnlyLocal = false

    override fun serviceRequest(): Observable<List<WalletsData>> {
        allWalletsCmd.currency = currency
        return allWalletsCmd.execute()
                .flatMap(
                        { getBalancesRequest(it) },
                        { wallets, balances ->
                            wallets.forEach { wallet ->
                                val balance = balances.firstOrNull { it.address == wallet.address }
                                if (balance != null) {
                                    wallet.balance = balance
                                }
                            }
                            wallets
                        })
    }

    private fun getBalancesRequest(addresses: List<WalletsData>): Observable<List<BalanceData>> {
        val list = mutableListOf<Observable<BalanceResponse>>()
        addresses.forEach {
            balanceCmd.address = it.address
            balanceCmd.currency = it.currency.toInt()
            balanceCmd.subscribeScheduler = Schedulers.from(executor)
            list.add(balanceCmd.execute())
        }

        if (addresses.isEmpty()) {
            return Observable.just(listOf())
        }

        return Observable.combineLatest(list)
        { balances ->
            balances.map {
                it as BalanceResponse
                it.result
            }
        }
    }

    fun executeWithCache() = execute()
            .observeOn(Schedulers.computation())
            .doOnNext {
                if (it.isNotEmpty()) {
                    WalletApplication.dbHelper.setWalletsData(it)
                }
            }
            .startWith(Observable.fromCallable {
                WalletApplication.dbHelper.getWalletsDataByCurrency(currency.toString(),
                    WalletApplication.dbHelper.getLogin())
            }
                    .subscribeOn(Schedulers.computation())
                    .filter { it.isNotEmpty() }
            )
            //filter by isLocalOnly variable
            .map {
                if (isOnlyLocal.not()) {
                    it
                } else {
                    it.filter { it.hasPrivateKey }
                }
            }
            .map { it.map { toSimpleWalletMapper.fromEntity(it) } }
            .map { WalletApplication.gson.toJson(it) }
}