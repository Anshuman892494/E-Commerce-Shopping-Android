package e_commerce_app.com.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import e_commerce_app.com.data.repository.AuthRepository
import e_commerce_app.com.ui.theme.OrangeCTA
import e_commerce_app.com.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authRepository: AuthRepository,
    onNavigateNext: (isLoggedIn: Boolean) -> Unit
) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(1200)
        val loggedIn = authRepository.isLoggedIn()
        onNavigateNext(loggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Algions Shop Logo",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Algions ",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Shop",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeCTA
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart Shopping • Fast Delivery",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = OrangeCTA,
                strokeWidth = 3.dp
            )
        }
    }
}

