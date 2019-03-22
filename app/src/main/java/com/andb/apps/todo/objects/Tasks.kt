package com.andb.apps.todo.objects

import androidx.room.*
import com.andb.apps.todo.objects.reminders.LocationFence
import com.andb.apps.todo.objects.reminders.SimpleReminder
import com.andb.apps.todo.typeconverters.*
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Values.SORT_TIME
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import java.util.ArrayList
import kotlin.Boolean
import kotlin.Cloneable
import kotlin.Comparator
import kotlin.Int
import kotlin.NoSuchElementException
import kotlin.String

@Entity(foreignKeys = [ForeignKey(entity = Project::class, parentColumns = ["key"], childColumns = ["project_id"], onDelete = ForeignKey.CASCADE)])
class Tasks : Cloneable {
    @SerializedName("key")
    @Expose
    @PrimaryKey
    val listKey: Int

    @SerializedName("list_name")
    @Expose
    @ColumnInfo(name = "list_name")
    var listName: String

    @SerializedName("list_items")
    @Expose
    @TypeConverters(ItemsConverter::class)
    @ColumnInfo(name = "list_items")
    val listItems: ArrayList<String>

    @SerializedName("list_items_checked")
    @Expose
    @TypeConverters(CheckedConverter::class)
    @ColumnInfo(name = "list_items_checked")
    val listItemsChecked: ArrayList<Boolean>

    @SerializedName("list_tags")
    @Expose
    @TypeConverters(TagConverter::class)
    @ColumnInfo(name = "list_tags")
    val listTags: ArrayList<Int>

    @SerializedName("list_times")
    @Expose
    @TypeConverters(SimpleReminderConverter::class)
    @ColumnInfo(name = "list_times")
    val timeReminders: ArrayList<SimpleReminder>

    @SerializedName("list_locations")
    @Expose
    @TypeConverters(LocationFenceConverter::class)
    @ColumnInfo(name = "list_locations")
    val locationReminders: ArrayList<LocationFence>

    @SerializedName("project_id")
    @Expose
    @ColumnInfo(name = "project_id")
    var projectId: Int

    @SerializedName("archived")
    @Expose
    @ColumnInfo(name = "archived")
    var isArchived: Boolean

    @Ignore
    var isEditing = false


    val isListItems: Boolean
        get() = !listItems.isEmpty()

    val listTagsSize: Int
        get() = listTags.size

    val isListTags: Boolean
        get() = !listTags.isEmpty()


    @Ignore
    @JvmOverloads
    constructor(listName: String, listItems: ArrayList<String>, listItemsChecked: ArrayList<Boolean>, listTags: ArrayList<Int>, timeReminders: ArrayList<SimpleReminder>,
                locationReminders: ArrayList<LocationFence>, projectId: Int = Current.projectKey(), archived: Boolean = false) : this(listName, listItems, listItemsChecked, listTags, timeReminders, locationReminders, ProjectsUtils.keyGenerator(), projectId, archived)

    constructor(listName: String, listItems: ArrayList<String>, listItemsChecked: ArrayList<Boolean>, listTags: ArrayList<Int>, timeReminders: ArrayList<SimpleReminder>,
                locationReminders: ArrayList<LocationFence>, listKey: Int, projectId: Int, archived: Boolean) {
        this.listKey = listKey
        this.listName = listName
        this.listItems = listItems
        this.listItemsChecked = listItemsChecked
        this.listTags = listTags
        this.timeReminders = timeReminders
        this.locationReminders = locationReminders
        this.projectId = projectId
        this.isArchived = archived
    }

    fun editListItemsChecked(checked: Boolean, pos: Int) {
        this.listItemsChecked[pos] = checked
    }


    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(listName)
        builder.append(", " + nextReminderTime().toString("MMMM d, h:mm") + "\n")
        for (s in listItems) {
            builder.append("- $s\n")
        }
        if(listTags.isNotEmpty()) {
            builder.append("Tags: \n")
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

    fun hasLocationOrTrigger(locationKey: String): Boolean{
        val reminders: Boolean = locationReminders.map { it.key }.contains(locationKey.toInt())
        val triggers: Boolean = locationReminders.map { it.trigger?.key ?: -1 }.contains(locationKey.toInt())
        return reminders || triggers
    }

    fun findLocation(locationKey: String, isTrigger: Boolean = isTrigger(locationKey)): LocationFence{
        return if(isTrigger){
            locationReminders.first { it.trigger?.key.toString()==locationKey }
        }else{
            locationReminders.first { it.key.toString()==locationKey }
        }
    }

    fun isTrigger(locationKey: String): Boolean{
        return locationReminders.map { it.trigger?.key ?: -1 }.contains(locationKey.toInt())
    }

    fun compareTo(o: Tasks, filterMode: Int): Int {
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


    private fun compareTimes(o: Tasks): Int {
        return this.nextReminderTime().compareTo(o.nextReminderTime())
    }

    private fun compareEditing(o: Tasks): Int {
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

    private fun compareAlphabetical(o: Tasks): Int {
        return this.listName.compareTo(o.listName)
    }

    private fun compareLists(o: Tasks): Int {
        return Integer.compare(this.listItems.size, o.listItems.size)
    }

    fun withKey(key: Int): Tasks {
        return Tasks(
            listName, listItems, listItemsChecked, listTags, timeReminders
                ?: ArrayList(), locationReminders ?: ArrayList(), key, projectId, isArchived
        )
    }


}
