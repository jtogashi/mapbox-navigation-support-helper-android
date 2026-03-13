package cc.jtogashi.mapboxnavsupporthelper

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(this)
                    .build()
            }.attachAllActivities(this)
        }
    }
}