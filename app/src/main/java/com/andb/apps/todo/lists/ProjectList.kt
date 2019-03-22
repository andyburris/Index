package com.andb.apps.todo.lists

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andb.apps.todo.databases.GetDatabase
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.OnceHolder
import com.pixplicity.easyprefs.library.Prefs

object ProjectList {
    private var currentKey = MutableLiveData<Int>()

    fun getKey(): LiveData<Int>{
        return currentKey
    }

    @JvmStatic
    fun setKey(key: Int){
        currentKey.value = key
        Prefs.putInt("project_viewing", key)
    }

    fun postKey(key: Int){
        currentKey.postValue(key)
        Prefs.putInt("project_viewing", key)
    }


}
