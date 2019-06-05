package org.metahash.metawallet

import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.provider.Settings
import android.support.multidex.MultiDexApplication
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.metahash.metawallet.Constants.BASE_URL
import org.metahash.metawallet.api.Api
import org.metahash.metawallet.api.ServiceApi
import org.metahash.metawallet.data.DBHelper
import org.metahash.metawallet.data.UserActivityHandler
import org.metahash.metawallet.presentation.base.AppLifecycleListener
import org.metahash.metawallet.presentation.base.AppLifecycleProvider
import org.spongycastle.jce.provider.BouncyCastleProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.Security
import java.util.concurrent.TimeUnit

class WalletApplication : MultiDexApplication(), AppLifecycleProvider {

    private val appLifecycleListener = AppLifecycleListener(this)

    companion object {
        lateinit var appContext: Context
        val dbHelper: DBHelper by lazy { DBHelper() }
        val api: ServiceApi by lazy { initApi() }
        val gson = Gson()
        val deviceId by lazy {
            Settings.Secure.getString(WalletApplication.appContext.contentResolver,
                    Settings.Secure.ANDROID_ID)
        }

        init {
            System.loadLibrary("native-lib")
        }

        val activityHandler = UserActivityHandler()

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
        appContext = this
        dbHelper.clearLastActionTime()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleListener)
    }

    override fun onAppStart() {
        //activityHandler.init()
    }

    override fun onAppStop() {
        //activityHandler.clear()
    }
}