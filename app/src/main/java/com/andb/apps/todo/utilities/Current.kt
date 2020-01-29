package com.andb.apps.todo.utilities

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.todo.data.local.Database
import com.andb.apps.todo.filtering.filterProject
import com.andb.apps.todo.filtering.filterProjectTags
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.data.model.Project
import com.andb.apps.todo.data.model.Tag
import com.andb.apps.todo.data.model.Task
import java.util.*

object Current {

    /**Used to get sizes and lookups from database without needing lifecycle and/or Asynctask */
    private val bufferTags = ArrayList<Tag>()
    private val bufferTasks = ArrayList<Task>()
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

        projectsDao().all.observe(lifecycleOwner, Observer {projects: List<Project>->
            bufferProjects.clear()
            bufferProjects.addAll(projects)
            if(bufferViewingKey!=0 && !bufferProjects.map { it.id }.contains(bufferViewingKey)){
                ProjectList.setKey(bufferProjects[0].id)
                bufferViewingKey = bufferProjects[0].id
            }
        })
    }

    @JvmStatic
    fun initViewing(lifecycleOwner: LifecycleOwner){
        ProjectList.getKey().observe(lifecycleOwner, Observer { viewing->
            bufferViewingKey = viewing
            if(bufferProjects.isNotEmpty() && !bufferProjects.map { it.id }.contains(bufferViewingKey)){
                ProjectList.setKey(bufferProjects[0].id)
                bufferViewingKey = bufferProjects[0].id
            }
        })
    }

    @JvmStatic
    fun initProjectsSync(db: Database){
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

        //d("allProjects", "size = ${allProjects().size}, items = ${allProjects().joinToString { "name: ${it.name}, id: ${it.id}" }}, id to find = ${projectKey()}")
        return allProjects().first { it.id== projectKey() }
    }

    @JvmStatic
    fun keyList(): ArrayList<Int> {
        val keys = ArrayList<Int>()
        for (t in bufferTags) {
            keys.add(t.id)
        }
        for (t in bufferTasks) {
            keys.add(t.listKey)
        }
        for (p in allProjects()) {
            keys.add(p.id)
        }
        return keys
    }

    @JvmStatic
    fun database(): Database {
        return GetDatabase.database
    }

    @JvmStatic
    fun hasProjects(): Boolean {
        return !allProjects().isEmpty()
    }

    @JvmStatic
    fun tagListAll(): List<Tag> {
        return bufferTags.filterProjectTags()
    }

    @JvmStatic
    @JvmOverloads
    fun taskListAll(id: Int = projectKey()): List<Task> {
        return bufferTasks.filterProject(id)
    }
}
