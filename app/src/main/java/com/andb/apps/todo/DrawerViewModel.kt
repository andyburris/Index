package com.andb.apps.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.todo.databases.projectsDao
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.utilities.Current

class DrawerViewModel : ViewModel(){

    val projects: LiveData<List<Project>> = projectsDao().all
    val projectsBuffer = ArrayList<Project>()


    fun getCurrentName(viewing: Int = Current.projectKey()): String{
        return getProject(viewing).name
    }

    fun getProject(viewing: Int = Current.projectKey()): Project{
        return projectsBuffer.first { it.key==viewing }
    }

    fun getProjectIndexed(index: Int): Project{
        return projectsBuffer[index]
    }

}