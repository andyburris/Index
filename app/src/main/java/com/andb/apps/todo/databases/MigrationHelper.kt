package com.andb.apps.todo.databases

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.typeconverters.TagConverter
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList

object MigrationHelper {

    val oldList_1_2: ArrayList<Tasks> = ArrayList()
    val oldList_4_5 = ArrayList<Triple<Project, Pair<ArrayList<Tasks>, ArrayList<Tasks>>, ArrayList<Tags>>>()
    var oldList_5_6 = ArrayList<Tasks>()


    @JvmStatic
    fun migrate_1_2_with_context(ctxt: Context, db: ProjectsDatabase) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctxt)

        val gson = Gson()
        val taskjson = prefs.getString("archiveTaskList", null)
        val tagjson = prefs.getString("tagList", null)
        val linkjson = prefs.getString("linkList", null)
        val taskType = object : TypeToken<ArrayList<Tasks>>(){}.type
        val tagType = object : TypeToken<ArrayList<Tags>>(){}.type
        val linkType = object : TypeToken<ArrayList<TagLinks>>(){}.type

        val archiveList = gson.fromJson<ArrayList<Tasks>>(taskjson, taskType)
        val tagList = gson.fromJson<ArrayList<Tags>>(tagjson, tagType)
        val tagLinks = gson.fromJson<ArrayList<TagLinks>>(linkjson, linkType)

        for ((index: Int, t: Tags) in tagList.withIndex()) {//convert taglinks to inside of tag
            val tagLink: TagLinks? = tagLinks.find { it.tagParent() == index }
            t.children = tagLink?.allTagLinks
        }

        val project = Project(Random().nextInt(), "Tasks",  0x000000, 0)

        AsyncTask.execute {
            db.projectsDao().insertOnlySingleProject(project)
            db.tasksDao().insertMultipleTasks(oldList_1_2)
            db.tasksDao().insertMultipleTasks(archiveList)
            db.tagsDao().insertMultipleTags(tagList)
            //ProjectList.projectList = ArrayList(db.projectsDao().all) TODO: Migrate to v5

            EventBus.getDefault().post(UpdateEvent(true))
        }
    }

    @JvmStatic
    fun migrate_4_5_with_context(ctxt: Context, db: ProjectsDatabase){
        val newTaskList = ArrayList<Tasks>()
        val newTagList = ArrayList<Tags>()
        val projectList = ArrayList<Project>()

        for(trip in oldList_4_5){
            trip.apply {
                val proj = first
                val taskList = second.first
                val archiveList = second.second
                val tagList = third
                projectList.add(proj)

                for((i, t) in tagList.withIndex()){
                    t.projectId = proj.key
                    t.index = i
                    newTagList.add(t)
                }
                for(t in taskList){
                    t.isArchived = false
                    t.projectId = proj.key
                    newTaskList.add(t)
                }
                for(t in archiveList){
                    t.isArchived = true
                    t.projectId = proj.key
                    newTaskList.add(t)
                }
            }
        }

        AsyncTask.execute {
            db.projectsDao().insertMultipleProjects(projectList)
            db.tasksDao().insertMultipleTasks(newTaskList)
            db.tagsDao().insertMultipleTags(newTagList)
        }

    }

    @JvmStatic
    fun migrate_5_6_with_context(ctxt: Context, db: ProjectsDatabase){
        AsyncTask.execute {
            db.tasksDao().insertMultipleTasks(oldList_5_6)
        }

    }

}

class TagLinks(
        @field:SerializedName("tagPosition")
        @field:Expose
        private val tagPosition: Int, links: java.util.ArrayList<Int>) {

    @SerializedName("links")
    @Expose
    @TypeConverters(TagConverter::class)
    var allTagLinks = java.util.ArrayList<Int>()

    val isTagLinked: Boolean
        get() = !allTagLinks.isEmpty()

    init {
        this.allTagLinks = links
    }

    fun tagParent(): Int {
        return tagPosition
    }

    fun addLink(pos: Int) {
        allTagLinks.add(pos)
    }

    fun getTagLink(linkPos: Int): Int {
        return allTagLinks[linkPos]
    }

    fun removeTagLink(pos: Int) {
        allTagLinks.remove(pos)
    }

    fun getLinkPosition(pos: Int): Int {
        return allTagLinks.indexOf(pos)
    }

    operator fun contains(pos: Int): Boolean {
        return allTagLinks.contains(pos)
    }
}

object TagLinkConverter {
    private val gson = Gson()

    @TypeConverter
    fun tagLinkArrayList(data: String?): ArrayList<TagLinks> {
        if (data == null) {
            return ArrayList()
        }

        val listType = object : TypeToken<ArrayList<TagLinks>>() {

        }.type

        return gson.fromJson(data, listType)

    }

    @TypeConverter
    fun tagLinkListToString(tagList: ArrayList<TagLinks>): String {
        return gson.toJson(tagList)
    }
}