package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceRequestFactory
import org.metahash.metawallet.api.base.BaseCommand
import org.metahash.metawallet.api.base.BaseCommandWithMapping
import org.metahash.metawallet.api.mappers.LocalWalletToWalletMapper
import org.metahash.metawallet.data.models.WalletsData
import org.metahash.metawallet.data.models.WalletsResponse

class AllWalletsCmd(
        private val api: Api)
    : BaseCommandWithMapping<List<WalletsData>, WalletsResponse>() {

    private val fromLocalMapper = LocalWalletToWalletMapper()

    var currency = -1
    var isLocalOnly = false

    override fun serviceRequest() = api
            .getUserWallets(Constants.BASE_URL_WALLET,
                    ServiceRequestFactory.getRequestData(
                            ServiceRequestFactory.REQUESTTYPE.ALLWALLETS,
                            ServiceRequestFactory.getAllWalletsParams(currency)))

    override fun afterResponse(response: Observable<WalletsResponse>): Observable<List<WalletsData>> {
        return response.map {
            val userLogin = WalletApplication.dbHelper.getLogin()
            val local = WalletApplication.dbHelper.getUserWalletsByCurrency(currency.toString(),
                    userLogin)
            if (isLocalOnly) {
                return@map local.map { fromLocalMapper.fromEntity(it) }
            }
            val result = it.data.toMutableList()
            //remove all local wallet from remote
            local.forEach { wallet ->
                result.removeAll { it.address.equals(wallet.address, true) }
            }
            result.forEach {
                it.userLogin = userLogin
            }
            result.addAll(local.map { fromLocalMapper.fromEntity(it) })
            result
        }
    }
}