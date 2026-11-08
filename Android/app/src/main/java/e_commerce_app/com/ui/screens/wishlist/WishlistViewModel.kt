package e_commerce_app.com.ui.screens.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.Product
import e_commerce_app.com.data.repository.CartRepository
import e_commerce_app.com.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    val wishlist: StateFlow<List<Product>> = wishlistRepository.wishlist

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun removeFromWishlist(productId: String) {
        wishlistRepository.removeFromWishlist(productId)
        viewModelScope.launch {
            _snackbarEvent.emit("Removed from Wishlist")
        }
    }

    fun moveToCart(product: Product) {
        wishlistRepository.moveToCart(product, cartRepository)
        viewModelScope.launch {
            _snackbarEvent.emit("Moved \"${product.title}\" to Cart")
        }
    }
}

