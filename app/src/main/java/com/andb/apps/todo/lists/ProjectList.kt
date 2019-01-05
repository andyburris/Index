package com.andb.apps.todo.lists

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.andb.apps.todo.MainActivity
import com.andb.apps.todo.databases.ProjectsDatabase
import com.andb.apps.todo.objects.Project

import java.util.ArrayList

object ProjectList {
    var projectList = ArrayList<Project>()
    var viewing: Int = 0

    fun appStart(context: Context, db: ProjectsDatabase){
        val viewing = PreferenceManager.getDefaultSharedPreferences(context).getInt("project_viewing", 0)
        projectList = ArrayList(db.projectsDao().all)
    }

    //TODO: sharedprefs for current viewing project
}
