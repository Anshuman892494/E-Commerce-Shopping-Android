package e_commerce_app.com.data.repository

import e_commerce_app.com.data.local.LocalDatabaseHelper
import e_commerce_app.com.data.model.*
import e_commerce_app.com.data.remote.ApiService
import e_commerce_app.com.data.remote.NetworkResult

class OrderRepository(
    private val apiService: ApiService,
    private val localDb: LocalDatabaseHelper,
    private val cartRepository: CartRepository
) {

    /**
     * Places an order against the backend server.
     * CRITICAL ACCEPTANCE RULE:
     * - Does NOT confirm order or deduct stock locally until backend returns HTTP 201 Created.
     * - On error (400, 402, 409, 504), returns failure, cart remains intact, no local order is recorded.
     */
    suspend fun placeOrder(
        items: List<CartItem>,
        address: ShippingAddress,
        paymentDetails: PaymentDetails,
        simulationMode: SimulationMode = SimulationMode.NORMAL,
        couponCode: String? = null
    ): NetworkResult<Order> {
        return try {
            val request = CreateOrderRequest(
                items = items,
                shippingAddress = address,
                paymentDetails = paymentDetails,
                simulationMode = simulationMode.name,
                couponCode = couponCode
            )

            val response = apiService.createOrder(request)
            if (response.isSuccessful && response.body() != null) {
                val confirmedOrder = response.body()!!.order
                // Persist confirmed order locally
                localDb.saveSingleOrder(confirmedOrder)
                // Clear cart only after server confirms the order
                cartRepository.clearCart()
                NetworkResult.Success(confirmedOrder)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                val message = parseErrorMessage(errorBody, response.code())
                NetworkResult.Error(response.code(), message)
            }
        } catch (e: Exception) {
            // Network timeout / connection lost
            NetworkResult.Exception(e)
        }
    }

    suspend fun getOrders(): Pair<List<Order>, Boolean> { // (orders, isOffline)
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful && response.body() != null) {
                val remoteOrders = response.body()!!.orders
                localDb.saveOrders(remoteOrders)
                Pair(remoteOrders, false)
            } else {
                val cached = localDb.getCachedOrders()
                Pair(cached, true)
            }
        } catch (e: Exception) {
            val cached = localDb.getCachedOrders()
            Pair(cached, true)
        }
    }

    suspend fun getOrderById(id: String): NetworkResult<Order> {
        return try {
            val response = apiService.getOrderById(id)
            if (response.isSuccessful && response.body() != null) {
                val fresh = response.body()!!
                localDb.saveSingleOrder(fresh)
                NetworkResult.Success(fresh)
            } else {
                val cached = localDb.getCachedOrders().find { it.id == id }
                if (cached != null) {
                    NetworkResult.Success(cached)
                } else {
                    NetworkResult.Error(response.code(), "Order not found")
                }
            }
        } catch (e: Exception) {
            val cached = localDb.getCachedOrders().find { it.id == id }
            if (cached != null) {
                NetworkResult.Success(cached)
            } else {
                NetworkResult.Exception(e)
            }
        }
    }

    suspend fun validateCoupon(code: String): NetworkResult<ValidateCouponResponse> {
        return try {
            val response = apiService.validateCoupon(ValidateCouponRequest(code))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), "Invalid promotional coupon code.")
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    private fun parseErrorMessage(errorBody: String, code: Int): String {
        return when {
            errorBody.contains("OUT_OF_STOCK", ignoreCase = true) ->
                "One or more items in your cart became out of stock. Please adjust quantities."
            errorBody.contains("PAYMENT_DECLINED", ignoreCase = true) || code == 402 ->
                "Payment was declined by your bank (Insufficient funds)."
            errorBody.contains("GATEWAY_TIMEOUT", ignoreCase = true) || code == 504 ->
                "Payment gateway timed out. Please try again."
            else -> "Order placement failed ($code). Please retry."
        }
    }
}

