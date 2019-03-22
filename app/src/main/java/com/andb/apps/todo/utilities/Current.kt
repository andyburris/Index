package com.andb.apps.todo.utilities

import android.app.Activity
import android.util.Log.d

import com.andb.apps.todo.TaskAdapter
import com.andb.apps.todo.databases.GetDatabase
import com.andb.apps.todo.databases.*
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks

import java.util.ArrayList

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.todo.filtering.filterProject
import com.andb.apps.todo.filtering.filterProjectTags

object Current {

    /**Used to get sizes and lookups from database without needing lifecycle and/or Asynctask */
    private val bufferTags = ArrayList<Tags>()
    private val bufferTasks = ArrayList<Tasks>()
    val bufferProjects = ArrayList<Project>()
    var bufferViewing = 0


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
        bufferProjects.clear()
        bufferProjects.addAll(projectsDao().allStatic)
        projectsDao().all.observe(lifecycleOwner, Observer {projects->
            bufferProjects.clear()
            bufferProjects.addAll(projects)
        })
    }

    @JvmStatic
    fun initViewing(lifecycleOwner: LifecycleOwner){
        ProjectList.getViewing().observe(lifecycleOwner, Observer { viewing->
            bufferViewing = viewing
        })
    }

    @JvmStatic
    fun initProjectsSync(db: ProjectsDatabase){
        bufferProjects.clear()
        bufferProjects.addAll(db.projectsDao().all!!.value as List<Project>)
    }

    @JvmStatic
    fun allProjects(): ArrayList<Project> {
        //name projectList?
        return bufferProjects
    }

    @JvmStatic
    fun viewing(): Int {
        return bufferViewing
    }

    @JvmStatic
    fun project(): Project {

        d("allProjects", "size = ${allProjects().size}, items = ${allProjects().joinToString { "name: ${it.name}, key: ${it.key}" }}, key to find = ${viewing()}")
        return allProjects().first { it.key== viewing() }
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
    fun taskListAll(id: Int = project().key): List<Tasks> {
        return bufferTasks.filterProject(id)
    }
}
