package com.andb.apps.todo.objects.reminders

import com.andb.apps.todo.utilities.ProjectsUtils

const val ENTER_RADIUS = 15
const val EXIT_RADIUS = 15
const val ENTER_DURATION = 15000

const val TYPE_ENTER = 75287934
const val TYPE_EXIT = 89757632
const val TYPE_NEAR = 84565287


sealed class LocationFence(var lat: Double,
                           var long: Double,
                           var radius: Int,
                           val key: Int
) {

    var name: String = ""
    var trigger: LocationFence? = null

    class Enter(lat: Double, long: Double, val duration: Int = ENTER_DURATION, key: Int = ProjectsUtils.keyGenerator()) :
        LocationFence(lat, long, ENTER_RADIUS, key)

    class Exit(lat: Double, long: Double, radius: Int = EXIT_RADIUS, key: Int = ProjectsUtils.keyGenerator()) : LocationFence(lat, long, radius, key)
    class Near(lat: Double, long: Double, radius: Int, key: Int = ProjectsUtils.keyGenerator()) : LocationFence(lat, long, radius, key)

    fun toType(): Int {
        return when(this) {
            is LocationFence.Enter -> TYPE_ENTER
            is LocationFence.Exit -> TYPE_EXIT
            is LocationFence.Near -> TYPE_NEAR
        }
    }

    override fun toString(): String {
        return "${if (name.isNotEmpty()) name else "$lat, $long"} - ${radius}m"
    }
}

fun fenceToType(fence: LocationFence): Int {
    val type: Int

    if (fence is LocationFence.Enter) {
        type = TYPE_ENTER
    } else if (fence is LocationFence.Exit) {
        type = TYPE_EXIT
    } else { //LocationFence.Near
        type = TYPE_NEAR
    }

    return type
}