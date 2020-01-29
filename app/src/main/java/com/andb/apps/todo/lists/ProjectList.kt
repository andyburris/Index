package com.andb.apps.todo.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
