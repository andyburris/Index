package com.andb.apps.todo.databases

import com.andb.apps.todo.objects.Tasks

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.matrix.roomigrant.GenerateRoomMigrations

@Database(entities = arrayOf(Tasks::class), version = 1, exportSchema = false)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao
}
