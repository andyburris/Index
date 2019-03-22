package com.andb.apps.todo.utilities

import android.os.AsyncTask
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.databases.projectsDao
import com.andb.apps.todo.databases.tagsDao
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks
import java.util.ArrayList
import java.util.Random

object ProjectsUtils {

    private val random = Random()

    @JvmOverloads
    @JvmStatic
    fun update(project: Project = Current.project(), projectsDatabase: ProjectsDatabase = Current.database()) {
        AsyncTask.execute {
            projectsDatabase.projectsDao().updateProject(project)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun update(tag: Tags, projectsDatabase: ProjectsDatabase = Current.database(), async: Boolean = true) {
        if(async) {
            AsyncTask.execute {
                projectsDatabase.tagsDao().updateTag(tag)
            }
        }else{
            projectsDatabase.tagsDao().updateTag(tag)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun update(task: Tasks, projectsDatabase: ProjectsDatabase = Current.database(), async: Boolean = true) {
        if(async) {
            AsyncTask.execute {
                projectsDatabase.tasksDao().updateTask(task)
            }
        }else{
            projectsDatabase.tasksDao().updateTask(task)
        }
    }

    @JvmStatic
    fun remove(tag: Tags){
        AsyncTask.execute {
            for(task in Current.taskListAll()){
                if(task.listTags.contains(tag.key)){
                    task.listTags.remove(tag.key)
                    update(task, async = false)
                }
            }
            for(childHolder in Current.tagListAll()){
                if(childHolder.children.contains(tag.key)){
                    childHolder.children.remove(tag.key)
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
    fun remove(task: Tasks){
        AsyncTask.execute {
            tasksDao().deleteTask(task)
        }
    }


    @JvmStatic
    fun projectFromKey(key: Int): Project? {
        for (p in Current.allProjects()) {
            if (p.key == key) {
                return p
            }
        }
        return null
    }

    @JvmStatic
    fun addProject(name: String, color: Int): Project {
        val project = Project(keyGenerator(), name,  color, Current.allProjects().size)
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
