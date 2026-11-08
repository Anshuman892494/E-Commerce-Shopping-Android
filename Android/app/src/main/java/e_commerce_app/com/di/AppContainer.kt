package e_commerce_app.com.di

import android.content.Context
import e_commerce_app.com.BuildConfig
import e_commerce_app.com.data.local.LocalDatabaseHelper
import e_commerce_app.com.data.local.SecureStorage
import e_commerce_app.com.data.remote.ApiService
import e_commerce_app.com.data.repository.*
import e_commerce_app.com.util.NetworkConnectivityObserver
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Clean & Explainable Dependency Container.
 * Reads Base URL and Secure API Key injected from local.properties via BuildConfig.
 * All data is fetched directly from the live backend server.
 */
class AppContainer(private val context: Context) {

    // Injected securely from local.properties
    var baseUrl: String = BuildConfig.BASE_URL

    // Injects secure API Key header
    private val apiKeyInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("x-api-key", BuildConfig.API_KEY)
            .method(original.method, original.body)
        chain.proceed(requestBuilder.build())
    }

    // Handles Windows Firewall virtual NAT by seamlessly retrying over ADB reverse tunnel (127.0.0.1)
    private val hostFallbackInterceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            val host = request.url.host
            if (host == "10.0.2.2") {
                val fallbackUrl = request.url.newBuilder().host("127.0.0.1").build()
                val fallbackRequest = request.newBuilder().url(fallbackUrl).build()
                chain.proceed(fallbackRequest)
            } else {
                throw e
            }
        }
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
            redactHeader("x-api-key")
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(hostFallbackInterceptor)
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
