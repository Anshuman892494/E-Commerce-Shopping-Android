package e_commerce_app.com.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.*
import e_commerce_app.com.data.remote.NetworkResult
import e_commerce_app.com.data.repository.AuthRepository
import e_commerce_app.com.data.repository.CartRepository
import e_commerce_app.com.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val fullName: String = "",
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val phone: String = "",
    val paymentMethod: String = "UPI", // "UPI", "COD", "CARD"
    val upiId: String = "anshul@upi",
    val cardNumber: String = "4242 •••• •••• 4242",
    val cardExp: String = "12/28",
    val cardCvv: String = "888",
    val couponCodeInput: String = "",
    val appliedCoupon: String? = null,
    val discountPercent: Double = 0.0,
    val couponMessage: String? = null,
    val isValidatingCoupon: Boolean = false,
    val simulationMode: SimulationMode = SimulationMode.NORMAL,
    val isPlacingOrder: Boolean = false,
    val errorMessage: String? = null,
    val confirmedOrder: Order? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    val discountAmount: Double
        get() = (subtotal * discountPercent / 100.0)

    val discountedSubtotal: Double
        get() = maxOf(0.0, subtotal - discountAmount)

    val shipping: Double
        get() = if (items.isEmpty() || discountedSubtotal > 100.0) 0.0 else 9.99

    val tax: Double
        get() = (discountedSubtotal * 0.08)

    val grandTotal: Double
        get() = discountedSubtotal + shipping + tax

    val isAddressValid: Boolean
        get() = fullName.isNotBlank() && street.isNotBlank() && city.isNotBlank() && postalCode.isNotBlank()
}

class CheckoutViewModel(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadUserAddress()
        viewModelScope.launch {
            cartRepository.cartItems.collect { cartList ->
                _uiState.update { it.copy(items = cartList) }
            }
        }
    }

    fun loadUserAddress() {
        authRepository?.getCurrentUser()?.let { user ->
            val addr = user.shippingAddress
            _uiState.update {
                it.copy(
                    fullName = addr?.fullName?.ifBlank { user.name } ?: user.name,
                    street = addr?.street ?: "",
                    city = addr?.city ?: "",
                    postalCode = addr?.postalCode ?: "",
                    phone = addr?.phone ?: ""
                )
            }
        }
    }

    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value) }
    fun updateStreet(value: String) = _uiState.update { it.copy(street = value) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value) }
    fun updatePostalCode(value: String) = _uiState.update { it.copy(postalCode = value) }
    fun updatePhone(value: String) = _uiState.update { it.copy(phone = value) }

    fun onCouponCodeChanged(code: String) {
        _uiState.update { it.copy(couponCodeInput = code, couponMessage = null) }
    }

    fun applyCoupon() {
        val code = _uiState.value.couponCodeInput.trim()
        if (code.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isValidatingCoupon = true, couponMessage = null) }
            when (val res = orderRepository.validateCoupon(code)) {
                is NetworkResult.Success -> {
                    val resp = res.data
                    if (resp.valid) {
                        _uiState.update {
                            it.copy(
                                isValidatingCoupon = false,
                                appliedCoupon = resp.code ?: code.uppercase(),
                                discountPercent = resp.discountPercent,
                                couponMessage = resp.message ?: "Coupon applied: ${resp.discountPercent}% OFF!"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isValidatingCoupon = false,
                                couponMessage = resp.message ?: "Invalid coupon code."
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isValidatingCoupon = false,
                            couponMessage = res.message
                        )
                    }
                }
                is NetworkResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isValidatingCoupon = false,
                            couponMessage = "Failed to connect to coupon validation service."
                        )
                    }
                }
            }
        }
    }

    fun removeCoupon() {
        _uiState.update {
            it.copy(
                appliedCoupon = null,
                discountPercent = 0.0,
                couponCodeInput = "",
                couponMessage = null
            )
        }
    }

    fun selectPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun updateUpiId(id: String) {
        _uiState.update { it.copy(upiId = id) }
    }

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
            val payment = when (current.paymentMethod) {
                "UPI" -> PaymentDetails(
                    method = "UPI (${current.upiId.ifBlank { "instant@upi" }})",
                    last4 = "UPI"
                )
                "COD" -> PaymentDetails(
                    method = "Cash on Delivery (COD)",
                    last4 = "COD"
                )
                else -> PaymentDetails(
                    method = "Credit/Debit Card",
                    last4 = current.cardNumber.takeLast(4)
                )
            }

            when (val result = orderRepository.placeOrder(
                items = current.items,
                address = address,
                paymentDetails = payment,
                simulationMode = SimulationMode.NORMAL,
                couponCode = current.appliedCoupon
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
