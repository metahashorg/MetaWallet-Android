package org.metahash.metawallet.api.base

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.metahash.metawallet.data.models.ResponseError
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.UnknownHostException

abstract class BaseCommandWithMapping<RESULT, RESPONSE> : ProxyAddressProvider {

    private val KEY_CODE = "code"
    private val KEY_MESSAGE = "message"
    private val KEY_DATA = "data"

    var subscribeScheduler = Schedulers.io()

    fun execute() : Observable<RESULT> {
        val request = serviceRequest()
                .subscribeOn(subscribeScheduler)
                .onErrorResumeNext(Function<Throwable, Observable<RESPONSE>> {
                    if (it is HttpException) {
                        Observable.error(
                                parseError(it.response()?.errorBody()?.string() ?: "")
                        )
                    } else if (it is ConnectException || it is UnknownHostException) {
                          Observable.error(ResponseError(ResponseError.CODE_NETWORK, ""))
                    } else {
                        Observable.error(it)
                    }
                })
                .map {
                    if (it is Response<*>) {
                        val c = it.code()
                        if (c != HttpURLConnection.HTTP_OK &&
                                c != HttpURLConnection.HTTP_CREATED &&
                                c != HttpURLConnection.HTTP_ACCEPTED &&
                                c != HttpURLConnection.HTTP_NO_CONTENT) {
                            throw parseError(it.errorBody()?.string()
                                    ?: "")
                        }
                    }
                    it
                }

        return returnOnUI(
                afterResponse(
                        request
                                .observeOn(Schedulers.computation())
                )
        )
    }

    protected abstract fun serviceRequest(): Observable<RESPONSE>

    abstract fun afterResponse(response: Observable<RESPONSE>): Observable<RESULT>

    private fun returnOnUI(result: Observable<RESULT>): Observable<RESULT> =
            result.observeOn(AndroidSchedulers.mainThread())

    private fun parseError(error: String): ResponseError {
        return try {
            val obj = JSONObject(error)
            if (obj.has(KEY_DATA)) {
                ResponseError(
                        obj.getJSONObject(KEY_DATA)
                                .getString(KEY_CODE),
                        obj.getJSONObject(KEY_DATA)
                                .getString(KEY_MESSAGE)
                )
            } else {
                ResponseError(obj.getString(KEY_CODE), obj.getString(KEY_MESSAGE))
            }
        } catch (ex: Exception) {
            ResponseError("", "")
        }
    }
}