package e_commerce_app.com.data.repository

import e_commerce_app.com.data.local.SecureStorage
import e_commerce_app.com.data.model.LoginRequest
import e_commerce_app.com.data.model.RegisterRequest
import e_commerce_app.com.data.model.User
import e_commerce_app.com.data.remote.ApiService
import e_commerce_app.com.data.remote.NetworkResult

class AuthRepository(
    private val apiService: ApiService,
    private val secureStorage: SecureStorage
) {

    fun getCurrentUser(): User? = secureStorage.getUser()

    fun isLoggedIn(): Boolean = secureStorage.isLoggedIn()

    suspend fun login(email: String, password: String): NetworkResult<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                secureStorage.saveAuthToken(auth.token)
                secureStorage.saveUser(auth.user)
                NetworkResult.Success(auth.user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Invalid credentials"
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): NetworkResult<User> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                secureStorage.saveAuthToken(auth.token)
                secureStorage.saveUser(auth.user)
                NetworkResult.Success(auth.user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    fun logout() {
        secureStorage.clearSession()
    }
}

