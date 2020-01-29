package com.andb.apps.todo.ui.archive

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.filtering.filterArchive
import com.andb.apps.todo.notifications.requestFromFence
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.utilities.ProjectsUtils

class ArchiveViewModel : ViewModel() {
    private val archiveTaskList = MediatorLiveData<List<Task>>()

    init {
        archiveTaskList.addSource(tasksDao().all) { tasks ->
            archiveTaskList.value = tasks.filterArchive(true)
        }
        archiveTaskList.addSource(Filters.filterObserver) {
            archiveTaskList.value = archiveTaskList.value?.filterArchive(true)
        }
    }

    fun getTasks(): LiveData<List<Task>> = archiveTaskList

    fun delete(task: Task) {
        ProjectsUtils.remove(task)
    }

    fun restore(ctxt: Context?, task: Task){
        task.isArchived = false
        restoreGeofences(ctxt, task)
    }

    fun restoreGeofences(ctxt: Context?, task: Task){
        if(ctxt == null){
            return
        }
        if (task.locationReminders.isNotEmpty()) {
            for(l in task.locationReminders){
                val trigger = l.trigger
                if(trigger!=null){
                    requestFromFence(trigger, ctxt)
                }else{
                    requestFromFence(l, ctxt)
                }
            }
        }

    }
}