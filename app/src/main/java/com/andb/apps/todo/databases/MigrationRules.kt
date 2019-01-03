package com.andb.apps.todo.databases

import android.database.Cursor
import android.util.Log
import com.andb.apps.todo.lists.TaskList
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.typeconverters.CheckedConverter
import com.andb.apps.todo.typeconverters.ItemsConverter
import com.andb.apps.todo.typeconverters.TagConverter
import dev.matrix.roomigrant.rules.OnMigrationEndRule
import dev.matrix.roomigrant.rules.OnMigrationStartRule
import org.joda.time.DateTime


/*class MigrationRules {

    var oldList: ArrayList<Tasks> = ArrayList()

    @OnMigrationStartRule(version1 = 1, version2 = 2)
    fun migrate_1_2_before(db: TasksDatabase, version1: Int, version2: Int) {
        val cursor = db.query("select * from TASKS", null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val listName = cursor.getString(cursor.getColumnIndex("list_name"))
                val listItems = ItemsConverter.itemsArrayList(cursor.getString(cursor.getColumnIndex("list_items")))
                val listItemsChecked = CheckedConverter.checkedArrayList(cursor.getString(cursor.getColumnIndex("list_items_checked")))
                val listTags = TagConverter.tagsArrayList(cursor.getString(cursor.getColumnIndex("list_tags")))
                val due = cursor.getLong(cursor.getColumnIndex("list_due"))
                val notified = cursor.getInt(cursor.getColumnIndex("list_notified")) > 0

                val tasks = Tasks(listName, listItems, listItemsChecked, listTags, DateTime(due), notified)
                Log.d("cursor", tasks.toString())
                oldList.add(tasks)
                cursor.moveToNext()
            }
        }
    }

    @OnMigrationEndRule(version1 = 1, version2 = 2)
    fun migrate_1_2_after(db: TasksDatabase, version1: Int, version2: Int) {

    }
}*/

