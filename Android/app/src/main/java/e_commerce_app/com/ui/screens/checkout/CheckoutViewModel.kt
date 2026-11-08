package e_commerce_app.com.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.*
import e_commerce_app.com.data.remote.NetworkResult
import e_commerce_app.com.data.repository.CartRepository
import e_commerce_app.com.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val fullName: String = "Anshul Sharma",
    val street: String = "42 Silicon Avenue, Sector 62",
    val city: String = "Noida",
    val postalCode: String = "201301",
    val phone: String = "+91 98765 43210",
    val cardNumber: String = "4242 •••• •••• 4242",
    val cardExp: String = "12/28",
    val cardCvv: String = "888",
    val simulationMode: SimulationMode = SimulationMode.NORMAL,
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
    val confirmedOrder: Order? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    val shipping: Double
        get() = if (items.isEmpty() || subtotal > 100.0) 0.0 else 9.99

    val tax: Double
        get() = (subtotal * 0.08)

    val grandTotal: Double
        get() = subtotal + shipping + tax

    val isAddressValid: Boolean
        get() = fullName.isNotBlank() && street.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()
}

class CheckoutViewModel(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cartRepository.cartItems.collect { cartList ->
                _uiState.update { it.copy(items = cartList) }
            }
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value) }
    fun updateStreet(value: String) = _uiState.update { it.copy(street = value) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value) }
    fun updatePostalCode(value: String) = _uiState.update { it.copy(postalCode = value) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value) }

    fun setSimulationMode(mode: SimulationMode) {
        _uiState.update { it.copy(simulationMode = mode) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Submits order to backend.
     * Strictly adheres to Acceptance Test 1 & 5:
     * - Disables UI button during network call to prevent duplicate submissions.
     * - Only marks order confirmed and updates UI upon backend 201 response.
     */
    fun placeOrder(onSuccess: (Order) -> Unit) {
        val current = _uiState.value
        if (!current.isAddressValid || current.isPlacingOrder || current.items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, errorMessage = null) }

            val address = ShippingAddress(
                fullName = current.fullName,
                street = current.street,
                city = current.city,
                postalCode = current.postalCode,
                phone = current.phone
            )
            val payment = PaymentDetails(
                method = "Simulated Card",
                last4 = current.cardNumber.takeLast(4)
            )

            when (val result = orderRepository.placeOrder(
                items = current.items,
                address = address,
                paymentDetails = payment,
                simulationMode = current.simulationMode
            )) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            confirmedOrder = result.data,
                            errorMessage = null
                        )
                    }
                    onSuccess(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            errorMessage = result.message
                        )
                    }
                }
                is NetworkResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isPlacingOrder = false,
                            errorMessage = "Network or timeout error. Please check connection and retry."
                        )
                    }
                }
            }
        }
    }
}

