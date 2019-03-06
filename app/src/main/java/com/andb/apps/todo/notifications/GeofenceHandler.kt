package com.andb.apps.todo.notifications

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.andb.apps.todo.R
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent

class GeofenceHandler : IntentService("GeofenceHandler") {
    // ...
    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent.errorCode.toString()
            Log.e("geofenceFailed", errorMessage)
            return
        }

        Toast.makeText(this, "GEOFENCE RECEIVED", Toast.LENGTH_LONG).show()
        //sendNotification(this, "GEOFENCE RECEIVED")
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.


        // Get the geofences that were triggered. A single event can trigger
        // multiple geofences.
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        // Get the transition details as a String.
        val geofenceTransitionDetails: String = getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences)


        for(geofence in triggeringGeofences) {
            val task: Tasks = findTaskFromLocationReminder(geofence)

            // Send notification and log the transition details.
            NotificationHandler.createNotification(task, this)
            GeofencingClient(this).removeGeofences(geofencingEvent.triggeringGeofences.map { it.requestId })
            Log.i("geofenceSuccess", geofenceTransitionDetails)
        }

    }


    fun findTaskFromLocationReminder(geofence: Geofence): Tasks{
        return Current.allProjects().flatMap { it.taskList }.first { it.locationReminders.map { it.key }.contains(geofence.requestId.toInt()) }
    }

    fun getGeofenceTransitionDetails(transitionType: Int, triggers: List<Geofence>): String {
        val string = "Geofence: ${geofenceTypeToString(transitionType)}, triggers: " + triggers.map { geofence -> geofence.requestId.toString() }

        return string
    }

    fun geofenceTypeToString(type: Int): String {
        return when (type) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "GEOFENCE_TRANSITION_ENTER"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "GEOFENCE_TRANSITION_DWELL"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "GEOFENCE_TRANSITION_EXIT"
            else -> "None"
        }
    }

}




