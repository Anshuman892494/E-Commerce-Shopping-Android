package e_commerce_app.com.data.repository

import e_commerce_app.com.data.local.LocalDatabaseHelper
import e_commerce_app.com.data.model.*
import e_commerce_app.com.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartRepository(
    private val apiService: ApiService,
    private val localDb: LocalDatabaseHelper
) {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        // Load persisted cart from local SQLite on startup
        val persisted = localDb.getCachedCart()
        _cartItems.value = persisted
    }

    fun addToCart(
        product: Product,
        selectedColor: String?,
        selectedSize: String?,
        quantity: Int = 1
    ): Boolean {
        if (product.stock <= 0) return false

        val currentList = _cartItems.value.toMutableList()
        val variantKey = "${product.id}-$selectedColor-$selectedSize"
        val existingIndex = currentList.indexOfFirst { it.variantKey == variantKey }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stock)
            currentList[existingIndex] = existing.copy(quantity = newQty, availableStock = product.stock)
        } else {
            val addedQty = quantity.coerceAtMost(product.stock)
            currentList.add(
                CartItem(
                    productId = product.id,
                    title = product.title,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    selectedColor = selectedColor,
                    selectedSize = selectedSize,
                    quantity = addedQty,
                    availableStock = product.stock
                )
            )
        }

        _cartItems.value = currentList
        localDb.saveCart(currentList)
        return true
    }

    fun updateQuantity(variantKey: String, newQuantity: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.variantKey == variantKey }
        if (index >= 0) {
            if (newQuantity <= 0) {
                currentList.removeAt(index)
            } else {
                val item = currentList[index]
                val safeQty = newQuantity.coerceAtMost(item.availableStock)
                currentList[index] = item.copy(quantity = safeQty)
            }
            _cartItems.value = currentList
            localDb.saveCart(currentList)
        }
    }

    fun removeFromCart(variantKey: String) {
        val currentList = _cartItems.value.filter { it.variantKey != variantKey }
        _cartItems.value = currentList
        localDb.saveCart(currentList)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        localDb.clearCart()
    }

    suspend fun validateStock(): CartValidationResponse {
        val items = _cartItems.value.map {
            CartValidationItem(
                productId = it.productId,
                quantity = it.quantity,
                variant = it.variantKey
            )
        }
        if (items.isEmpty()) {
            return CartValidationResponse(valid = true)
        }

        return try {
            val response = apiService.validateCart(CartValidationRequest(items))
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                CartValidationResponse(valid = true) // Fallback if network unavailable
            }
        } catch (e: Exception) {
            CartValidationResponse(valid = true)
        }
    }
}

