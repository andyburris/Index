package com.andb.apps.todo.databases

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andb.apps.todo.eventbus.MigrateEvent
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.objects.reminders.SimpleReminder
import com.andb.apps.todo.typeconverters.*
import com.andb.apps.todo.utilities.ProjectsUtils
import dev.matrix.roomigrant.rules.OnMigrationEndRule
import dev.matrix.roomigrant.rules.OnMigrationStartRule
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime

@Suppress("FunctionName")
class MigrationRules {

    @OnMigrationStartRule(version1 = 1, version2 = 2)
    fun migrate_1_2_before(db: SupportSQLiteDatabase, version1: Int, version2: Int) {
        val cursor = db.query("select * from TASKS", null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val listName = cursor.getString(cursor.getColumnIndex("list_name"))
                val listItems = ItemsConverter.itemsArrayList(cursor.getString(cursor.getColumnIndex("list_items")))
                val listItemsChecked = CheckedConverter.checkedArrayList(cursor.getString(cursor.getColumnIndex("list_items_checked")))
                val listTags = TagConverter.tagsArrayList(cursor.getString(cursor.getColumnIndex("list_tags")))
                val due = cursor.getLong(cursor.getColumnIndex("list_due"))
                val notified = cursor.getInt(cursor.getColumnIndex("list_notified")) > 0
                val taskKey = cursor.getInt(cursor.getColumnIndex("listKey"))

                val tasks = Tasks(listName, listItems, listItemsChecked, listTags, arrayListOf(SimpleReminder(DateTime(due), notified)), ArrayList(), taskKey, false)

                Log.d("cursor", tasks.toString())
                MigrationHelper.oldList_1_2.add(tasks)
                cursor.moveToNext()
            }
        }
    }

    @OnMigrationEndRule(version1 = 1, version2 = 2)
    fun migrate_1_2_after(db: SupportSQLiteDatabase, version1: Int, version2: Int) {
        EventBus.getDefault().post(MigrateEvent(1, 2))
    }

    @OnMigrationStartRule(version1 = 4, version2 = 5)
    fun migrate_4_5_before(db: SupportSQLiteDatabase, version1: Int, version2: Int){
        val cursor = db.query("select * from PROJECT", null)
        cursor.apply {
            if (moveToFirst()) {
                while (!isAfterLast) {
                    val key = getInt(getColumnIndex("key"))
                    val projectName = getString(getColumnIndex("project_name"))
                    val color = getInt(getColumnIndex("project_color"))
                    val index = getInt(getColumnIndex("project_index"))
                    val taskList = TaskListConverter.tasksArrayList(getString(getColumnIndex("task_list")))
                    val archiveList = TaskListConverter.tasksArrayList(getString(getColumnIndex("archive_list")))
                    val tagList = TagListConverter.tagListArrayList(getString(getColumnIndex("tag_list")))



                    val project = Project(key, projectName, taskList, archiveList, tagList, color, index)
                    Log.d("cursor", project.toString())
                    MigrationHelper.oldList_4_5.add(project)
                    moveToNext()
                }
            }
        }

    }

    @OnMigrationEndRule(version1 = 4, version2 = 5)
    fun migrate_4_5_after(db: SupportSQLiteDatabase, version1: Int, version2: Int){
        EventBus.getDefault().post(MigrateEvent(4, 5))
    }

    @OnMigrationStartRule(version1 = 5, version2 = 6)
    fun migrate_5_6_before(db: SupportSQLiteDatabase, version1: Int, version2: Int){
        val cursor = db.query("select * from TASKS", null)
        cursor.apply {
            if (moveToFirst()) {
                while (!isAfterLast) {
                    val listName = cursor.getString(cursor.getColumnIndex("list_name"))
                    val listItems = ItemsConverter.itemsArrayList(cursor.getString(cursor.getColumnIndex("list_items")))
                    val listItemsChecked = CheckedConverter.checkedArrayList(cursor.getString(cursor.getColumnIndex("list_items_checked")))
                    val listTags = TagConverter.tagsArrayList(cursor.getString(cursor.getColumnIndex("list_tags")))
                    val due = cursor.getLong(cursor.getColumnIndex("list_due"))
                    val notified = cursor.getInt(cursor.getColumnIndex("list_notified")) > 0
                    val taskKey = cursor.getInt(cursor.getColumnIndex("listKey"))
                    val projectKey = cursor.getInt(cursor.getColumnIndex("project_id"))
                    val archived =  cursor.getInt(cursor.getColumnIndex("archived")) > 0



                    val task = Tasks(listName, listItems, listItemsChecked, listTags, arrayListOf(SimpleReminder(DateTime(due), notified)), ArrayList(), taskKey, projectKey, archived)
                    Log.d("cursor", task.toString())
                    MigrationHelper.oldList_5_6.add(task)
                    moveToNext()
                }
            }
        }

    }

    @OnMigrationEndRule(version1 = 5, version2 = 6)
    fun migrate_5_6_after(db: SupportSQLiteDatabase, version1: Int, version2: Int){
        EventBus.getDefault().post(MigrateEvent(5, 6))
    }


}





