package com.andb.apps.todo

import android.content.ClipData.Item
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.objects.reminders.SimpleReminder
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.TaskListItem
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_divider.view.*
import org.joda.time.DateTime


class TaskAdapter(var inboxBrowseArchive: Int) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    var taskList: MutableList<Tasks> = ArrayList()
    lateinit var expandedList: ArrayList<Boolean>
    var selected = -1

    private var viewType = 0

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.MyViewHolder {

        val itemView: View

        val context = parent.context

        if (viewType == TASK_VIEW_ITEM) {
            itemView = TaskListItem(context)
            itemView.setLayoutParams(ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT))
        } else if (viewType == ADD_EDIT_TASK_PLACEHOLDER) {
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

        } else if (viewType == ADD_EDIT_TASK_PLACEHOLDER) {
            val addTask = holder.itemView as AddTask
            var browse = false
            if (inboxBrowseArchive == FROM_BROWSE) {
                browse = true
            }
            addTask.setup(browse, taskList[realPosition])
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
            else -> {
                if (task.isEditing) {
                    ADD_EDIT_TASK_PLACEHOLDER
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




    private fun dispatchUpdates(newItems: List<Tasks>, diffResult: DiffUtil.DiffResult) {
        Log.d("dipatchUpdates", "newItems size: ${newItems.size}")
        taskList.apply {
            clear()
            addAll(newItems)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    fun update(newList: ArrayList<Tasks>) {
        val oldItems: List<Tasks> = ArrayList(this.taskList)

        val handler = Handler()
        Thread(Runnable {
            val diffResult = DiffUtil.calculateDiff(TaskAdapterDiffCallback(oldItems, newList))
            handler.post {
                dispatchUpdates(newList, diffResult)
            }
        }).start()
    }

    internal class TaskAdapterDiffCallback(private val oldTasks: List<Tasks>, private val newTasks: List<Tasks>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTasks[oldItemPosition].listKey == newTasks[newItemPosition].listKey
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTasks[oldItemPosition] == newTasks[newItemPosition] && oldTasks[oldItemPosition].isEditing == newTasks[newItemPosition].isEditing
        }

        override fun getNewListSize(): Int {
            return newTasks.size
        }

        override fun getOldListSize(): Int {
            return oldTasks.size
        }

    }

    companion object {

        const val TASK_VIEW_ITEM = 0
        const val OVERDUE_DIVIDER = 1
        const val TODAY_DIVIDER = 2
        const val THIS_WEEK_DIVIDER = 3
        const val THIS_MONTH_DIVIDER = 4
        const val FUTURE_DIVIDER = 5
        const val ADD_EDIT_TASK_PLACEHOLDER = 6

        const val FROM_INBOX = 0
        const val FROM_BROWSE = 1
        const val FROM_ARCHIVE = 2

        @JvmStatic
        fun newDivider(name: String, dateTime: DateTime): Tasks {
            return Tasks(name, ArrayList(), ArrayList(), ArrayList(), arrayListOf(SimpleReminder(dateTime)), ArrayList())
        }

        @JvmStatic
        fun newAddTask(): Tasks {
            return Tasks("", ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList()).also { it.isEditing = true }
        }
    }
}
