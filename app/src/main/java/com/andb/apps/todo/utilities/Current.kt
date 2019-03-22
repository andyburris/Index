package com.andb.apps.todo.utilities

import android.util.Log.d

import com.andb.apps.todo.databases.GetDatabase
import com.andb.apps.todo.databases.*
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks

import java.util.ArrayList

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.todo.filtering.filterProject
import com.andb.apps.todo.filtering.filterProjectTags

object Current {

    /**Used to get sizes and lookups from database without needing lifecycle and/or Asynctask */
    private val bufferTags = ArrayList<Tags>()
    private val bufferTasks = ArrayList<Tasks>()
    val bufferProjects = ArrayList<Project>()
    var bufferViewingKey = 0


    @JvmStatic
    fun initTags(lifecycleOwner: LifecycleOwner) {
        tagsDao().all.observe(lifecycleOwner, Observer{ tags ->
            bufferTags.clear()
            bufferTags.addAll(tags)
        })
    }

    @JvmStatic
    fun initTasks(lifecycleOwner: LifecycleOwner) {
        tasksDao().all.observe(lifecycleOwner, Observer{ tasks ->
            bufferTasks.clear()
            bufferTasks.addAll(tasks)
        })
    }

    @JvmStatic
    fun initProjects(lifecycleOwner: LifecycleOwner){

        projectsDao().all.observe(lifecycleOwner, Observer {projects->
            bufferProjects.clear()
            bufferProjects.addAll(projects)
        })
    }

    @JvmStatic
    fun initViewing(lifecycleOwner: LifecycleOwner){
        ProjectList.getKey().observe(lifecycleOwner, Observer { viewing->
            bufferViewingKey = viewing
        })
    }

    @JvmStatic
    fun initProjectsSync(db: ProjectsDatabase){
        bufferProjects.clear()
        bufferProjects.addAll(db.projectsDao().allStatic)
    }

    @JvmStatic
    fun allProjects(): ArrayList<Project> {
        //name projectList?
        return bufferProjects
    }

    @JvmStatic
    fun projectKey(): Int {
        return bufferViewingKey
    }

    @JvmStatic
    fun project(): Project {

        d("allProjects", "size = ${allProjects().size}, items = ${allProjects().joinToString { "name: ${it.name}, key: ${it.key}" }}, key to find = ${projectKey()}")
        return allProjects().first { it.key== projectKey() }
    }

    @JvmStatic
    fun keyList(): ArrayList<Int> {
        val keys = ArrayList<Int>()
        for (t in bufferTags) {
            keys.add(t.key)
        }
        for (t in bufferTasks) {
            keys.add(t.listKey)
        }
        for (p in allProjects()) {
            keys.add(p.key)
        }
        return keys
    }

    @JvmStatic
    fun database(): ProjectsDatabase {
        return GetDatabase.projectsDatabase
    }

    @JvmStatic
    fun hasProjects(): Boolean {
        return !allProjects().isEmpty()
    }

    @JvmStatic
    fun tagListAll(): List<Tags> {
        return bufferTags.filterProjectTags()
    }

    @JvmStatic
    @JvmOverloads
    fun taskListAll(id: Int = projectKey()): List<Tasks> {
        return bufferTasks.filterProject(id)
    }
}
