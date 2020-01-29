package com.andb.apps.todo.notifications

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.andb.apps.todo.databases.GetDatabase
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.utilities.Current
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

        Log.d("fenceRecieved", "GEOFENCE RECEIVED")
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
            GeofencingClient(this).removeGeofences(listOf(geofence.requestId))
            val task = findTaskFromLocationReminder(geofence)
            val isTrigger = task.isTrigger(geofence.requestId)

            if(isTrigger){
                val reminder = task.findLocation(geofence.requestId, true)
                requestFromFence(reminder, this)
            }else {
                // Send notification and log the transition details.
                NotificationHandler.createNotification(task, this, geofence.requestId)
            }


            Log.i("geofenceSuccess", geofenceTransitionDetails)
        }

    }


    private fun findTaskFromLocationReminder(geofence: Geofence): Task {
        GetDatabase.database = GetDatabase.getDatabase(this)

        val allTasks = Current.taskListAll().filter { !it.isArchived }
        Log.d("fenceTask", "database search size: ${allTasks.size}")
        Log.d("fenceTask", "all reminder ids: ${allTasks.flatMap { it.locationReminders.map { it.key } } }")
        Log.d("fenceTask", "all trigger ids: ${allTasks.flatMap { it.locationReminders.mapNotNull { it.trigger?.key } } }")
        val task = allTasks.first{ it.hasLocationOrTrigger(geofence.requestId) }
        return task
    }

    private fun getGeofenceTransitionDetails(transitionType: Int, triggers: List<Geofence>): String {
        return "Geofence: ${geofenceTypeToString(transitionType)}, triggers: " + triggers.map { geofence -> geofence.requestId.toString() }
    }

    private fun geofenceTypeToString(type: Int): String {
        return when (type) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "GEOFENCE_TRANSITION_ENTER"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "GEOFENCE_TRANSITION_DWELL"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "GEOFENCE_TRANSITION_EXIT"
            else -> "None"
        }
    }

}




