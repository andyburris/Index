package com.andb.apps.todo.ui.drawer

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.todo.databases.projectsDao
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.data.model.Project
import com.andb.apps.todo.utilities.Current

class DrawerViewModel : ViewModel(){

    val projectsAndKey = MediatorLiveData<Pair<List<Project>, Int>>()

    init {
        projectsAndKey.addSource(projectsDao().all){
            projectsAndKey.value = Pair(it, Current.projectKey())
        }
        projectsAndKey.addSource(ProjectList.getKey()){
            projectsAndKey.value = Pair(Current.allProjects(), it)
        }
    }
}