package org.metahash.metawallet

import android.app.Application
import android.support.multidex.MultiDexApplication
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.metahash.metawallet.Constants.BASE_URL
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceApi
import org.metahash.metawallet.data.DBHelper
import org.spongycastle.jce.provider.BouncyCastleProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.Security
import java.util.concurrent.TimeUnit

class WalletApplication : MultiDexApplication() {

    companion object {
        val dbHelper: DBHelper by lazy { DBHelper() }
        val api: ServiceApi by lazy { initApi() }
        val gson = Gson()

        private fun initApi(): ServiceApi {
            return ServiceApi(createRetrofit().create(Api::class.java))
        }

        private fun createRetrofit(): Retrofit {
            val client = OkHttpClient.Builder()
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()

            return Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .baseUrl(BASE_URL)
                    .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
        try {
            Security.addProvider(BouncyCastleProvider())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}