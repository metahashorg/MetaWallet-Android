package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.base.BaseCommandWithMapping
import org.metahash.metawallet.data.models.WalletsData

class BalanceChangedCmd(
        private val allBalances: AllBalancesCmd)
    : BaseCommandWithMapping<Boolean, List<WalletsData>>() {

    var cur = ""

    override fun serviceRequest() = allBalances.apply { this.currency = cur }.execute()

    override fun afterResponse(response: Observable<List<WalletsData>>): Observable<Boolean> {
        return response.map { remote ->
            val local = WalletApplication.dbHelper.getWalletsDataByCurrency(cur)
            if (local.isEmpty() && remote.isEmpty()) {
                return@map false
            } else {
                remote.forEach { rWallet ->
                    val common = local.firstOrNull { it.address == rWallet.address }
                    if (common != null) {
                        val result = common.balance.received != rWallet.balance.received ||
                                common.balance.spent != rWallet.balance.spent
                        if (result) {
                            //update
                            WalletApplication.dbHelper.setWalletsData(remote)
                            return@map true
                        }
                    }
                }
            }
            false
        }
    }
}