package com.andb.apps.todo

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.TaskListItem
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_divider.view.*
import me.saket.inboxrecyclerview.InboxRecyclerView
import java.util.*

class TaskAdapter(var taskList: List<Tasks>,
                  var inboxBrowseArchive: Int) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    lateinit var expandedList: ArrayList<Boolean>
    var selected = -1

    private var viewType = 0

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.MyViewHolder {

        val itemView: View

        val context = parent.context

        if (viewType == TASK_VIEW_ITEM) {
            itemView = TaskListItem(context)
        } else if (viewType == ADD_TASK_PLACEHOLDER || viewType == EDIT_TASK_PLACEHOLDER) {
            itemView = AddTask(context)
            itemView.setLayoutParams(ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT))
        } else {
            itemView = LayoutInflater.from(context).inflate(
                    R.layout.inbox_divider, parent, false)
        }

        return TaskAdapter.MyViewHolder(itemView)

    }


    override fun onBindViewHolder(holder: TaskAdapter.MyViewHolder, position: Int) {
        val realPosition = holder.layoutPosition

        setUpByViewType(position, holder, realPosition)

        Log.d("onePosUpError", Integer.toString(realPosition))


    }

    private fun setUpByViewType(position: Int, holder: TaskAdapter.MyViewHolder, realPosition: Int) {


        if (viewType == TASK_VIEW_ITEM) {
            val taskListItem = holder.itemView as TaskListItem
            taskListItem.setup(taskList[position], realPosition, inboxBrowseArchive)
            if (position == selected) {
                taskListItem.setCyaneaBackground(Utilities.desaturate(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 0.8f), 0.7))
                //TODO: lighter color on dark theme
            }

            if (taskList[position].isEditing) {

            }

        } else if (viewType == ADD_TASK_PLACEHOLDER) {
            val addTask = holder.itemView as AddTask
            var browse = false
            if (inboxBrowseArchive == FROM_BROWSE) {
                browse = true
            }
            addTask.setup(browse, realPosition)
        } else if (viewType == EDIT_TASK_PLACEHOLDER) {
            val addTask = holder.itemView as AddTask
            var browse = false
            if (inboxBrowseArchive == FROM_BROWSE) {
                browse = true
            }
            addTask.setup(browse, realPosition, taskList[realPosition], true)
        } else { //divider logic
            Log.d("roomViewType", java.lang.Boolean.toString(holder.itemView is TaskListItem))

            if (realPosition == 0) { //resize if at top
                val scale = Resources.getSystem().displayMetrics.density
                val padding16Dp = (16 * scale).toInt()
                val padding20Dp = (20 * scale).toInt()
                holder.itemView.dividerName.setPadding(padding16Dp, padding20Dp, 0, padding16Dp)
            }

            when (viewType) {
                OVERDUE_DIVIDER -> holder.itemView.dividerName.setText(R.string.divider_overdue)
                TODAY_DIVIDER -> holder.itemView.dividerName.setText(R.string.divider_today)
                THIS_WEEK_DIVIDER -> holder.itemView.dividerName.setText(R.string.divider_week)
                THIS_MONTH_DIVIDER -> holder.itemView.dividerName.setText(R.string.divider_month)
                FUTURE_DIVIDER -> holder.itemView.dividerName.setText(R.string.divider_future)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        val task = taskList[position]
        val taskName = task.listName
        viewType = when (taskName) {
            "OVERDUE" -> OVERDUE_DIVIDER
            "TODAY" -> TODAY_DIVIDER
            "WEEK" -> THIS_WEEK_DIVIDER
            "MONTH" -> THIS_MONTH_DIVIDER
            "FUTURE" -> FUTURE_DIVIDER
            "ADD_TASK_PLACEHOLDER" -> ADD_TASK_PLACEHOLDER
            else -> {
                if (task.isEditing) {
                    EDIT_TASK_PLACEHOLDER
                } else {
                    TASK_VIEW_ITEM
                }
            }
        }




        return viewType
    }

    override fun getItemCount(): Int {
        return taskList.size
    }


    override fun getItemId(position: Int): Long {
        //return super.getItemId(position);
        return if (getItemViewType(position) == 0) {
            taskList[position].listKey.toLong()
        } else {
            (-1 * getItemViewType(position)).toLong()
        }
    }

    companion object {

        const val TASK_VIEW_ITEM = 0
        const val OVERDUE_DIVIDER = 1
        const val TODAY_DIVIDER = 2
        const val THIS_WEEK_DIVIDER = 3
        const val THIS_MONTH_DIVIDER = 4
        const val FUTURE_DIVIDER = 5
        const val ADD_TASK_PLACEHOLDER = 6
        const val EDIT_TASK_PLACEHOLDER = 7

        const val FROM_INBOX = 0
        const val FROM_BROWSE = 1
        const val FROM_ARCHIVE = 2
    }
}
