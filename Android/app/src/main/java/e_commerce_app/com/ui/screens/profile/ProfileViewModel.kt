package e_commerce_app.com.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.ShippingAddress
import e_commerce_app.com.data.model.User
import e_commerce_app.com.data.remote.NetworkResult
import e_commerce_app.com.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isDarkMode: Boolean = false,
    val savedAddress: ShippingAddress = ShippingAddress(
        fullName = "Algions Demo Student",
        street = "452 Innovation Blvd, Suite 300",
        city = "Bangalore",
        postalCode = "560100",
        phone = "+91 98765 43210"
    ),
    val isUpdating: Boolean = false,
    val message: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val darkModeFlow: MutableStateFlow<Boolean>
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        val user = authRepository.getCurrentUser()
        val currentDark = darkModeFlow.value
        val initialAddress = user?.shippingAddress ?: _uiState.value.savedAddress
        _uiState.update {
            it.copy(
                user = user,
                isDarkMode = currentDark,
                savedAddress = initialAddress
            )
        }

        viewModelScope.launch {
            darkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
    }

    fun toggleDarkMode() {
        val next = !darkModeFlow.value
        darkModeFlow.value = next
    }

    fun updateAddress(newAddress: ShippingAddress) {
        _uiState.update { it.copy(isUpdating = true, message = null) }
        viewModelScope.launch {
            val userName = _uiState.value.user?.name ?: "Algions User"
            when (val res = authRepository.updateProfile(userName, newAddress)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            user = res.data,
                            savedAddress = newAddress,
                            isUpdating = false,
                            message = "Delivery address updated in MongoDB."
                        )
                    }
                }
                else -> {
                    // Fallback local update if network is offline
                    _uiState.update {
                        it.copy(
                            savedAddress = newAddress,
                            isUpdating = false,
                            message = "Address updated locally (offline mode)."
                        )
                    }
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        authRepository.logout()
        onLoggedOut()
    }
}
