package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.api.mappers.WalletWithBalanceMapper
import org.metahash.metawallet.data.models.BalanceData
import org.metahash.metawallet.data.models.BalanceResponse
import org.metahash.metawallet.data.models.WalletsData
import java.util.concurrent.Executors

class AllWalletsCmd(
        private val api: Api,
        private val balanceCmd: WalletBalanceCmd
) : BaseCommand<List<WalletsData>>() {

    private val executor = Executors.newFixedThreadPool(3)
    private val toSimpleWalletMapper = WalletWithBalanceMapper()

    var currency = ""

    override fun serviceRequest(): Observable<List<WalletsData>> {
        return getWalletsRequest()
                .flatMap(
                        {
                            getBalancesRequest(it.data.map { it.address })
                        },
                        { wallets, balances ->
                            wallets.data.forEach { wallet ->
                                val balance = balances.firstOrNull { it.address == wallet.address }
                                if (balance != null) {
                                    wallet.balance = balance
                                }
                            }
                            wallets.data
                        })
    }

    private fun getBalancesRequest(addresses: List<String>): Observable<List<BalanceData>> {
        val list = mutableListOf<Observable<BalanceResponse>>()
        addresses.forEach {
            balanceCmd.address = it
            balanceCmd.subscribeScheduler = Schedulers.from(executor)
            list.add(balanceCmd.execute())
        }

        return Observable.combineLatest(
                list
        ) { balances ->
            balances.map {
                it as BalanceResponse
                it.result
            }
        }
    }

    fun getWalletsRequest() = api
            .getUserWallets(Constants.BASE_URL_WALLET,
                    ServiceRequestFactory.getRequestData(
                            ServiceRequestFactory.REQUESTTYPE.ALLWALLETS,
                            ServiceRequestFactory.getAllWalletsParams(currency)))

    fun executeWithCache() = execute()
            .observeOn(Schedulers.computation())
            .doOnNext {
                if (it.isNotEmpty()) {
                    WalletApplication.dbHelper.setWallets(it, currency)
                }
            }
            .startWith(Observable.fromCallable {
                val res = WalletApplication.dbHelper.getWallets(currency)
                if (res.isEmpty()) {

                }
                res
            }
                    .subscribeOn(Schedulers.computation())
                    .filter { it.isNotEmpty() }
            )
            .map { it.map { toSimpleWalletMapper.fromEntity(it) } }
            .map { WalletApplication.gson.toJson(it) }
}