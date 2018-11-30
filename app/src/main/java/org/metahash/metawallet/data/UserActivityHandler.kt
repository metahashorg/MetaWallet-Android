package org.metahash.metawallet.data

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.metahash.metawallet.WalletApplication
import java.util.concurrent.TimeUnit

class UserActivityHandler {

    private val MAX_ACTIVITY_DELAY = 15

    private val activitySubject = PublishSubject.create<Unit>()
    private val subscription = CompositeDisposable()

    var onMaxTimeExceeded = {}

    fun clearExceedHandler() {
        onMaxTimeExceeded = {}
    }

    fun handleActivity() {
        activitySubject.onNext(Unit)
    }

    fun init() {
        startActivityObserving()
        startActivityCheck()
    }

    fun clear() {
        subscription.clear()
    }

    private fun startActivityObserving() {
        subscription.add(activitySubject.debounce(5, TimeUnit.SECONDS)
                .observeOn(Schedulers.computation())
                .onErrorReturn { Unit }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { WalletApplication.dbHelper.setLastActionTime(System.currentTimeMillis()) },
                        { it.printStackTrace() })
        )
    }

    private fun startActivityCheck() {
        subscription.add(Observable.interval(0, 5, TimeUnit.MINUTES)
                .observeOn(Schedulers.computation())
                .onErrorReturn { 0L }
                .map {
                    val difference = System.currentTimeMillis() - WalletApplication.dbHelper.getLastActionTime()
                    TimeUnit.MILLISECONDS.toMinutes(difference) >= MAX_ACTIVITY_DELAY
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            //need to show pincode
                            if (it) {
                                onMaxTimeExceeded.invoke()
                            }
                        },
                        { it.printStackTrace() }
                )
        )
    }
}