package com.andb.apps.todo.filtering

import com.andb.apps.todo.SORT_TIME
import com.andb.apps.todo.TaskAdapter
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.util.ArrayList
import kotlin.Comparator

val END_OF_DAY = LocalTime(23, 59, 59, 999)
val OVERDUE = TaskAdapter.newDivider("OVERDUE", DateTime(1970, 1, 1, 0, 0))
val TODAY = TaskAdapter.newDivider("TODAY", DateTime(DateTime.now()))
val THIS_WEEK = TaskAdapter.newDivider("WEEK", DateTime(DateTime.now().withTime(END_OF_DAY)))
val THIS_MONTH = TaskAdapter.newDivider("MONTH", DateTime(DateTime.now().plusWeeks(1).minusDays(1).withTime(END_OF_DAY)))
val FUTURE = TaskAdapter.newDivider("FUTURE", DateTime(DateTime.now().plusMonths(1).minusDays(1).withTime(END_OF_DAY)))
val HEADER = TaskAdapter.newHeader()

/*********Task List************/

@JvmOverloads
fun List<Tasks>.filterInbox(filterMode: Int, filters: List<Tags> = Filters.getCurrentFilter()): List<Tasks> {

    System.out.println("projectKey: " + Current.projectKey())

    val mutableList = this.filterProject().filterArchive()
        .filter { task ->
            task.listTags.containsAll(filters.map { it.index })
                    && !listOf(END_OF_DAY, OVERDUE, TODAY, THIS_WEEK, THIS_MONTH, FUTURE, HEADER).contains(task)
        }
        .toMutableList()


    if (filterMode == SORT_TIME) {
        if (mutableList.any { tasks ->
                tasks.nextReminderTime().isBefore(TODAY.nextReminderTime())
            }) {
            mutableList.add(OVERDUE)
        }
        if (mutableList.any { tasks -> tasks.nextReminderTime().isAfter(TODAY.nextReminderTime()) && tasks.nextReminderTime().isBefore(THIS_WEEK.nextReminderTime()) }) {
            mutableList.add(TODAY)
        }
        if (mutableList.any { tasks -> tasks.nextReminderTime().isAfter(THIS_WEEK.nextReminderTime()) && tasks.nextReminderTime().isBefore(THIS_MONTH.nextReminderTime()) }) {
            mutableList.add(THIS_WEEK)
        }
        if (mutableList.any { tasks -> tasks.nextReminderTime().isAfter(THIS_MONTH.nextReminderTime()) && tasks.nextReminderTime().isBefore(FUTURE.nextReminderTime()) }) {
            mutableList.add(THIS_MONTH)
        }
        if (mutableList.any { tasks ->
                tasks.nextReminderTime().isAfter(FUTURE.nextReminderTime())
            }) {
            mutableList.add(FUTURE)
        }
    }

    return mutableList.also {
        it.sortWith(Comparator { t1, t2 ->
            t1.compareTo(t2, filterMode)
        })
        it.add(0, HEADER)
    }
}


@JvmOverloads
fun List<Tasks>.filterProject(id: Int = Current.projectKey()): List<Tasks> {
    return this.filter { it.projectId == id }
}

@JvmOverloads
fun List<Tasks>.filterArchive(isArchived: Boolean = false): List<Tasks> {
    return this.filterProject().filter { task ->
        task.isArchived == isArchived
    }
}


/*********Tag List************/

@JvmOverloads
fun List<Tags>.filterProjectTags(id: Int = Current.projectKey()): List<Tags> {
    return this.filter { it.projectId == id }
}


@JvmOverloads
fun List<Tags>.filterTags(filters: ArrayList<Tags> = Filters.getCurrentFilter()): List<Tags> {
    return this.filterProjectTags()
        .filter { if (filters.isNotEmpty()) filters.last().children.contains(it.index) && !filters.contains(it) else true }
        .sortedBy { it.index }
}
