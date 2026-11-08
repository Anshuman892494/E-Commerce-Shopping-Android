package e_commerce_app.com.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.User
import e_commerce_app.com.data.remote.NetworkResult
import e_commerce_app.com.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "demo@algions.com",
    val password: String = "Password123!",
    val name: String = "Anshul Demo User",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val existingUser = authRepository.getCurrentUser()
        if (existingUser != null) {
            _uiState.update { it.copy(user = existingUser) }
        }
    }

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun updateName(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }

    fun toggleAuthMode() {
        _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(current.email, current.password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Cannot connect to server. Ensure mock backend is running."
                        )
                    }
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.name.isBlank() || current.email.isBlank() || current.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required for registration.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.register(current.name, current.email, current.password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Registration failed. Check server connection."
                        )
                    }
                }
            }
        }
    }

    fun fillDemoCredentials() {
        _uiState.update {
            it.copy(
                email = "demo@algions.com",
                password = "Password123!",
                isRegisterMode = false,
                errorMessage = null
            )
        }
    }
}

