package e_commerce_app.com.data.model

data class ProductVariantColors(
    val name: String,
    val hex: String
)

data class ProductVariants(
    val colors: List<ProductVariantColors> = emptyList(),
    val sizes: List<String> = emptyList()
)

data class Product(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val originalPrice: Double,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    val description: String,
    val variants: ProductVariants = ProductVariants(),
    val stock: Int
)

data class ProductsResponse(
    val products: List<Product>,
    val total: Int,
    val categories: List<String>
)

