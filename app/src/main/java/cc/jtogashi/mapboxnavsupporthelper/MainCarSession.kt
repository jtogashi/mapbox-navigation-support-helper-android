package cc.jtogashi.mapboxnavsupporthelper

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.ContextMode
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.mapboxMapInstaller
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.core.trip.session.TripSessionStateObserver
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.map.MapboxCarMapLoader
import com.mapbox.navigation.ui.androidauto.map.compass.CarCompassRenderer
import com.mapbox.navigation.ui.androidauto.map.logo.CarLogoRenderer
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens

@OptIn(MapboxExperimental::class)
class MainCarSession : Session() {

    private val mapboxCarMapLoader = MapboxCarMapLoader()

    private val mapboxCarMap = mapboxMapInstaller()
        .onCreated(mapboxCarMapLoader)
        .onResumed(CarLogoRenderer(), CarCompassRenderer())
        .install { carContext ->
            Log.i(TAG, "installing car map")
            MapInitOptions(
                context = carContext,
                mapOptions = MapOptions.Builder()
                    .contextMode(ContextMode.SHARED)
                    .build()
            )
        }

    private val mapboxCarContext = MapboxCarContext(lifecycle, mapboxCarMap)
        .prepareScreens()

    private val mapboxNavigation by requireMapboxNavigation()

    private val tripSessionStateObserver = object : TripSessionStateObserver {
        override fun onSessionStateChanged(tripSessionState: TripSessionState) {
            Log.i(TAG, "trip session changed to: ${tripSessionState.name}")
        }
    }

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapboxNavigation.registerTripSessionStateObserver(tripSessionStateObserver)
                Log.i(TAG, "attaching trip session")
                MapboxNavigationApp.attach(owner)
                checkLocationPermissions()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapboxNavigation.unregisterTripSessionStateObserver(tripSessionStateObserver)
                if (mapboxNavigation.getTripSessionState() == TripSessionState.STARTED) {
                    Log.i(TAG, "stopping trip session")
                    mapboxNavigation.stopTripSession()
                }
                Log.i(TAG, "detaching nav")
                MapboxNavigationApp.detach(owner)
            }
        })
    }

    override fun onCreateScreen(intent: Intent): Screen {
        val screenKey = MapboxScreenManager.current()?.key
        checkNotNull(screenKey) { "The screen key should be set before the Screen is requested." }
        return mapboxCarContext.mapboxScreenManager.createScreen(screenKey)
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        mapboxCarMapLoader.onCarConfigurationChanged(carContext)
    }

    @SuppressLint("MissingPermission")
    private fun checkLocationPermissions() {
        PermissionsManager.areLocationPermissionsGranted(carContext).also { isGranted ->
            val currentKey = MapboxScreenManager.current()?.key
            if (!isGranted) {
                MapboxScreenManager.replaceTop(MapboxScreen.NEEDS_LOCATION_PERMISSION)
            } else if (currentKey == null || currentKey == MapboxScreen.NEEDS_LOCATION_PERMISSION) {
                MapboxScreenManager.replaceTop(MapboxScreen.FREE_DRIVE)

                if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
                    Log.i(TAG, "starting trip session")
                    mapboxNavigation.startTripSession()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CarSession"
    }
}