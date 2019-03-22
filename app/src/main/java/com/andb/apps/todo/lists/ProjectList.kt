package com.andb.apps.todo.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andb.apps.todo.databases.ProjectsDatabase
import com.pixplicity.easyprefs.library.Prefs

object ProjectList {
    private var viewing = MutableLiveData<Int>()

    fun getViewing(): LiveData<Int>{
        return viewing
    }

    @JvmStatic
    fun setViewing(key: Int){
        viewing.value = key
        Prefs.putInt("project_viewing", key)
    }

    fun postViewing(key: Int){
        viewing.postValue(key)
        Prefs.putInt("project_viewing", key)
    }

    fun appStart(db: ProjectsDatabase) {

        db.tasksDao().apply {//cleanse blank names from add & exit app
            val list = findTasksByName("")
            for(t in list){
                deleteTask(t)
            }

        }

    }
}
