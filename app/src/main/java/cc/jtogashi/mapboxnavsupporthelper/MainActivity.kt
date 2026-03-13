package cc.jtogashi.mapboxnavsupporthelper

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cc.jtogashi.mapboxnavsupporthelper.databinding.ActivityMainBinding
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.core.trip.session.TripSessionStateObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mapboxNavigation by requireMapboxNavigation()

    private val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            binding.mapMain.camera.easeTo(
                cameraOptions {
                    center(
                        Point.fromLngLat(
                            locationMatcherResult.enhancedLocation.longitude,
                        locationMatcherResult.enhancedLocation.latitude
                        )
                    )
                    zoom(14.0)
                }
            )
            navigationLocationProvider.changePosition(
                locationMatcherResult.enhancedLocation
            )
        }

        override fun onNewRawLocation(rawLocation: Location) {
        }
    }

    private val tripSessionStateObserver = object : TripSessionStateObserver {
        override fun onSessionStateChanged(tripSessionState: TripSessionState) {
            Log.i(TAG, "trip session changed to: ${tripSessionState.name}")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            mapMain.mapboxMap.flyTo(
                cameraOptions {
                    center(Point.fromLngLat(150.944, -33.8437))
                    zoom(12.0)
                }
            )

            mapMain.location.apply {
                locationPuck = LocationPuck2D(
                    bearingImage = ImageHolder.from(com.mapbox.navigation.R.drawable.mapbox_navigation_puck_icon)
                )
                setLocationProvider(navigationLocationProvider)
                puckBearingEnabled = true
                enabled = true
            }
        }

        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerTripSessionStateObserver(tripSessionStateObserver)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.fab.setOnClickListener { _ ->
            if (mapboxNavigation.getTripSessionState() == TripSessionState.STARTED) {
                Log.i(TAG, "stopping trip session")
                mapboxNavigation.stopTripSession()
            } else {
                Log.i(TAG, "starting trip session")
                mapboxNavigation.startTripSession()
            }
        }

    }

    override fun onDestroy() {
//        carConnection.type.removeObserver(carConnectionObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterTripSessionStateObserver(tripSessionStateObserver)

        if (mapboxNavigation.getTripSessionState() == TripSessionState.STARTED) {
            mapboxNavigation.stopTripSession()
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}