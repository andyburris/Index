package com.andb.apps.todo.lists

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import androidx.room.Query
import com.andb.apps.todo.utilities.OnceHolder
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.ProjectsUtils
import java.util.ArrayList
import kotlin.Comparator

object ProjectList {
    var projectList = ArrayList<Project>()
    var viewing: Int = 0

    fun appStart(context: Context, db: ProjectsDatabase) {
        var viewing = PreferenceManager.getDefaultSharedPreferences(context).getInt("project_viewing", 0)

        db.tasksDao().apply {//cleanse blank names from add & exit app
            val list = findTasksByName("")
            for(t in list){
                deleteTask(t)
            }

        }

        ProjectsUtils.setupProjectList(db)



        OnceHolder.checkAppSetup(context)

        if(viewing>= projectList.size){
            viewing = 0
        }
        ProjectList.viewing = viewing

        if (projectList.any { it.index == -1 }) {
            for ((i, p) in projectList.withIndex()) {
                p.index = i
                AsyncTask.execute{
                    db.projectsDao().updateProject(p)
                }
            }

        }
        projectList.sortWith(Comparator { o1, o2 -> o1.index.compareTo(o2.index) })
    }
}
