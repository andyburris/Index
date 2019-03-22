package com.andb.apps.todo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.filtering.filterInbox
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Values
import com.google.android.gms.location.GeofencingClient

class InboxViewModel : ViewModel() {
    private val filterMode = SORT_TIME
    private val inboxTaskList = MediatorLiveData<List<Tasks>>()

    init {
        inboxTaskList.addSource(tasksDao().all) { tasks ->
            inboxTaskList.value = tasks.filterInbox(filterMode)
        }
        inboxTaskList.addSource(ProjectList.getKey()){ viewing->
            inboxTaskList.refresh()
        }
        inboxTaskList.addSource(Filters.filterObserver) {
            inboxTaskList.refresh()
        }
    }

    fun getTasks(): LiveData<List<Tasks>> = inboxTaskList

    fun archiveTask(ctxt: Context?, task: Tasks) {
        task.isArchived = true
        removeGeofences(ctxt, task)
        ProjectsUtils.update(task)
    }

    fun removeGeofences(ctxt: Context?, task: Tasks) {

            if(ctxt == null){
                return
            }
            if (task.locationReminders.isNotEmpty()) {
                GeofencingClient(ctxt).removeGeofences(task.locationReminders.map { it.key.toString() })
                GeofencingClient(ctxt).removeGeofences(task.locationReminders.mapNotNull { it.trigger?.key.toString() })
            }

    }

    internal fun MediatorLiveData<List<Tasks>>.refresh(){
        value = value?.filterInbox(filterMode)
    }

}

