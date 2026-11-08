package e_commerce_app.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import e_commerce_app.com.ui.navigation.AppNavigation
import e_commerce_app.com.ui.theme.ECommerce_AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ECommerceApplication
        val container = app.container

        setContent {
            val isDarkMode by container.isDarkMode.collectAsState()
            ECommerce_AppTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(container = container)
                }
            }
        }
    }
}
