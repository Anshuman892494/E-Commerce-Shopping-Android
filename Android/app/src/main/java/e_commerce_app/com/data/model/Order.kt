package e_commerce_app.com.data.model

data class ShippingAddress(
    val fullName: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val phone: String
)

data class PaymentDetails(
    val method: String = "Credit Card",
    val last4: String = "4242"
)

enum class SimulationMode {
    NORMAL,          // Standard successful placement (201 Created)
    CARD_DECLINED,   // Simulates card failure (402 Payment Required)
    OUT_OF_STOCK,    // Simulates stock collision (409 Conflict)
    TIMEOUT          // Simulates network drop / gateway timeout
}

data class CreateOrderRequest(
    val items: List<CartItem>,
    val shippingAddress: ShippingAddress,
    val paymentDetails: PaymentDetails,
    val simulationMode: String = "NORMAL",
    val couponCode: String? = null
)

data class Order(
    val id: String,
    val createdAt: String,
    val status: String, // CONFIRMED, PROCESSING, SHIPPED, DELIVERED, FAILED
    val items: List<CartItem>,
    val shippingAddress: ShippingAddress,
    val paymentMethod: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    val shipping: Double,
    val tax: Double,
    val grandTotal: Double,
    val couponCode: String? = null,
    val estimatedDelivery: String,
    val isServerConfirmed: Boolean = true
)

data class ValidateCouponRequest(
    val code: String
)

data class ValidateCouponResponse(
    val valid: Boolean,
    val code: String? = null,
    val discountPercent: Double = 0.0,
    val description: String? = null,
    val message: String? = null
)

data class CreateOrderResponse(
    val success: Boolean,
    val message: String,
    val order: Order
)

data class OrdersListResponse(
    val orders: List<Order>,
    val total: Int
)

