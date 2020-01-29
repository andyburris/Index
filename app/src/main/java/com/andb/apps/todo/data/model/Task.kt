package com.andb.apps.todo.data.model

import androidx.room.*
import com.andb.apps.todo.ui.inbox.SORT_TIME
import com.andb.apps.todo.data.model.reminders.LocationFence
import com.andb.apps.todo.data.model.reminders.SimpleReminder
import org.joda.time.DateTime
import java.util.ArrayList
import kotlin.Comparator

@Entity
data class Task (
        @PrimaryKey
        val listKey: Int,
        var listName: String,
        val listItems: ArrayList<String>,
        val listItemsChecked: ArrayList<Boolean>,
        val listTags: ArrayList<Int>,
        val timeReminders: ArrayList<SimpleReminder>,
        val locationReminders: ArrayList<LocationFence>,
        val projectId: Int,
        val isArchived: Boolean
) {

    @Ignore
    var isEditing = false

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(listName)
        builder.append(", " + nextReminderTime().toString("MMMM d, h:mm") + "\n")
        for (s in listItems) {
            builder.append("- $s\n")
        }
        if (listTags.isNotEmpty()) {
            builder.append("Tag: \n")
        }
        for (i in listTags) {
            builder.append("- Tag $i\n")
        }
        return builder.toString()
    }

    fun nextReminderTime(): DateTime {
        return nextReminder()?.asDateTime() ?: DateTime(3000, 1, 1, 0, 0, 59)
    }

    fun nextReminder(): SimpleReminder? {
        val sorted = timeReminders.sortedWith(Comparator { o1, o2 ->
            o1.asDateTime().compareTo(o2.asDateTime())
        })
        return try {
            sorted.first { !it.notified }
        } catch (e: NoSuchElementException) {
            if (!timeReminders.isEmpty()) {
                sorted[0]
            } else {
                null
            }
        }

    }

    fun hasLocationOrTrigger(locationKey: String): Boolean {
        val reminders: Boolean = locationReminders.map { it.key }.contains(locationKey.toInt())
        val triggers: Boolean = locationReminders.map { it.trigger?.key ?: -1 }
            .contains(locationKey.toInt())
        return reminders || triggers
    }

    fun findLocation(locationKey: String, isTrigger: Boolean = isTrigger(locationKey)): LocationFence {
        return if (isTrigger) {
            locationReminders.first { it.trigger?.key.toString() == locationKey }
        } else {
            locationReminders.first { it.key.toString() == locationKey }
        }
    }

    fun isTrigger(locationKey: String): Boolean {
        return locationReminders.map { it.trigger?.key ?: -1 }.contains(locationKey.toInt())
    }

    fun compareTo(o: Task, filterMode: Int): Int {
        if (filterMode == SORT_TIME) {
            return if (compareTimes(o) != 0) {
                compareTimes(o)
            } else if (compareEditing(o) != 0) {
                compareEditing(o)
            } else if (compareAlphabetical(o) != 0) {
                compareAlphabetical(o)
            } else {
                compareLists(o)
            }

        } else {//Alphabetical sort
            return if (compareEditing(o) != 0) {
                compareEditing(o)
            } else if (compareAlphabetical(o) != 0) {
                compareAlphabetical(o)
            } else if (compareTimes(o) != 0) {
                compareTimes(o)
            } else {
                compareLists(o)
            }
        }
    }


    private fun compareTimes(o: Task): Int {
        return this.nextReminderTime().compareTo(o.nextReminderTime())
    }

    private fun compareEditing(o: Task): Int {
        return if (this.isEditing != o.isEditing) {
            if (this.isEditing) {
                1
            } else {
                -1
            }
        } else {
            0
        }


    }

    private fun compareAlphabetical(o: Task): Int {
        return this.listName.compareTo(o.listName)
    }

    private fun compareLists(o: Task): Int {
        return Integer.compare(this.listItems.size, o.listItems.size)
    }

}
