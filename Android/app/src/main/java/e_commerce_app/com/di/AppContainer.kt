package e_commerce_app.com.di

import android.content.Context
import e_commerce_app.com.data.local.LocalDatabaseHelper
import e_commerce_app.com.data.local.SecureStorage
import e_commerce_app.com.data.remote.ApiService
import e_commerce_app.com.data.remote.MockApiInterceptor
import e_commerce_app.com.data.repository.*
import e_commerce_app.com.util.NetworkConnectivityObserver
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Clean & Explainable Dependency Container (Service Locator Pattern).
 * Holds and provides singletons for Network, Local Database, and Repositories.
 */
class AppContainer(private val context: Context) {

    // Attempts connection to Node.js backend. If blocked by Windows Firewall or offline,
    // MockApiInterceptor seamlessly provides the documented mock API contract.
    var baseUrl: String = "http://10.0.2.2:5000/"

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(MockApiInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val secureStorage: SecureStorage by lazy {
        SecureStorage(context)
    }

    val localDb: LocalDatabaseHelper by lazy {
        LocalDatabaseHelper(context)
    }

    val networkObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, secureStorage)
    }

    val cartRepository: CartRepository by lazy {
        CartRepository(apiService, localDb)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(apiService, localDb)
    }

    val orderRepository: OrderRepository by lazy {
        OrderRepository(apiService, localDb, cartRepository)
    }
}
