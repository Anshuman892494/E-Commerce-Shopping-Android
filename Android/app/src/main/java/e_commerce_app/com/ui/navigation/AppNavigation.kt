package e_commerce_app.com.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import e_commerce_app.com.di.AppContainer
import e_commerce_app.com.ui.screens.auth.AuthScreen
import e_commerce_app.com.ui.screens.auth.AuthViewModel
import e_commerce_app.com.ui.screens.cart.CartScreen
import e_commerce_app.com.ui.screens.cart.CartViewModel
import e_commerce_app.com.ui.screens.catalog.CatalogScreen
import e_commerce_app.com.ui.screens.catalog.CatalogViewModel
import e_commerce_app.com.ui.screens.checkout.CheckoutScreen
import e_commerce_app.com.ui.screens.checkout.CheckoutViewModel
import e_commerce_app.com.ui.screens.detail.ProductDetailScreen
import e_commerce_app.com.ui.screens.detail.ProductDetailViewModel
import e_commerce_app.com.ui.screens.orders.OrderHistoryScreen
import e_commerce_app.com.ui.screens.orders.OrderTrackingScreen
import e_commerce_app.com.ui.screens.orders.OrdersViewModel
import e_commerce_app.com.ui.screens.profile.ProfileScreen
import e_commerce_app.com.ui.screens.profile.ProfileViewModel
import e_commerce_app.com.ui.screens.splash.SplashScreen
import e_commerce_app.com.ui.screens.wishlist.WishlistScreen
import e_commerce_app.com.ui.screens.wishlist.WishlistViewModel
import e_commerce_app.com.ui.theme.OrangeCTA

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Catalog : Screen("catalog")
    object Wishlist : Screen("wishlist")
    object Cart : Screen("cart")
    object OrderHistory : Screen("order_history")
    object Profile : Screen("profile")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Checkout : Screen("checkout")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun AppNavigation(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    // Shared Cart items count for Badges
    val cartItems by container.cartRepository.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    // Shared Wishlist count for Badges
    val wishlistItems by container.wishlistRepository.wishlist.collectAsState()
    val wishlistCount = wishlistItems.size

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavTabs = listOf(
        BottomNavItem(Screen.Catalog.route, "Shop", Icons.Default.Home),
        BottomNavItem(Screen.Wishlist.route, "Wishlist", Icons.Default.Favorite, badgeCount = wishlistCount),
        BottomNavItem(Screen.Cart.route, "Cart", Icons.Default.ShoppingCart, badgeCount = cartCount),
        BottomNavItem(Screen.OrderHistory.route, "Orders", Icons.Default.ReceiptLong),
        BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
    )

    val showBottomBar = currentRoute in bottomNavTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (tab.badgeCount > 0) {
                                            Badge(
                                                containerColor = OrangeCTA,
                                                contentColor = Color.White
                                            ) {
                                                Text(tab.badgeCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                }
                            },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- 0. Splash Screen ---
            composable(Screen.Splash.route) {
                SplashScreen(
                    authRepository = container.authRepository,
                    onNavigateNext = { isLoggedIn ->
                        val target = if (isLoggedIn) Screen.Catalog.route else Screen.Auth.route
                        navController.navigate(target) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- 1. Authentication Screen ---
            composable(Screen.Auth.route) {
                val authVm = remember { AuthViewModel(container.authRepository) }
                AuthScreen(
                    viewModel = authVm,
                    onAuthSuccess = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onSkipGuest = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- 2. Catalog / Discovery Screen ---
            composable(Screen.Catalog.route) {
                val catalogVm = remember {
                    CatalogViewModel(
                        container.productRepository,
                        container.wishlistRepository,
                        container.networkObserver
                    )
                }
                CatalogScreen(
                    viewModel = catalogVm,
                    cartItemCount = cartCount,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }

            // --- 3. Wishlist Screen ---
            composable(Screen.Wishlist.route) {
                val wishlistVm = remember {
                    WishlistViewModel(container.wishlistRepository, container.cartRepository)
                }
                WishlistScreen(
                    viewModel = wishlistVm,
                    onNavigateToProduct = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onExploreProducts = {
                        navController.navigate(Screen.Catalog.route)
                    }
                )
            }

            // --- 4. Product Detail Screen ---
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val detailVm = remember(productId) {
                    ProductDetailViewModel(
                        productId,
                        container.productRepository,
                        container.cartRepository,
                        container.wishlistRepository
                    )
                }
                ProductDetailScreen(
                    viewModel = detailVm,
                    cartItemCount = cartCount,
                    onBackClick = { navController.popBackStack() },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }

            // --- 5. Cart Screen ---
            composable(Screen.Cart.route) {
                val cartVm = remember { CartViewModel(container.cartRepository) }
                CartScreen(
                    viewModel = cartVm,
                    onBackClick = { navController.popBackStack() },
                    onStartShopping = {
                        navController.navigate(Screen.Catalog.route)
                    },
                    onProceedToCheckout = {
                        navController.navigate(Screen.Checkout.route)
                    }
                )
            }

            // --- 6. Checkout & Payment Screen ---
            composable(Screen.Checkout.route) {
                val checkoutVm = remember {
                    CheckoutViewModel(container.orderRepository, container.cartRepository, container.authRepository)
                }
                CheckoutScreen(
                    viewModel = checkoutVm,
                    onBackClick = { navController.popBackStack() },
                    onOrderSuccess = { confirmedOrder ->
                        navController.navigate(Screen.OrderTracking.createRoute(confirmedOrder.id)) {
                            popUpTo(Screen.Catalog.route) { inclusive = false }
                        }
                    }
                )
            }

            // --- 7. Order Tracking & Confirmation Screen ---
            composable(
                route = Screen.OrderTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val ordersVm = remember { OrdersViewModel(container.orderRepository) }
                val ordersUiState by ordersVm.uiState.collectAsState()

                LaunchedEffect(orderId) {
                    ordersVm.loadOrderById(orderId)
                }

                val currentOrder = ordersUiState.selectedOrder
                    ?: ordersUiState.orders.find { it.id == orderId }

                if (currentOrder != null) {
                    OrderTrackingScreen(
                        order = currentOrder,
                        onBackToCatalog = {
                            navController.navigate(Screen.Catalog.route) {
                                popUpTo(Screen.Catalog.route) { inclusive = true }
                            }
                        },
                        onViewAllOrders = {
                            navController.navigate(Screen.OrderHistory.route)
                        }
                    )
                }
            }

            // --- 8. Order History Screen ---
            composable(Screen.OrderHistory.route) {
                val ordersVm = remember { OrdersViewModel(container.orderRepository) }
                OrderHistoryScreen(
                    viewModel = ordersVm,
                    onBackClick = { navController.popBackStack() },
                    onOrderClick = { order ->
                        navController.navigate(Screen.OrderTracking.createRoute(order.id))
                    },
                    onStartShopping = {
                        navController.navigate(Screen.Catalog.route)
                    }
                )
            }

            // --- 9. Profile Screen ---
            composable(Screen.Profile.route) {
                val profileVm = remember {
                    ProfileViewModel(container.authRepository, container.isDarkMode)
                }
                ProfileScreen(
                    viewModel = profileVm,
                    onNavigateToOrders = {
                        navController.navigate(Screen.OrderHistory.route)
                    },
                    onNavigateToWishlist = {
                        navController.navigate(Screen.Wishlist.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
