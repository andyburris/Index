package com.andb.apps.todo.filtering

import android.util.Log

import com.andb.apps.todo.BrowseFragment
import com.andb.apps.todo.InboxFragment
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Current

import java.util.ArrayList
import java.util.Collections

object FilteredLists {
    var inboxTaskList = ArrayList<Tasks>()
    var browseTaskList = ArrayList<Tasks>()
    var filteredTagLinks = ArrayList<Int>()

    fun createFilteredTaskList(tagsToFilter: ArrayList<Int>, viewing: Boolean) {


        Log.d("noFilters", Integer.toString(tagsToFilter.size))

        /*need to maintain object reference for recyclerview*/
        inboxTaskList.clear()
        browseTaskList.clear()
        filteredTagLinks.clear()

        val parentIndex = Filters.getMostRecent()
        if (parentIndex > -1) {
            val tagParent = Current.tagList()[parentIndex]

            if (tagParent.children == null) {
                tagParent.children = ArrayList()
            }
            filteredTagLinks.addAll(filterChildren(Current.tagList(), tagParent, Filters.getCurrentFilter()))
        } else {
            filteredTagLinks.addAll(filterChildren(Current.tagList(), null, Filters.getCurrentFilter()))
        }


        inboxTaskList.addAll(filterInbox(Current.taskList(), Filters.getCurrentFilter()))
        browseTaskList.addAll(filterBrowse(inboxTaskList, Filters.getCurrentFilter(), filteredTagLinks, Current.tagList(), SettingsActivity.subFilter))


        BrowseFragment.refreshWithAnim()

        BrowseFragment.tAdapter.notifyDataSetChanged()


        //if (viewing) {
        InboxFragment.setFilterMode()
        InboxFragment.refreshWithAnim()
        //InboxFragment.Companion.getMAdapter().notifyDataSetChanged();

        Log.d("inboxTaskList", "inboxTaskList: " + inboxTaskList.size + ", InboxFragment.mAdapter.taskList: " + InboxFragment.mAdapter.taskList.size)
        for (t in InboxFragment.mAdapter.taskList) {
            Log.d("inboxTaskList", t.toString())
        }
        //}


        InboxFragment.setTaskCountText(inboxTaskList.size)


    }

    fun filterChildren(tags: ArrayList<Tags>, parent: Tags?, previousFilters: ArrayList<Int>): ArrayList<Int> {

        val filteredList = ArrayList<Int>()

        return filteredList.apply {
            if (parent != null) {
                filteredList.addAll(parent.children.filter { !previousFilters.contains(it) && !previousFilters.isEmpty() })
            } else {
                filteredList.addAll(tags.filter { tag ->  !tag.isSubFolder }.map { tag ->  tag.index })
            }
        }
    }

    fun filterInbox(tasks: ArrayList<Tasks>, parents: ArrayList<Int>): ArrayList<Tasks> {

        val filteredList = ArrayList<Tasks>()
        filteredList.addAll(tasks.filter { task ->  task.listTags.containsAll(parents) })
        return filteredList
    }

    fun filterBrowse(tasks: ArrayList<Tasks>, parents: ArrayList<Int>, children: ArrayList<Int>, tags: ArrayList<Tags>, subfolderAsFilter: Boolean): ArrayList<Tasks> {

        val filteredList = ArrayList<Tasks>()

        val toRemove = ArrayList<Int>()
        toRemove.addAll(children.filter { subfolderAsFilter && tags[it].isSubFolder})
        children.removeAll(toRemove)


        filteredList.addAll(tasks.filter { task -> task.listTags.containsAll(parents) && !task.listTags.any { children.contains(it) } })
        return filteredList

            /*} else {
                if (!t.isListTags) {
                    filteredList.add(t)
                }
            }
        }*/


    }
}
