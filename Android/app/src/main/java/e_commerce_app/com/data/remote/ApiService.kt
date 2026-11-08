package e_commerce_app.com.data.remote

import e_commerce_app.com.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication ---
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<MeResponse>

    // --- Products Catalog ---
    @GET("api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): Response<ProductsResponse>

    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<Product>

    // --- Cart Stock Validation ---
    @POST("api/cart/validate")
    suspend fun validateCart(@Body request: CartValidationRequest): Response<CartValidationResponse>

    // --- Orders ---
    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<CreateOrderResponse>

    @GET("api/orders")
    suspend fun getOrders(): Response<OrdersListResponse>

    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): Response<Order>
}

