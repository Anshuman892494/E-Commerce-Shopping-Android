package e_commerce_app.com.ui.navigation

import androidx.compose.runtime.*
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

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Catalog : Screen("catalog")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
    object OrderHistory : Screen("order_history")
}

@Composable
fun AppNavigation(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    // Shared Cart items count for TopBar badge
    val cartItems by container.cartRepository.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    val startDestination = if (container.authRepository.isLoggedIn()) Screen.Catalog.route else Screen.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
                CatalogViewModel(container.productRepository, container.networkObserver)
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

        // --- 3. Product Detail Screen ---
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val detailVm = remember(productId) {
                ProductDetailViewModel(productId, container.productRepository, container.cartRepository)
            }
            ProductDetailScreen(
                viewModel = detailVm,
                cartItemCount = cartCount,
                onBackClick = { navController.popBackStack() },
                onCartClick = { navController.navigate(Screen.Cart.route) }
            )
        }

        // --- 4. Cart Screen ---
        composable(Screen.Cart.route) {
            val cartVm = remember { CartViewModel(container.cartRepository) }
            CartScreen(
                viewModel = cartVm,
                onBackClick = { navController.popBackStack() },
                onStartShopping = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                },
                onProceedToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                }
            )
        }

        // --- 5. Checkout & Payment Screen ---
        composable(Screen.Checkout.route) {
            val checkoutVm = remember {
                CheckoutViewModel(container.orderRepository, container.cartRepository)
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

        // --- 6. Order Tracking & Confirmation Screen ---
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

        // --- 7. Order History Screen ---
        composable(Screen.OrderHistory.route) {
            val ordersVm = remember { OrdersViewModel(container.orderRepository) }
            OrderHistoryScreen(
                viewModel = ordersVm,
                onBackClick = { navController.popBackStack() },
                onOrderClick = { order ->
                    navController.navigate(Screen.OrderTracking.createRoute(order.id))
                },
                onStartShopping = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

