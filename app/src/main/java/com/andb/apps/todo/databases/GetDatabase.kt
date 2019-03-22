package com.andb.apps.todo.databases

import android.content.Context
import androidx.room.Room
import com.andb.apps.todo.databases.GetDatabase.projectsDatabase

object GetDatabase{
    const val DATABASE_NAME = "Tasks_db"

    @JvmStatic
    fun getDatabase(ctxt: Context): ProjectsDatabase{
        return Room.databaseBuilder(ctxt,
                ProjectsDatabase::class.java, DATABASE_NAME)
                .addMigrations(*ProjectsDatabase_Migrations.build(), MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
    }

    @JvmStatic
    lateinit var projectsDatabase: ProjectsDatabase

    @JvmStatic
    fun isInit(): Boolean{
        return ::projectsDatabase.isInitialized
    }
}

fun tasksDao() = projectsDatabase.tasksDao()
fun tagsDao() = projectsDatabase.tagsDao()
fun projectsDao() = projectsDatabase.projectsDao()