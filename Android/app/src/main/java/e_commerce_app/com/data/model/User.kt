package e_commerce_app.com.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val shippingAddress: ShippingAddress? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    val shippingAddress: ShippingAddress? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class MeResponse(
    val user: User
)

