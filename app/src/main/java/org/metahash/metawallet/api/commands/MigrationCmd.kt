package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.PrivateWalletFileHelper

class MigrationCmd {

    fun execute(): Observable<Unit> {
        return Observable.fromIterable(
            WalletApplication.dbHelper.getUserWalletByLogin(
                WalletApplication.dbHelper.getLogin()
            )
        )
            .filter { !PrivateWalletFileHelper.isWalletHasFile(it) }
            .map { PrivateWalletFileHelper.saveWalletToFile(it) }
            .toList()
            .map { Unit }
            .toObservable()
            .subscribeOn(Schedulers.io())
    }
}