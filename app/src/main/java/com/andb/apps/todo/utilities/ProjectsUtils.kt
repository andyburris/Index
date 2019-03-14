package com.andb.apps.todo.utilities

import android.os.AsyncTask
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.BaseProject
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks
import java.util.ArrayList
import java.util.Random
import kotlin.Comparator

object ProjectsUtils {

    private val random = Random()

    @JvmOverloads
    @JvmStatic
    fun update(project: Project = Current.project(), projectsDatabase: ProjectsDatabase = Current.database()) {
        AsyncTask.execute {
            projectsDatabase.projectsDao().updateProject(project as BaseProject)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun update(tag: Tags, projectsDatabase: ProjectsDatabase = Current.database()) {
        AsyncTask.execute {
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
    fun setupProjectList(db: ProjectsDatabase) {
        ProjectList.projectList = ArrayList()
        for (bp in db.projectsDao().all) {
            bp.apply {
                ProjectList.projectList.add(Project(key, name, ArrayList(), ArrayList(), ArrayList(), color, index))
            }
        }

        for (p in ProjectList.projectList) {
            p.apply {
                val tasksPair = db.tasksDao().getAllFromProject(key).partition { tasks -> !tasks.isArchived }
                taskList = ArrayList(tasksPair.first)
                archiveList = ArrayList(tasksPair.second)
                tagList = ArrayList(db.tagsDao().getAllFromProject(key))
                tagList.sortWith(Comparator { o1, o2 -> o1.index.compareTo(o2.index) })
            }
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
        val project = Project(keyGenerator(), name, ArrayList(), ArrayList(), ArrayList(), color, Current.allProjects().size)
        ProjectList.projectList.add(project)
        ProjectsUtils.update(project)
        return project
    }

    @JvmStatic
    fun keyGenerator(): Int {
        var key = random.nextInt()
        val keys = ArrayList<Int>()
        for (project in Current.allProjects()) {
            keys.add(project.key)
            keys.addAll(project.keyList)
        }

        while (keys.contains(key) || key == 0) {
            key = random.nextInt()
        }
        return key
    }
}
