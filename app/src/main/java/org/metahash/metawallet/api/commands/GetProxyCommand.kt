package org.metahash.metawallet.api.commands

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.metahash.metawallet.Constants
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.Proxy
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.concurrent.Executors

class GetProxyCommand {

    private val executor = Executors.newFixedThreadPool(2)

    fun execute(): Observable<Boolean> {
        return Observable.combineLatest(
                getPingObs(Constants.URL_PROXY_DEV),
                getPingObs(Constants.URL_TORRENT_DEV),
                BiFunction<List<Proxy>, List<Proxy>, Boolean> { proxy, torrent ->
                    if (proxy.isNotEmpty()) {
                        WalletApplication.dbHelper.setProxy(proxy[0])
                    }
                    if (torrent.isNotEmpty()) {
                        WalletApplication.dbHelper.setTorrent(torrent[0])
                    }
                    proxy.isNotEmpty() && torrent.isNotEmpty()
                }
        )
    }

    private fun getPingObs(address: String): Observable<List<Proxy>> {
        return Observable.fromCallable {
            try {
                val ips = InetAddress.getAllByName(address)
                val avg = "avg"
                val regEx = "[0-9]+.[0-9]+".toRegex()
                val result = mutableListOf<Proxy>()
                ips.forEach {
                    try {
                        val ip = it.hostAddress
                        val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 $ip")
                        val exitValue = process.waitFor()
                        BufferedReader(InputStreamReader(process.inputStream)).use {
                            it.lineSequence().forEach {
                                if (it.toLowerCase().indexOf(avg.toLowerCase()) != -1) {
                                    val matches = regEx.findAll(it).toList()
                                    if (matches.size == 4) {
                                        result.add(Proxy(ip, matches[1].value.toDouble()))
                                    }
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                return@fromCallable result.sortedWith(compareBy { it.ping })
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return@fromCallable listOf<Proxy>()

        }.subscribeOn(Schedulers.from(executor))
    }
}