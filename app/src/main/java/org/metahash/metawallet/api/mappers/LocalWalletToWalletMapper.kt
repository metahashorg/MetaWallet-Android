package org.metahash.metawallet.api.mappers

import android.util.Base64
import org.metahash.metawallet.data.models.BalanceData
import org.metahash.metawallet.data.models.Wallet
import org.metahash.metawallet.data.models.WalletsData

class LocalWalletToWalletMapper : BaseMapper<Wallet, WalletsData>() {

    override fun fromEntity(from: Wallet) = WalletsData(
            from.address, from.currency, "", "", from.code,
            "", BalanceData(), true, from.name, from.userLogin)

    fun convertName(original: String): String {
        return if (original.isEmpty()) {
            original
        } else {
            val bytes = Base64.decode(original, Base64.DEFAULT)
            String(bytes, Charsets.UTF_8)
        }
    }
}