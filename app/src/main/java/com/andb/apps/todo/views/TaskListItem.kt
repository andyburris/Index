package com.andb.apps.todo.views

import android.content.Context
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.*
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class TaskListItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    lateinit var task: Tasks
    private val STATE_ZERO = intArrayOf(R.attr.state_on, -R.attr.state_off)
    private val STATE_ONE = intArrayOf(-R.attr.state_on, R.attr.state_off)
    private var sublistToggleState = SettingsActivity.subtaskDefaultShow

    init {
        inflate(context, R.layout.inbox_list_item, this)
    }

    fun setup(tasks: Tasks, pos: Int, inboxBrowseArchive: Int) {
        task = tasks
        setTasks(pos, inboxBrowseArchive)
        topLayout.setTags(tasks)
        topLayout.setTitle(task.listName)
        topLayout.setOverflow(this)
        moreTags.visibility = if (topLayout.chipsVisible > task.listTagsSize) View.GONE else View.VISIBLE
        inboxCard.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("pos", pos)
            bundle.putInt("inboxBrowseArchive", inboxBrowseArchive)

            val activity = context as FragmentActivity
            val ft = activity.supportFragmentManager.beginTransaction()

            val taskView = TaskView()
            taskView.arguments = bundle


            when (inboxBrowseArchive) {
                TaskAdapter.FROM_BROWSE -> {
                    if(BrowseFragment.mAdapter.selected==-1) {
                        ft.add(R.id.expandable_page_browse, taskView)
                        ft.commit()
                        BrowseFragment.mRecyclerView.expandItem(BrowseFragment.mAdapter.getItemId(pos))
                    }
                }
                TaskAdapter.FROM_ARCHIVE -> {
                }
                else -> { //inbox
                    if(InboxFragment.mAdapter.selected==-1) {
                        ft.add(R.id.expandable_page_inbox, taskView)
                        ft.commit()
                        InboxFragment.mRecyclerView.expandItem(InboxFragment.mAdapter.getItemId(pos))
                    }
                }
            }
        }
        setCyaneaBackground(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
    }


    private fun setTasks(pos: Int, inboxBrowseArchive: Int) {

        val checkBoxes = ArrayList(Arrays.asList<CheckBox>(item1, item2, item3))

        if (task.isListItems) {
            Log.d("items", task.listName + ", multipleItems: " + task.listItemsSize)

            for (i in 0..2) {
                val scale: Float = this.resources.displayMetrics.density
                val checkBox: CheckBox = checkBoxes[i]
                checkBox.setPadding(Math.round(4f * scale + 0.5f), checkBox.paddingTop, checkBox.paddingRight, checkBox.paddingBottom)

                if (i < task.listItemsSize) {

                    checkBox.text = task.listItems[i]
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        task.editListItemsChecked(isChecked, i)
                        checkBox.paintFlags = if(!isChecked)checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else  checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        AsyncTask.execute {
                            ProjectsUtils.update()
                        }

                    }
                    checkBox.isChecked = task.getListItemsChecked(i)
                    checkBox.paintFlags = if(!checkBox.isChecked)checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else  checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    checkBox.visibility = View.VISIBLE

                } else {
                    checkBox.visibility = View.GONE
                }
            }
            if (task.listItemsSize > 3) {
                moreTasks.visibility = View.VISIBLE
            } else {
                moreTasks.visibility = View.GONE
            }

            var expandedList: ArrayList<Boolean>
            var taskList: ArrayList<Tasks>
            val adapter: TaskAdapter
            val rv: RecyclerView
            when (inboxBrowseArchive) {
                TaskAdapter.FROM_ARCHIVE -> rv = Archive.mRecyclerView
                TaskAdapter.FROM_BROWSE -> rv = BrowseFragment.mRecyclerView
                else -> rv = InboxFragment.mRecyclerView
            }

            adapter = rv.adapter as TaskAdapter
            taskList = ArrayList(adapter.taskList)
            adapter.expandedList = ArrayList<Boolean>()
            expandedList = adapter.expandedList

            for (tasks in taskList) { /*init here since tasklist is null in constructor*/
                var selected = false
                if (SettingsActivity.subtaskDefaultShow) {
                    selected = tasks.isListItems
                }
                expandedList.add(selected)
            }

            setupCollapse(expandedList, pos, rv)


        } else { //no checkboxes
            item1.visibility = View.GONE
            item2.visibility = View.GONE
            item3.visibility = View.GONE
            moreTasks.visibility = View.GONE
            sublistIcon.visibility = View.GONE
        }


    }

    fun setupCollapse(expandedList: ArrayList<Boolean>, pos: Int, rv: RecyclerView) {
        if (expandedList[pos]) {
            expandSublist()
        } else {
            collapseSublist()
        }
        sublistIcon.setOnClickListener {

            expandedList[pos] = !expandedList[pos]
            TransitionManager.beginDelayedTransition(rv, ChangeBounds())
            if (expandedList[pos]) {
                expandSublist()
                expandedList[pos] = true
            } else {
                collapseSublist()
                expandedList[pos] = false
            }

        }
    }

    fun expandSublist() {
        sublistToggleState = true

        item1.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        item2.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        item3.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        listitempadding.visibility = View.VISIBLE
        sublistIcon.setImageState(STATE_ZERO, true)

    }

    fun collapseSublist() {
        sublistToggleState = false

        item1.layoutParams.height = 1
        item2.layoutParams.height = 1
        item3.layoutParams.height = 1
        listitempadding.visibility = View.GONE
        sublistIcon.setImageState(STATE_ONE, true)
    }


    fun setCyaneaBackground(color: Int) {
        inboxCard.setBackgroundColor(color)
    }


}