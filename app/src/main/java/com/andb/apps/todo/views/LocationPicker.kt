package com.andb.apps.todo.views

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.andb.apps.todo.R
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.view_location_picker.view.*

const val ENTER_RADIUS = 15
const val EXIT_RADIUS = 15
const val ENTER_DURATION = 15000

sealed class LocationFence(var lat: Double, var long: Double, var radius: Int, val key:Int = ProjectsUtils.keyGenerator()) {
    class Enter(lat: Double, long: Double, val duration: Int = ENTER_DURATION) :
        LocationFence(lat, long, ENTER_RADIUS)

    class Exit(lat: Double, long: Double) : LocationFence(lat, long, EXIT_RADIUS)
    class Near(lat: Double, long: Double, radius: Int) : LocationFence(lat, long, radius)
}

class LocationPicker(context: Context) : ConstraintLayout(context), OnMapReadyCallback,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {

    var map: GoogleMap? = null
    lateinit var fence: LocationFence
    var nearRadius = 30
    private val geocoder by lazy { Geocoder(context) }
    lateinit var currentCircle: Circle

    init {
        inflate(context, R.layout.view_location_picker, this)
    }

    fun setup(bundle: Bundle , callback: (LocationFence) -> Unit) {
        Log.d("locationPickerSetup", "setting up")
        MapsInitializer.initialize(context)
        locationPickerMapView.apply {
            onCreate(bundle)
            getMapAsync(this@LocationPicker)
        }
        locationPickerTabLayout.apply {
            setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))//lighter since on CardView background

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    Log.d("tabSelectListener", "tab selected")
                    getFence()
                    extrasVisibility(selectedTabPosition)

                    setRadiusOnMap()
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })
            extrasVisibility(0)
        }

        locationRadiusPicker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.d("seekbarListener", "progress changed")
                nearRadius = progress+15
                locationRadiusPickerStatus.text = nearRadius.toString()

                setRadiusOnMap()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        locationPickerConfirm.setOnClickListener {
            val currentMap = map //to finalize for smart cast
            if (currentMap != null) {
                callback(fence)
            } else {
                Snackbar.make(this, R.string.location_map_not_loaded, Snackbar.LENGTH_LONG)
                    .also { it.animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
            }

        }
    }

    override fun onMapReady(map: GoogleMap?) {
        Log.d("mapReady", "map ready")
        val currentLocation = getCurrentLocation()
        val lat = currentLocation.latitude
        val long = currentLocation.longitude
        this.map = map
        fence = LocationFence.Enter(lat, long)
        map?.apply {
            uiSettings.isMyLocationButtonEnabled = true
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, long)))
            setOnCameraMoveListener(this@LocationPicker)
            setOnCameraIdleListener(this@LocationPicker)
            locationPickerMapView.onResume()
            setRadiusOnMap()
        }
    }

    fun getCurrentLocation(): Location {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return if (lm!=null &&ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: Location("").also { it.latitude = 0.0; it.longitude = 0.0 }
        } else {
            Location("").also {
                it.latitude = 0.0
                it.longitude = 0.0
            }
        }
    }

    fun getFence(lat: Double = fence.lat, long: Double = fence.long) {
        fence = when (locationPickerTabLayout.selectedTabPosition) {
            0 -> LocationFence.Enter(lat, long)
            1 -> LocationFence.Exit(lat, long)
            else -> LocationFence.Near(lat, long, nearRadius)
        }
    }

    private fun extrasVisibility(position: Int) {
        val height = if (position == 2) 1000 else 0
        TransitionManager.beginDelayedTransition(locationPickerFrame, ChangeBounds())
        locationRadiusPickerFrame.maxHeight = height
        locationRadiusPickerStatus.text = nearRadius.toString()
    }

    fun setRadiusOnMap() {
        map?.apply {
            if (::currentCircle.isInitialized) {
                currentCircle.remove()
            }

            if(locationPickerTabLayout.selectedTabPosition==2) {
                currentCircle = addCircle(
                    CircleOptions()
                        .center(LatLng(fence.lat, fence.long))
                        .radius(nearRadius.toDouble())
                        .strokeWidth(0f)
                        .fillColor(0x44000000)
                )
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewRemoved(view: View?) {
        locationPickerMapView.onViewRemoved(view)
    }

    override fun onCameraIdle() {
        val handler = Handler()

        Thread{
            val addresses = geocoder.getFromLocation(fence.lat, fence.long, 1)
            handler.post {
                locationPickerAddress.text = addresses.let { addresses ->
                    if (addresses.isEmpty()) {
                        context.getString(R.string.location_no_address)
                    } else {
                        val address = addresses[0]
                        val streetAddr = address.getAddressLine(0)
                        streetAddr
                    }
                }

            }
        }.start()

    }

    override fun onCameraMove() {
        map?.apply {
            fence.lat = cameraPosition.target.latitude
            fence.long = cameraPosition.target.longitude
            locationPickerMapScale.update(cameraPosition.zoom, cameraPosition.target.latitude)
        }

        setRadiusOnMap()

    }

}

fun View.setVisibility(boolean: Boolean) {
    visibility = Utilities.visibilityFromBoolean(boolean)
}