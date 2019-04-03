package com.andb.apps.todo

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.objects.reminders.SimpleReminder
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.utilities.clearWith
import com.andb.apps.todo.views.FolderButtonCardLayout
import com.andb.apps.todo.views.InboxHeader
import com.andb.apps.todo.views.TaskListItem
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_divider.view.*
import kotlinx.android.synthetic.main.inbox_header.view.*
import me.saket.inboxrecyclerview.InboxRecyclerView
import org.joda.time.DateTime


class TaskAdapter(val activity: Activity) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    var taskList: MutableList<Tasks> = ArrayList()
    var expandedList = ArrayList<Boolean>()
    var selected = -1
    var headerPair: Pair<Boolean, Boolean> = Pair(first = false, second = false)

    lateinit var parentRecycler: RecyclerView

    private var viewType = 0

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        parentRecycler = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.MyViewHolder {

        val itemView: View

        val context = parent.context

        if (viewType == TASK_VIEW_ITEM) {
            itemView = TaskListItem(context)
        } else if (viewType == ADD_EDIT_TASK_PLACEHOLDER) {
            itemView = AddTask(context, activity as MainActivity)
        } else if (viewType == INBOX_HEADER) {
            itemView = InboxHeader(context)
        } else {
            itemView = LayoutInflater.from(context).inflate(
                R.layout.inbox_divider, parent, false
            )
        }

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return TaskAdapter.MyViewHolder(itemView)

    }


    override fun onBindViewHolder(holder: TaskAdapter.MyViewHolder, position: Int) {
        setUpByViewType(holder, holder.adapterPosition)
    }

    private fun setUpByViewType(holder: TaskAdapter.MyViewHolder, position: Int) {


        if (viewType == TASK_VIEW_ITEM) {
            (holder.itemView as TaskListItem).apply {
                setup(taskList[position], expandedList[position])
                clickListener = { long ->
                    if (!long && selected == -1) {
                        val bundle = Bundle()
                        bundle.putInt("key", task.listKey)

                        val fragmentActivity = parentRecycler.context as FragmentActivity
                        val ft = fragmentActivity.supportFragmentManager.beginTransaction()

                        val taskView = TaskView()
                        taskView.arguments = bundle


                        ft.add(R.id.expandable_page_inbox, taskView)
                        ft.commit()
                        (parentRecycler as InboxRecyclerView).expandItem(getItemId(position))
                    }

                }
                expandCollapseListener = { expanded ->
                    expandedList[position] = expanded
                }
                checkListener = { pos, isChecked ->
                    task.listItemsChecked[pos] = isChecked
                    Log.d("checkPersisted", (taskList[position].listItemsChecked[pos] == isChecked).toString())

                    AsyncTask.execute {
                        ProjectsUtils.update(task)
                    }
                }
                if (position == selected) {
                    setCyaneaBackground(Utilities.desaturate(Utilities.sidedLighterDarker(Cyanea.instance.backgroundColor, 0.8f), 0.7))
                }
            }


        } else if (viewType == ADD_EDIT_TASK_PLACEHOLDER) {
            val addTask = holder.itemView as AddTask
            addTask.setup(taskList[position])
        } else if (viewType == INBOX_HEADER) {
            (holder.itemView as InboxHeader).apply {
                setup(taskList.filter { !isDivider(it) }.size, headerPair)
                folderButton.addExpandCollapseListener { e ->
                    TransitionManager.beginDelayedTransition(parentRecycler, ChangeBounds().setDuration(FolderButtonCardLayout.ANIMATION_DURATION))
                    headerPair = Pair(e, headerPair.second)
                }
                folderButton.addEditListener { e ->
                    headerPair = Pair(headerPair.first, e)
                }
                folderButton.tagClickListener = {tag, longClick ->
                    if(!longClick) {
                        Filters.tagForward(tag)
                    }else{
                        Filters.tagReset(tag)
                    }
                }
            }



        } else { //divider logic
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
            "INBOX_HEADER" -> INBOX_HEADER
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
        return taskList[position].listKey.toLong()
    }


    private fun dispatchUpdates(newItems: List<Tasks>, diffResult: DiffUtil.DiffResult) {
        Log.d("dipatchUpdates", "newItems size: ${newItems.size}")
        taskList.clearWith(newItems)
        expandedList.clearWith(newItems.map { SettingsActivity.subtaskDefaultShow })//TODO: preserve expanded across diffutil
        diffResult.dispatchUpdatesTo(this)
    }

    fun update(newList: List<Tasks>) {
        val oldItems: List<Tasks> = ArrayList(this.taskList)

        val handler = Handler(Looper.getMainLooper())
        Thread(Runnable {
            val diffResult = DiffUtil.calculateDiff(TaskAdapterDiffCallback(oldItems, newList))
            handler.post {
                dispatchUpdates(newList, diffResult)
            }
        }).start()
    }

    internal class TaskAdapterDiffCallback(private val oldTasks: List<Tasks>, private val newTasks: List<Tasks>) :
        DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldTasks[oldItemPosition]
            val newTask = newTasks[newItemPosition]

            return oldTask.listKey == newTask.listKey
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldTask = oldTasks[oldItemPosition]
            val newTask = newTasks[newItemPosition]

            return oldTask == newTask && oldTask.isEditing == newTask.isEditing && oldTask.listName != "INBOX_HEADER"
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
        const val INBOX_HEADER = 7


        @JvmStatic
        fun newDivider(name: String, dateTime: DateTime): Tasks {
            return Tasks(name, ArrayList(), ArrayList(), ArrayList(), arrayListOf(SimpleReminder(dateTime)), ArrayList())
        }

        @JvmStatic
        fun newHeader(): Tasks {
            val dateTime = DateTime(DateTime(1969, 1, 1, 0, 0))
            return Tasks("INBOX_HEADER", ArrayList(), ArrayList(), ArrayList(), arrayListOf(SimpleReminder(dateTime)), ArrayList())
        }

        @JvmStatic
        fun newAddTask(): Tasks {
            return Tasks("", ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList()).also { it.isEditing = true }
        }
    }
}
