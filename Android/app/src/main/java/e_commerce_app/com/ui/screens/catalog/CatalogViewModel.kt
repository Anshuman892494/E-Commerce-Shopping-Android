package e_commerce_app.com.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import e_commerce_app.com.data.model.Product
import e_commerce_app.com.data.repository.ProductRepository
import e_commerce_app.com.data.repository.WishlistRepository
import e_commerce_app.com.util.NetworkConnectivityObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = listOf("All", "Electronics", "Footwear", "Apparel", "Accessories", "Home"),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val selectedSort: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val wishlistedIds: Set<String> = emptySet()
)

class CatalogViewModel(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val networkObserver: NetworkConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe connectivity changes
        viewModelScope.launch {
            networkObserver.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isOffline = !isConnected) }
                if (isConnected && _uiState.value.products.isEmpty()) {
                    loadProducts()
                }
            }
        }

        // Observe wishlist changes
        viewModelScope.launch {
            wishlistRepository.wishlist.collect { list ->
                val ids = list.map { it.id }.toSet()
                _uiState.update { it.copy(wishlistedIds = ids) }
            }
        }

        loadProducts()
    }

    fun toggleWishlist(product: Product) {
        wishlistRepository.toggleWishlist(product)
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val (products, isOffline) = productRepository.getProducts(
                category = _uiState.value.selectedCategory,
                search = _uiState.value.searchQuery,
                sort = _uiState.value.selectedSort
            )
            _uiState.update {
                it.copy(
                    products = products,
                    isLoading = false,
                    isOffline = isOffline,
                    errorMessage = if (products.isEmpty() && isOffline) "No offline cached products available." else null
                )
            }
        }
    }

    fun selectCategory(category: String) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.update { it.copy(selectedCategory = category) }
            loadProducts()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            loadProducts()
        }
    }

    fun selectSort(sort: String?) {
        _uiState.update { it.copy(selectedSort = sort) }
        loadProducts()
    }
}
