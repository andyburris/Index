package com.andb.apps.todo.ui.inbox

import android.content.Context
import androidx.lifecycle.ViewModel
import com.andb.apps.todo.data.model.Tag
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.data.repository.TagsRepository
import com.andb.apps.todo.data.repository.TasksRepository
import com.andb.apps.todo.util.InitialLiveData
import com.andb.apps.todo.util.ListLiveData
import com.andb.apps.todo.util.filterEach
import com.google.android.gms.location.GeofencingClient
import com.snakydesign.livedataextensions.map
import com.snakydesign.livedataextensions.switchMap

class InboxViewModel(
        private val tasksRepository: TasksRepository,
        private val tagsRepository: TagsRepository
) : ViewModel() {
    private val filterMode = SORT_TIME
    private val tagStack = ListLiveData<Tag>()
    private val projectID = InitialLiveData(0)
    private val allTasks = projectID.map { id -> tasksRepository.getTasksByProject(id) }
    private val allTags = projectID.map { id -> tagsRepository.getTagsByProject(id) }

    val tasks = tagStack.switchMap { selectedTags ->
        allTasks.filterEach { task ->
            val tagIDs = selectedTags.map { it.id }
            task.listTags.containsAll(tagIDs)
        }
    }

    val tags = tagStack.switchMap { selectedTags ->
        if (selectedTags.isEmpty()) return@switchMap ListLiveData<Tag>()
        allTags.filterEach { selectedTags.last().children.contains(it.id) }
    }

    val path = tagStack.map { list -> list.joinToString("/") { it.name } }

    fun selectProject(id: Int) {
        tagStack.clear()
        projectID.value = id
    }

    fun nextTag(tag: Tag) {
        tagStack.add(tag)
    }

    fun back() {
        tagStack.drop(1)
    }

    fun isBackPossible() = tagStack.isNotEmpty()

    fun archiveTask(task: Task) {
        tasksRepository.updateTask(task.copy(isArchived = true))
    }

    fun removeGeofences(ctxt: Context?, task: Task) {

        if (ctxt == null) {
            return
        }
        if (task.locationReminders.isNotEmpty()) {
            GeofencingClient(ctxt).removeGeofences(task.locationReminders.map { it.key.toString() })
            GeofencingClient(ctxt).removeGeofences(task.locationReminders.mapNotNull { it.trigger?.key.toString() })
        }

    }
}

