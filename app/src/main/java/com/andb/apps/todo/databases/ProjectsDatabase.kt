package com.andb.apps.todo.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andb.apps.todo.objects.Project
import dev.matrix.roomigrant.GenerateRoomMigrations
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.andb.apps.todo.objects.BaseProject
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks

@Database(entities = arrayOf(BaseProject::class, Tasks::class, Tags::class), version = 6)
@GenerateRoomMigrations(MigrationRules::class)
abstract class ProjectsDatabase : RoomDatabase() {
    abstract fun projectsDao(): ProjectsDao
    abstract fun tasksDao(): TasksDao
    abstract fun tagsDao(): TagsDao
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PROJECT " + " ADD COLUMN project_color INTEGER DEFAULT 0x000000 NOT NULL")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PROJECT " + " ADD COLUMN project_index INTEGER DEFAULT -1 NOT NULL")
    }
}

/*
val MIGRATION_5_6: Migration = object : Migration(5, 6){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE UNIQUE INDEX index_BaseProject_key ON BaseProject(`key`)")
    }
}
*/
