package e_commerce_app.com

import android.app.Application
import e_commerce_app.com.di.AppContainer

class ECommerceApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

