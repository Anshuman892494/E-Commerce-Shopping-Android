package e_commerce_app.com.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import e_commerce_app.com.data.model.Order
import e_commerce_app.com.ui.theme.SuccessGreen
import e_commerce_app.com.ui.theme.SuccessGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    order: Order,
    onBackToCatalog: () -> Unit,
    onViewAllOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Confirmation", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackToCatalog) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewAllOrders,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("All Orders", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onBackToCatalog,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Continue Shopping", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Success Card Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreenContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Order Placed Successfully!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Order ID: #${order.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimated Delivery: ${order.estimatedDelivery}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF166534)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Order Tracking Timeline
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Delivery Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    val isConfirmed = true
                    val isProcessing = order.status in listOf("PROCESSING", "SHIPPED", "DELIVERED")
                    val isShipped = order.status in listOf("SHIPPED", "DELIVERED")
                    val isDelivered = order.status == "DELIVERED"

                    TimelineStep(
                        icon = Icons.Default.CheckCircle,
                        title = "Order Placed",
                        subtitle = "Server confirmed payment and stock allocation",
                        isCompleted = isConfirmed,
                        isLast = false
                    )

                    TimelineStep(
                        icon = Icons.Default.Inventory,
                        title = "Packed & Prepared",
                        subtitle = if (isProcessing) "Items inspected & packed in warehouse" else "Warehouse awaiting dispatch",
                        isCompleted = isProcessing,
                        isLast = false
                    )

                    TimelineStep(
                        icon = Icons.Default.LocalShipping,
                        title = "Shipped",
                        subtitle = if (isShipped) "Carrier has picked up package" else "Carrier awaiting pickup",
                        isCompleted = isShipped,
                        isLast = false
                    )

                    TimelineStep(
                        icon = Icons.Default.Home,
                        title = "Delivered",
                        subtitle = if (isDelivered) "Package arrived at recipient destination" else "Delivery by ${order.estimatedDelivery}",
                        isCompleted = isDelivered,
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Details & Items
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery & Payment Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Ship to:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${order.shippingAddress.fullName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${order.shippingAddress.street}, ${order.shippingAddress.city} - ${order.shippingAddress.postalCode}", style = MaterialTheme.typography.bodySmall)
                    Text("Phone: ${order.shippingAddress.phone}", style = MaterialTheme.typography.bodySmall)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Payment Method:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${order.paymentMethod} (Total: ₹${"%.2f".format(order.grandTotal)})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TimelineStep(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

