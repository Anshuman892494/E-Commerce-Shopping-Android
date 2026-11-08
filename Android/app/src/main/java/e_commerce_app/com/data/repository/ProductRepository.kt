package e_commerce_app.com.data.repository

import e_commerce_app.com.data.local.LocalDatabaseHelper
import e_commerce_app.com.data.model.Product
import e_commerce_app.com.data.remote.ApiService
import e_commerce_app.com.data.remote.NetworkResult

class ProductRepository(
    private val apiService: ApiService,
    private val localDb: LocalDatabaseHelper
) {

    suspend fun getProducts(
        category: String? = null,
        search: String? = null,
        sort: String? = null
    ): Pair<List<Product>, Boolean> { // (products, isOfflineCached)
        return try {
            val response = apiService.getProducts(category, search, sort)
            if (response.isSuccessful && response.body() != null) {
                val freshProducts = response.body()!!.products
                // Update local cache when on 'All' products view
                if (category.isNullOrEmpty() || category == "All") {
                    localDb.saveProducts(freshProducts)
                }
                Pair(freshProducts, false)
            } else {
                // Fallback to offline cached products
                val cached = localDb.getCachedProducts()
                val filtered = filterCached(cached, category, search, sort)
                Pair(filtered, true)
            }
        } catch (e: Exception) {
            // Network failure / offline
            val cached = localDb.getCachedProducts()
            val filtered = filterCached(cached, category, search, sort)
            Pair(filtered, true)
        }
    }

    suspend fun getProductById(id: String): NetworkResult<Product> {
        return try {
            val response = apiService.getProductById(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                // Check local cache
                val cached = localDb.getCachedProducts().find { it.id == id }
                if (cached != null) {
                    NetworkResult.Success(cached)
                } else {
                    NetworkResult.Error(response.code(), "Product not found")
                }
            }
        } catch (e: Exception) {
            val cached = localDb.getCachedProducts().find { it.id == id }
            if (cached != null) {
                NetworkResult.Success(cached)
            } else {
                NetworkResult.Exception(e)
            }
        }
    }

    private fun filterCached(
        items: List<Product>,
        category: String?,
        search: String?,
        sort: String?
    ): List<Product> {
        var result = items
        if (!category.isNullOrEmpty() && category != "All") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }
        if (!search.isNullOrEmpty()) {
            val q = search.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }
        if (sort == "price_asc") {
            result = result.sortedBy { it.price }
        } else if (sort == "price_desc") {
            result = result.sortedByDescending { it.price }
        } else if (sort == "rating") {
            result = result.sortedByDescending { it.rating }
        }
        return result
    }
}

