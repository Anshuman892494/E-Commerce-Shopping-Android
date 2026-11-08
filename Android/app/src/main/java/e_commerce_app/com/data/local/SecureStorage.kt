package e_commerce_app.com.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import e_commerce_app.com.data.model.ShippingAddress
import e_commerce_app.com.data.model.User

/**
 * Manages secure storage of authentication tokens and session credentials
 * using Android Jetpack Security (EncryptedSharedPreferences with AES-256-GCM).
 */
class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_ecommerce_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUser(user: User) {
        val editor = sharedPreferences.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)

        user.shippingAddress?.let { addr ->
            editor.putString(KEY_ADDR_FULL_NAME, addr.fullName)
                .putString(KEY_ADDR_STREET, addr.street)
                .putString(KEY_ADDR_CITY, addr.city)
                .putString(KEY_ADDR_POSTAL, addr.postalCode)
                .putString(KEY_ADDR_PHONE, addr.phone)
        }
        editor.apply()
    }

    fun getUser(): User? {
        val id = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        val name = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        val email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
        val street = sharedPreferences.getString(KEY_ADDR_STREET, null)
        val shippingAddress = if (street != null) {
            ShippingAddress(
                fullName = sharedPreferences.getString(KEY_ADDR_FULL_NAME, name) ?: name,
                street = street,
                city = sharedPreferences.getString(KEY_ADDR_CITY, "") ?: "",
                postalCode = sharedPreferences.getString(KEY_ADDR_POSTAL, "") ?: "",
                phone = sharedPreferences.getString(KEY_ADDR_PHONE, "") ?: ""
            )
        } else null
        return User(id, name, email, shippingAddress)
    }

    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_ADDR_FULL_NAME)
            .remove(KEY_ADDR_STREET)
            .remove(KEY_ADDR_CITY)
            .remove(KEY_ADDR_POSTAL)
            .remove(KEY_ADDR_PHONE)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_ADDR_FULL_NAME = "addr_full_name"
        private const val KEY_ADDR_STREET = "addr_street"
        private const val KEY_ADDR_CITY = "addr_city"
        private const val KEY_ADDR_POSTAL = "addr_postal"
        private const val KEY_ADDR_PHONE = "addr_phone"
    }
}

