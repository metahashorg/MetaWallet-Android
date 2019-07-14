package org.metahash.metawallet.api.commands

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.api.PrivateWalletFileHelper

class MigrationCmd {

    fun execute(): Completable {
        return Observable.fromIterable(
            WalletApplication.dbHelper.getUserWalletByLogin(
                WalletApplication.dbHelper.getLogin()
            )
        )
            .filter { !PrivateWalletFileHelper.isWalletHasFile(it) }
            .map { PrivateWalletFileHelper.saveWalletToFile(it) }
            .ignoreElements()
            .subscribeOn(Schedulers.io())
    }
}