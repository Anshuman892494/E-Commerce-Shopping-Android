package e_commerce_app.com.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import e_commerce_app.com.ui.components.ErrorView
import e_commerce_app.com.ui.components.LoadingView
import e_commerce_app.com.ui.components.StockBadge
import e_commerce_app.com.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    cartItemCount: Int,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isAddedToCart) {
        if (uiState.isAddedToCart) {
            snackbarHostState.showSnackbar(
                message = "Item added to your shopping cart!",
                duration = SnackbarDuration.Short
            )
            viewModel.resetCartFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::toggleWishlist,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save to Wishlist",
                            tint = if (uiState.isWishlisted) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartItemCount")
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "View Cart")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val product = uiState.product
            if (product != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity Stepper (if in stock)
                        if (product.stock > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = viewModel::decrementQuantity,
                                    enabled = uiState.quantity > 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
                                }
                                Text(
                                    text = "${uiState.quantity}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                IconButton(
                                    onClick = viewModel::incrementQuantity,
                                    enabled = uiState.quantity < product.stock,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        // Add to Cart Button
                        Button(
                            onClick = { viewModel.addToCart() },
                            enabled = product.stock > 0,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Icon(
                                imageVector = if (product.stock > 0) Icons.Default.AddShoppingCart else Icons.Default.Block,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (product.stock > 0) "Add to Cart • ₹${"%.2f".format(product.price * uiState.quantity)}" else "Out of Stock",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView(text = "Loading product details...")
                }
                uiState.errorMessage != null -> {
                    ErrorView(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::loadProduct
                    )
                }
                uiState.product != null -> {
                    val product = uiState.product!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Product Hero Image with stock badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            StockBadge(
                                stock = product.stock,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                            )
                        }

                        // Details Container
                        Card(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-16).dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = product.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = product.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Rating and Review Count
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${product.rating}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${product.reviewCount} customer reviews)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Price row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${"%.2f".format(product.price)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (product.originalPrice > product.price) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "₹${"%.2f".format(product.originalPrice)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                textDecoration = TextDecoration.LineThrough
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Color Variants
                                if (product.variants.colors.isNotEmpty()) {
                                    Text(
                                        text = "Select Color: ${uiState.selectedColor ?: ""}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        product.variants.colors.forEach { colorVariant ->
                                            val isSelected = uiState.selectedColor == colorVariant.name
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(parseHexColor(colorVariant.hex))
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.selectColor(colorVariant.name) }
                                                    .semantics {
                                                        contentDescription = "Color ${colorVariant.name}"
                                                    }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                // Size Variants
                                if (product.variants.sizes.isNotEmpty()) {
                                    Text(
                                        text = "Select Size / Option: ${uiState.selectedSize ?: ""}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        product.variants.sizes.forEach { size ->
                                            val isSelected = uiState.selectedSize == size
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.selectSize(size) },
                                                label = { Text(size) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                // Stock availability description
                                Surface(
                                    color = if (product.stock > 0) SuccessGreenContainer else ErrorRedContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (product.stock > 0) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (product.stock > 0) SuccessGreen else ErrorRed
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (product.stock > 3) {
                                                "In Stock (${product.stock} units available for fast delivery)"
                                            } else if (product.stock in 1..3) {
                                                "Hurry! Only ${product.stock} units left in stock."
                                            } else {
                                                "This item is currently out of stock."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (product.stock > 0) Color(0xFF166534) else Color(0xFF991B1B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Description
                                Text(
                                    text = "About this item",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = product.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hexString: String): Color {
    return try {
        val clean = hexString.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color.Gray
    }
}

