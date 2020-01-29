package com.andb.apps.todo.utilities

import android.os.AsyncTask
import com.andb.apps.todo.data.local.Database
import com.andb.apps.todo.databases.projectsDao
import com.andb.apps.todo.databases.tagsDao
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.data.model.Project
import com.andb.apps.todo.data.model.Tag
import com.andb.apps.todo.data.model.Task
import java.util.*

object ProjectsUtils {

    private val random = Random()

    @JvmOverloads
    @JvmStatic
    fun update(project: Project = Current.project(), database: Database = Current.database()) {
        AsyncTask.execute {
            database.projectsDao().updateProject(project)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun update(tag: Tag, database: Database = Current.database(), async: Boolean = true) {
        if(async) {
            AsyncTask.execute {
                database.tagsDao().updateTag(tag)
            }
        }else{
            database.tagsDao().updateTag(tag)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun update(task: Task, database: Database = Current.database(), async: Boolean = true) {
        if(async) {
            AsyncTask.execute {
                database.tasksDao().updateTask(task)
            }
        }else{
            database.tasksDao().updateTask(task)
        }
    }

    @JvmStatic
    fun remove(tag: Tag){
        AsyncTask.execute {
            for(task in Current.taskListAll()){
                if(task.listTags.contains(tag.id)){
                    task.listTags.remove(tag.id)
                    update(task, async = false)
                }
            }
            for(childHolder in Current.tagListAll()){
                if(childHolder.children.contains(tag.id)){
                    childHolder.children.remove(tag.id)
                    update(childHolder, async = false)
                }
            }
            for(t in Current.tagListAll()){
                if (t.index>tag.index){
                    t.index--
                }
            }

            tagsDao().deleteTag(tag)
        }
    }

    @JvmStatic
    fun remove(task: Task){
        AsyncTask.execute {
            tasksDao().deleteTask(task)
        }
    }


    @JvmStatic
    fun projectFromKey(key: Int): Project? {
        for (p in Current.allProjects()) {
            if (p.id == key) {
                return p
            }
        }
        return null
    }

    @JvmStatic
    fun addProject(name: String, color: Int): Project {
        val project = Project(keyGenerator(), name, color, Current.allProjects().size)
        AsyncTask.execute {
            projectsDao().insertOnlySingleProject(project)
        }

        return project
    }

    @JvmStatic
    fun keyGenerator(): Int {
        var key = random.nextInt()

        while (Current.keyList().contains(key) || key in listOf(0, -1)) {
            key = random.nextInt()
        }
        return key
    }


}
