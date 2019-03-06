package com.andb.apps.todo.objects.reminders

import com.andb.apps.todo.views.LocationFence

class LocationReminder(val lat: Double, val long: Double, val radius: Int, val key: Int, var repeat: Int, var name: String = "") {

    override fun toString(): String {
        return "${if (name.isNotEmpty()) name else "$lat, $long"} - ${radius}m"
    }

    companion object {
        fun fromFence(locationFence: LocationFence): LocationReminder {
            return LocationReminder(locationFence.lat, locationFence.long, locationFence.radius, locationFence.key, 0)
        }
    }
}