package e_commerce_app.com.data.repository

import e_commerce_app.com.data.local.LocalDatabaseHelper
import e_commerce_app.com.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WishlistRepository(
    private val localDb: LocalDatabaseHelper
) {
    private val _wishlist = MutableStateFlow<List<Product>>(emptyList())
    val wishlist: StateFlow<List<Product>> = _wishlist.asStateFlow()

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        _wishlist.value = localDb.getWishlist()
    }

    fun isInWishlist(productId: String): Boolean {
        return _wishlist.value.any { it.id == productId }
    }

    fun toggleWishlist(product: Product) {
        if (isInWishlist(product.id)) {
            localDb.removeWishlistItem(product.id)
        } else {
            localDb.saveWishlistItem(product)
        }
        loadWishlist()
    }

    fun removeFromWishlist(productId: String) {
        localDb.removeWishlistItem(productId)
        loadWishlist()
    }

    fun moveToCart(product: Product, cartRepository: CartRepository) {
        val selectedColor = product.variants?.colors?.firstOrNull()?.name
        val selectedSize = product.variants?.sizes?.firstOrNull()
        cartRepository.addToCart(
            product = product,
            selectedColor = selectedColor,
            selectedSize = selectedSize,
            quantity = 1
        )
        removeFromWishlist(product.id)
    }
}
