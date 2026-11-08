package e_commerce_app.com.data.repository

import e_commerce_app.com.data.local.SecureStorage
import e_commerce_app.com.data.model.*
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

    suspend fun updateProfile(name: String, shippingAddress: ShippingAddress): NetworkResult<User> {
        val token = secureStorage.getAuthToken() ?: return NetworkResult.Error(401, "Not authenticated")
        return try {
            val response = apiService.updateProfile("Bearer $token", UpdateProfileRequest(name, shippingAddress))
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!.user
                secureStorage.saveUser(updatedUser)
                NetworkResult.Success(updatedUser)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }

    fun logout() {
        secureStorage.clearSession()
    }

    suspend fun getDemoAccount(): NetworkResult<DemoAccountResponse> {
        return try {
            val response = apiService.getDemoAccount()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch demo account"
                NetworkResult.Error(response.code(), errorMsg)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
}

