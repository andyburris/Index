package com.andb.apps.todo.lists

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.objects.Project
import java.util.ArrayList
import kotlin.Comparator

object ProjectList {
    var projectList = ArrayList<Project>()
    var viewing: Int = 0

    fun appStart(context: Context, db: ProjectsDatabase) {
        var viewing = PreferenceManager.getDefaultSharedPreferences(context).getInt("project_viewing", 0)
        projectList = ArrayList(db.projectsDao().all)
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
        //TODO: sharedprefs for current viewing project
    }
}
