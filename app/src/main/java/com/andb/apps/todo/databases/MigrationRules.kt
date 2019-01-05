package com.andb.apps.todo.databases

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andb.apps.todo.eventbus.MigrateEvent
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.typeconverters.CheckedConverter
import com.andb.apps.todo.typeconverters.ItemsConverter
import com.andb.apps.todo.typeconverters.TagConverter
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

                val tasks = Tasks(listName, listItems, listItemsChecked, listTags, DateTime(due), notified, taskKey)
                Log.d("cursor", tasks.toString())
                MigrationHelper.oldList.add(tasks)
                cursor.moveToNext()
            }
        }
    }

    @OnMigrationEndRule(version1 = 1, version2 = 2)
    fun migrate_1_2_after(db: SupportSQLiteDatabase, version1: Int, version2: Int) {
        EventBus.getDefault().post(MigrateEvent(db, MigrationHelper.oldList))
    }

}





