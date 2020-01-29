package com.andb.apps.todo

import androidx.lifecycle.ViewModel
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import java.util.*

class TaskViewViewModel(val key: Int) : ViewModel() {

    private val viewModelTask = Current.taskListAll().first { it.listKey == key }

    fun task(): Task = viewModelTask

    fun setChecked(position: Int, checked: Boolean){
        viewModelTask.listItemsChecked[position] = checked
        ProjectsUtils.update(viewModelTask)
    }

    fun reorder(from: Int, to: Int){

        val task = viewModelTask

        if (from < to) {
            for (i in from until to) {
                Collections.swap(task.listItems, i, i + 1)
                Collections.swap(task.listItemsChecked, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(task.listItems, i, i - 1)
                Collections.swap(task.listItemsChecked, i, i - 1)
            }
        }

        ProjectsUtils.update(task)

        //viewModelTask.value = task
    }

    fun setArchived(){
        viewModelTask.isArchived = true
        ProjectsUtils.update(viewModelTask)
    }

    fun setEditing(){
        viewModelTask.isEditing = true
        ProjectsUtils.update(viewModelTask)
    }
}