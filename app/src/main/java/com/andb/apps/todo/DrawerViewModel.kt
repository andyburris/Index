package com.andb.apps.todo

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.todo.databases.projectsDao
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.utilities.Current

class DrawerViewModel : ViewModel(){

    val projects: LiveData<List<Project>> = projectsDao().all
    val projectsBuffer = ArrayList<Project>()


    fun getCurrentName(viewing: Int = Current.viewing()): String{
        return getProject(viewing).name
    }

    fun getProject(viewing: Int = Current.viewing()): Project{
        return projectsBuffer.first { it.key==viewing }
    }

    fun getProjectIndexed(index: Int): Project{
        return projectsBuffer[index]
    }

}