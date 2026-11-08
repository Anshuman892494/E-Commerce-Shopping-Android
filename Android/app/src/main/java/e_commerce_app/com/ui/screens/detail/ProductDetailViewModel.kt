package e_commerce_app.com.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.CartItem
import e_commerce_app.com.data.model.Product
import e_commerce_app.com.data.remote.NetworkResult
import e_commerce_app.com.data.repository.CartRepository
import e_commerce_app.com.data.repository.ProductRepository
import e_commerce_app.com.data.repository.WishlistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    val quantity: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddedToCart: Boolean = false,
    val isWishlisted: Boolean = false
)

class ProductDetailViewModel(
    private val productId: String,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
        viewModelScope.launch {
            wishlistRepository.wishlist.collect { list ->
                val wishlisted = list.any { it.id == productId }
                _uiState.update { it.copy(isWishlisted = wishlisted) }
            }
        }
    }

    fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = productRepository.getProductById(productId)) {
                is NetworkResult.Success -> {
                    val p = result.data
                    val defaultColor = p.variants?.colors?.firstOrNull()?.name
                    val defaultSize = p.variants?.sizes?.firstOrNull()
                    val isWishlisted = wishlistRepository.isInWishlist(p.id)
                    _uiState.update {
                        it.copy(
                            product = p,
                            selectedColor = defaultColor,
                            selectedSize = defaultSize,
                            quantity = if (p.stock > 0) 1 else 0,
                            isLoading = false,
                            isWishlisted = isWishlisted
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.throwable.localizedMessage ?: "Failed to load product"
                        )
                    }
                }
            }
        }
    }

    fun toggleWishlist() {
        _uiState.value.product?.let { product ->
            wishlistRepository.toggleWishlist(product)
        }
    }

    fun selectColor(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun selectSize(size: String) {
        _uiState.update { it.copy(selectedSize = size) }
    }

    fun incrementQuantity() {
        val current = _uiState.value.quantity
        val max = _uiState.value.product?.stock ?: 1
        if (current < max) {
            _uiState.update { it.copy(quantity = current + 1) }
        }
    }

    fun decrementQuantity() {
        val current = _uiState.value.quantity
        if (current > 1) {
            _uiState.update { it.copy(quantity = current - 1) }
        }
    }

    fun addToCart() {
        val p = _uiState.value.product ?: return
        if (p.stock <= 0) return

        cartRepository.addToCart(
            product = p,
            selectedColor = _uiState.value.selectedColor,
            selectedSize = _uiState.value.selectedSize,
            quantity = _uiState.value.quantity
        )
        _uiState.update { it.copy(isAddedToCart = true) }
    }

    fun resetCartFeedback() {
        _uiState.update { it.copy(isAddedToCart = false) }
    }
}
