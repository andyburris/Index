package com.andb.apps.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.filtering.filterProject
import com.andb.apps.todo.objects.Tasks
import java.util.*

class TaskViewViewModel(val key: Int) : ViewModel() {

    val viewModelTask: LiveData<Tasks> = Transformations.map(tasksDao().all) { tasks ->
        tasks.filterProject().first { it.listKey == key }
    }

    fun reorder(from: Int, to: Int){

        val task = viewModelTask.value

        if (from < to) {
            for (i in from until to) {
                Collections.swap(viewModelTask.value?.listItems, i, i + 1)
                Collections.swap(viewModelTask.value?.listItemsChecked, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(viewModelTask.value?.listItems, i, i - 1)
                Collections.swap(viewModelTask.value?.listItemsChecked, i, i - 1)
            }
        }

        //viewModelTask.value = task
    }
}