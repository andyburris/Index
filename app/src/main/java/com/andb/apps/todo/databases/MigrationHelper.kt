package com.andb.apps.todo.databases

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.typeconverters.TagConverter
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

object MigrationHelper{

    val oldList: ArrayList<Tasks> = ArrayList()


    @JvmStatic
        fun migrate_1_2_with_context(ctxt: Context, db: ProjectsDatabase, taskList: ArrayList<Tasks>) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctxt)

            val gson = Gson()
            val taskjson = prefs.getString("archiveTaskList", null)
            val tagjson = prefs.getString("tagList", null)
            val linkjson = prefs.getString("linkList", null)
            val taskType = object : TypeToken<ArrayList<Tasks>>() {

            }.type
            val tagType = object : TypeToken<ArrayList<Tags>>() {

            }.type
            val linkType = object : TypeToken<ArrayList<TagLinks>>() {

            }.type

            val archiveList = gson.fromJson<ArrayList<Tasks>>(taskjson, taskType)
            val tagList = gson.fromJson<ArrayList<Tags>>(tagjson, tagType)
            val tagLinks = gson.fromJson<ArrayList<TagLinks>>(linkjson, linkType)

            for ((index: Int, t: Tags) in tagList.withIndex()){//convert taglinks to inside of tag
                val tagLink: TagLinks? = tagLinks.find { it.tagParent()==index }
                t.children = tagLink?.allTagLinks
            }

            val project = Project(Random().nextInt(), "Tasks", taskList, archiveList, tagList)

            AsyncTask.execute {
                db.projectsDao().insertOnlySingleProject(project)
                ProjectList.projectList = ArrayList(db.projectsDao().all)
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