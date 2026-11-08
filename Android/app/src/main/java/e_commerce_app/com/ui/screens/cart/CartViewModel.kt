package e_commerce_app.com.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.CartItem
import e_commerce_app.com.data.model.CartValidationIssue
import e_commerce_app.com.data.repository.CartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isValidatingStock: Boolean = false,
    val stockIssues: List<CartValidationIssue> = emptyList()
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    val shipping: Double
        get() = if (items.isEmpty() || subtotal > 100.0) 0.0 else 9.99

    val tax: Double
        get() = (subtotal * 0.08)

    val grandTotal: Double
        get() = subtotal + shipping + tax
}

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // Collect reactive cart updates from repository
        viewModelScope.launch {
            cartRepository.cartItems.collect { list ->
                _uiState.update { it.copy(items = list) }
                if (list.isNotEmpty()) {
                    validateStock()
                }
            }
        }
    }

    fun updateQuantity(variantKey: String, newQty: Int) {
        cartRepository.updateQuantity(variantKey, newQty)
    }

    fun removeItem(variantKey: String) {
        cartRepository.removeFromCart(variantKey)
    }

    fun validateStock() {
        viewModelScope.launch {
            _uiState.update { it.copy(isValidatingStock = true) }
            val response = cartRepository.validateStock()
            _uiState.update {
                it.copy(
                    isValidatingStock = false,
                    stockIssues = response.issues
                )
            }
        }
    }
}

