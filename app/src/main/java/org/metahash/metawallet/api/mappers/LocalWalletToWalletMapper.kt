package org.metahash.metawallet.api.mappers

import org.metahash.metawallet.data.models.BalanceData
import org.metahash.metawallet.data.models.Wallet
import org.metahash.metawallet.data.models.WalletsData

class LocalWalletToWalletMapper : BaseMapper<Wallet, WalletsData>() {

    override fun fromEntity(from: Wallet) = WalletsData(
            from.address, from.currency, "", "", from.code,
            "", BalanceData(), true, from.name)
}