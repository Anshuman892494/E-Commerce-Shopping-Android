package e_commerce_app.com.data.model

data class CartItem(
    val productId: String,
    val title: String,
    val price: Double,
    val imageUrl: String,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    var quantity: Int = 1,
    val availableStock: Int = 1
) {
    val subtotal: Double
        get() = price * quantity

    // Unique key identifying a specific variant combination in the cart
    val variantKey: String
        get() = "$productId-$selectedColor-$selectedSize"
}

data class CartValidationItem(
    val productId: String,
    val quantity: Int,
    val variant: String? = null
)

data class CartValidationRequest(
    val items: List<CartValidationItem>
)

data class CartValidationIssue(
    val productId: String,
    val title: String? = null,
    val requested: Int = 0,
    val available: Int = 0,
    val message: String
)

data class CartValidationResponse(
    val valid: Boolean,
    val issues: List<CartValidationIssue> = emptyList()
)

