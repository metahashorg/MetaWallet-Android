package org.metahash.metawallet.api.commands

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.Info
import org.metahash.metawallet.data.models.Proxy
import org.metahash.metawallet.data.models.ResolvingResult
import org.metahash.metawallet.data.models.Status
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.concurrent.Executors

class GetProxyCmd(
        private val gson: Gson
) {

    private val avg = "avg"
    private val averageRegEx = "[0-9]+.[0-9]+".toRegex()
    private val MAX_PROXY_COUNT = 3
    private val executor = Executors.newFixedThreadPool(2)

    val proxyList = mutableListOf<Proxy>()
    val torrentLis = mutableListOf<Proxy>()

    fun execute(): Observable<String> {
        proxyList.clear()
        torrentLis.clear()
        return Observable.combineLatest(
                getResolveObserver(Constants.URL_PROXY_DEV),
                getResolveObserver(Constants.URL_TORRENT_DEV, false),
                BiFunction<Info, Info, ResolvingResult>
                { proxy, torrent -> ResolvingResult(proxy, torrent) }
        ).map { resultToString(it) }
    }

    fun saveProxy() {
        proxyList.sortBy { it.ping }
        torrentLis.sortBy { it.ping }
        if (proxyList.isNotEmpty()) {
            if (proxyList.size <= MAX_PROXY_COUNT) {
                WalletApplication.dbHelper.setProxy(proxyList)
            } else {
                WalletApplication.dbHelper.setProxy(proxyList.subList(0, MAX_PROXY_COUNT))
            }
        }
        if (torrentLis.isNotEmpty()) {
            if (torrentLis.size <= MAX_PROXY_COUNT) {
                WalletApplication.dbHelper.setTorrent(torrentLis)
            } else {
                WalletApplication.dbHelper.setTorrent(torrentLis.subList(0, MAX_PROXY_COUNT))
            }
        }
    }

    private fun resultToString(result: ResolvingResult) = gson.toJson(result)

    private fun getResolveObserver(address: String, isProxy: Boolean = true): Observable<Info> {
        return Observable.fromCallable { InetAddress.getAllByName(address).toMutableList() }
                .flatMap {
                    getPingObs(it, isProxy)
                            .startWith(Info(it.size))
                }
                .subscribeOn(Schedulers.from(executor))
                .startWith(Info())
    }

    private fun getPingObs(ips: MutableList<InetAddress>, isProxy: Boolean): Observable<Info> {
        return Observable.fromIterable(ips.toMutableList())
                .map {
                    try {
                        val ip = it.hostAddress
                        val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $ip")
                        val exitValue = process.waitFor()
                        BufferedReader(InputStreamReader(process.inputStream)).use {
                            it.lineSequence().forEach {
                                if (it.toLowerCase().indexOf(avg.toLowerCase()) != -1) {
                                    val matches = averageRegEx.findAll(it).toList()
                                    if (matches.size == 4) {
                                        if (isProxy) {
                                            proxyList.add(Proxy(ip, matches[1].value.toDouble()))
                                        } else {
                                            torrentLis.add(Proxy(ip, matches[1].value.toDouble()))
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    return@map Info(2, Status(ips.size, if (isProxy) proxyList.size else torrentLis.size))
                }
    }
}