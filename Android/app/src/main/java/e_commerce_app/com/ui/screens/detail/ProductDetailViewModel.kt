package e_commerce_app.com.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.Product
import e_commerce_app.com.data.remote.NetworkResult
import e_commerce_app.com.data.repository.CartRepository
import e_commerce_app.com.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    val quantity: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAddedToCart: Boolean = false
)

class ProductDetailViewModel(
    private val productId: String,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = productRepository.getProductById(productId)) {
                is NetworkResult.Success -> {
                    val p = result.data
                    val defaultColor = p.variants.colors.firstOrNull()?.name
                    val defaultSize = p.variants.sizes.firstOrNull()
                    _uiState.update {
                        it.copy(
                            product = p,
                            selectedColor = defaultColor,
                            selectedSize = defaultSize,
                            quantity = if (p.stock > 0) 1 else 0,
                            isLoading = false
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

    fun selectColor(color: String) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun selectSize(size: String) {
        _uiState.update { it.copy(selectedSize = size) }
    }

    fun incrementQuantity() {
        val current = _uiState.value
        val maxStock = current.product?.stock ?: 1
        if (current.quantity < maxStock) {
            _uiState.update { it.copy(quantity = it.quantity + 1) }
        }
    }

    fun decrementQuantity() {
        val current = _uiState.value
        if (current.quantity > 1) {
            _uiState.update { it.copy(quantity = it.quantity - 1) }
        }
    }

    fun addToCart(): Boolean {
        val current = _uiState.value
        val prod = current.product ?: return false
        if (prod.stock <= 0) return false

        val success = cartRepository.addToCart(
            product = prod,
            selectedColor = current.selectedColor,
            selectedSize = current.selectedSize,
            quantity = current.quantity
        )
        if (success) {
            _uiState.update { it.copy(isAddedToCart = true) }
        }
        return success
    }

    fun resetCartFeedback() {
        _uiState.update { it.copy(isAddedToCart = false) }
    }
}

