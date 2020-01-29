package com.andb.apps.todo.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.andb.apps.todo.data.model.reminders.LocationFence
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest


fun requestFromFence(fence: LocationFence, context: Context) {
    Log.d("returnLocation", "Fence - type: ${fence.toType()} lat: ${fence.lat}, long: ${fence.long}, id: ${fence.key}, radius: ${fence.radius}")

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceHandler::class.java)
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    when (fence) {
        is LocationFence.Enter -> {
            val builtFence = buildFence(fence, GeofencingRequest.INITIAL_TRIGGER_DWELL, duration = fence.duration)
            try {
                GeofencingClient(context).addGeofences(getGeofencingRequest(builtFence, GeofencingRequest.INITIAL_TRIGGER_DWELL), geofencePendingIntent)
                Log.d("testGeofence", "geofence set")
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
        is LocationFence.Near -> {
            val builtFence = buildFence(fence, GeofencingRequest.INITIAL_TRIGGER_ENTER)
            try {
                GeofencingClient(context).addGeofences(getGeofencingRequest(builtFence, GeofencingRequest.INITIAL_TRIGGER_ENTER), geofencePendingIntent)
                Log.d("testGeofence", "geofence set")
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
        is LocationFence.Exit -> {
            val builtFence = buildFence(fence, GeofencingRequest.INITIAL_TRIGGER_EXIT)
            try {
                GeofencingClient(context).addGeofences(getGeofencingRequest(builtFence, GeofencingRequest.INITIAL_TRIGGER_EXIT), geofencePendingIntent)
                Log.d("testGeofence", "geofence set")
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}

fun removeFence(id: Int, context: Context){
    GeofencingClient(context).removeGeofences(listOf(id.toString()))
}

fun buildFence(locationFence: LocationFence, type: Int, duration: Int = -1): Geofence {
    return Geofence.Builder()
        .setRequestId(locationFence.key.toString())
        .setCircularRegion(locationFence.lat, locationFence.long, locationFence.radius.toFloat())
        .also { if (duration != -1) it.setLoiteringDelay(duration) }
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(type)
        .build()
}


private fun getGeofencingRequest(geofence: Geofence, trigger: Int): GeofencingRequest {
    return GeofencingRequest.Builder().apply {
        setInitialTrigger(trigger)
        addGeofence(geofence)
    }.build()
}
