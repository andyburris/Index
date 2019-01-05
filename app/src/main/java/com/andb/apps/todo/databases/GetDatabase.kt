package com.andb.apps.todo.databases

import android.content.Context
import androidx.room.Room

object GetDatabase{
    const val DATABASE_NAME = "Tasks_db"

    @JvmStatic
    fun getDatabase(ctxt: Context): ProjectsDatabase{
        return Room.databaseBuilder(ctxt,
                ProjectsDatabase::class.java, DATABASE_NAME)
                .addMigrations(*ProjectsDatabase_Migrations.build())
                .build()
    }
}