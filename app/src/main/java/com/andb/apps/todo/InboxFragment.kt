package com.andb.apps.todo

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.MaterialCab
import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.utilities.Values
import com.andb.apps.todo.utilities.Values.*
import com.andb.apps.todo.utilities.Vibes
import com.google.android.gms.location.GeofencingClient
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.CyaneaFragment
import kotlinx.android.synthetic.main.fragment_inbox.view.*
import me.saket.inboxrecyclerview.InboxRecyclerView
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.lang.Exception
import java.util.*

class InboxFragment : CyaneaFragment() {

    internal var isSwiping = false


    private var filterMode = 0 //0=date, 1=alphabetical, more to come
    var dividersForTaskCount = 0

    var addingTask = false

    lateinit var mRecyclerView: InboxRecyclerView
    lateinit var mAdapter: TaskAdapter


    // Extend the Callback class
    private val _ithCallback = object : ItemTouchHelper.Callback() {

        var vibedOnSwipe: Boolean = false

        //and in your implementation of
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d("swipeAction", "swiped")

            if (viewHolder.itemViewType != TaskAdapter.ADD_EDIT_TASK_PLACEHOLDER) {
                if (filterMode == TIME_SORT) {
                    removeWithDivider(viewHolder.adapterPosition)
                } else {
                    removeTask(viewHolder.adapterPosition)
                }
            } else {
                if (addingTask) {
                    if (filterMode == TIME_SORT) {
                        removeWithDivider(viewHolder.adapterPosition)
                    } else {
                        removeTask(viewHolder.adapterPosition)
                    }
                    addingTask = false
                } else {
                    (viewHolder.itemView as AddTask).save()
                }
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

            if (viewHolder.itemViewType == TaskAdapter.TASK_VIEW_ITEM) {
                val deleteIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_done_all_black_24dp)!!.mutate()
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                val intrinsicWidth = deleteIcon.intrinsicWidth
                val intrinsicHeight = deleteIcon.intrinsicHeight
                val background = GradientDrawable()
                val newDx = dX * Values.swipeFriction
                val alpha: Float = if (newDx > swipeThreshold) 1f else newDx / swipeThreshold
                val backgroundColor = Color.argb((alpha * 255).toInt(), 27, 125, 27)


                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                // Draw the green delete background
                background.setColor(backgroundColor)
                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )

                //background.setCornerRadius(0);


                background.draw(c)

                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.left + intrinsicWidth
                val deleteIconRight = itemView.left + intrinsicWidth * 2
                val deleteIconBottom = deleteIconTop + intrinsicHeight
                // Draw the delete icon
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon.draw(c)

                if (newDx >= swipeThreshold && !vibedOnSwipe) {
                    Vibes.vibrate()
                    vibedOnSwipe = true
                }
                if (newDx <= swipeThreshold && vibedOnSwipe) {
                    vibedOnSwipe = false
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    isSwiping = isCurrentlyActive
                }
            } else {
                val newDx = dX * Values.swipeFriction

                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                val deleteIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_clear_black_24dp)!!.mutate()

                deleteIcon.setColorFilter(Utilities.colorWithAlpha(Utilities.textFromBackground(Cyanea.instance.backgroundColor), .7f), PorterDuff.Mode.SRC_ATOP)
                val intrinsicWidth = deleteIcon.intrinsicWidth
                val intrinsicHeight = deleteIcon.intrinsicHeight

                if (newDx >= swipeThreshold && !vibedOnSwipe) {
                    Vibes.vibrate()
                    vibedOnSwipe = true
                }
                if (newDx <= swipeThreshold && vibedOnSwipe) {
                    vibedOnSwipe = false
                }


                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.left + intrinsicWidth
                val deleteIconRight = itemView.left + intrinsicWidth * 2
                val deleteIconBottom = deleteIconTop + intrinsicHeight
                // Draw the delete icon
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)
            }
        }

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
            return swipeThreshold / (viewHolder.itemView.width * swipeFriction)
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return mAdapter.selected == -1
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (viewHolder.itemViewType == TaskAdapter.TASK_VIEW_ITEM || viewHolder.itemViewType == TaskAdapter.ADD_EDIT_TASK_PLACEHOLDER) {
                ItemTouchHelper.Callback.makeFlag(
                    ItemTouchHelper.ACTION_STATE_SWIPE,
                    ItemTouchHelper.RIGHT
                )
            } else {
                0
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        Log.d("inflating", "inbox inflating")
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)
        prepareRecyclerView(view)


        mRecyclerView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        mRecyclerView.addOnItemTouchListener(object :
            RecyclerTouchListener(context, mRecyclerView, object :
                RecyclerTouchListener.ClickListener {

                override fun onClick(view: View, position: Int) {}

                override fun onLongClick(view: View, position: Int) {
                    if (!isSwiping
                        && (mAdapter.getItemViewType(position) == 0)
                        && mAdapter.selected == -1
                        && !Current.taskList().any { tasks -> tasks.isEditing }) {

                        Vibes.vibrate()
                        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
                            title = FilteredLists.inboxTaskList[position].listName
                            backgroundColor = Cyanea.instance.accent
                            titleColor = Utilities.textFromBackground(Cyanea.instance.accent)
                            menuRes = R.menu.toolbar_inbox_long_press
                            contentInsetStart = 96

                            slideDown()
                            onSelection {
                                when (it.itemId) {
                                    R.id.editTask -> {
                                        FilteredLists.inboxTaskList[position].isEditing = true
                                        mAdapter.update(FilteredLists.inboxTaskList)
                                        MaterialCab.destroy()
                                        true
                                    }
                                    else -> false
                                }

                            }
                            onCreate { _, _ ->
                                activity?.window?.statusBarColor = Cyanea.instance.accentDark
                                menu!!.forEach { m ->
                                    val icon = m.icon.mutate()
                                        .also { it.setColorFilter(Utilities.textFromBackground(Cyanea.instance.accent), PorterDuff.Mode.SRC_ATOP) }
                                    m.icon = icon
                                }
                                mAdapter.selected = position
                                mAdapter.notifyItemChanged(position)
                            }
                            onDestroy {
                                activity?.window?.statusBarColor = 0x33333333
                                mAdapter.selected = -1
                                mAdapter.notifyItemChanged(position)
                                true
                            }
                        }
                    }
                }
            }) {

        })

        return view


    }


    private fun prepareRecyclerView(view: View) {


        mRecyclerView = view.inboxRecycler

        //mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = CrashFixLinearLayoutManager(view.context)
        mAdapter = TaskAdapter(TaskAdapter.FROM_INBOX, activity as MainActivity)
        mAdapter.setHasStableIds(true)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.isNestedScrollingEnabled = false
        mAdapter.update(FilteredLists.inboxTaskList)
        mRecyclerView.setExpandablePage(view.expandable_page_inbox)

        view.expandable_page_inbox.addStateChangeCallbacks(TaskView.TaskViewPageCallbacks(activity!!))

        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(mRecyclerView)


    }

/*    internal class FlingFixNestedScrollView(ctxt: Context, attributeSet: AttributeSet): NestedScrollView(ctxt, attributeSet){
        override fun stopNestedScroll(type: Int) {
            super.stopNestedScroll(type)
            if(type==ViewCompat.TYPE_NON_TOUCH){

            }
        }
    }*/

    internal class CrashFixLinearLayoutManager(ctxt: Context) : LinearLayoutManager(ctxt) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }


    fun removeWithDivider(position: Int) {

        val dividerPosition = position - 1
        val belowDividerPosition = position + 1

        val tasks = FilteredLists.inboxTaskList[position]
        val dividerTask = FilteredLists.inboxTaskList[dividerPosition]
        val belowDividerTask: Tasks? = if (belowDividerPosition < FilteredLists.inboxTaskList.size) FilteredLists.inboxTaskList[belowDividerPosition] else null


        if (isDivider(dividerTask) and isDivider(belowDividerTask)) {
            removeTask(position, true)
        } else {
            removeTask(position, false)
        }

        if (FilteredLists.browseTaskList.contains(tasks)) {
            FilteredLists.browseTaskList.remove(tasks)
        }

        (activity as MainActivity).browseFragment.mAdapter.update(FilteredLists.browseTaskList)

    }

    private fun removeTask(position: Int, above: Boolean = false) {

        val tasks = FilteredLists.inboxTaskList[position]
        tasks.isArchived = true

        Current.archiveTaskList().add(tasks)
        Current.taskList().remove(tasks)

        FilteredLists.inboxTaskList.removeAt(position)

        if (above) {
            val dividerPosition = position - 1
            FilteredLists.inboxTaskList.removeAt(dividerPosition)
            dividersForTaskCount--
        }

        mAdapter.update(FilteredLists.inboxTaskList)


        AsyncTask.execute {
            if(context==null){
                throw Exception("Context null for removing geofence")
            } else {
                if(tasks.locationReminders.isNotEmpty()) {
                    GeofencingClient(context!!).removeGeofences(tasks.locationReminders.map { it.key.toString() })
                    GeofencingClient(context!!).removeGeofences(tasks.locationReminders.mapNotNull { it.trigger?.key.toString() })
                }
            }
            Current.database().tasksDao().updateTask(tasks)
        }

    }






    @JvmOverloads
    fun setFilterMode(mode: Int = filterMode, sort: Boolean = true) {

        filterMode = mode

        if (sort) {
            var tempList = ArrayList(FilteredLists.inboxTaskList)

            Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size))

            run {
                var i = 0
                while (i < tempList.size) {
                    val task = tempList[i]
                    if (isDivider(task)) {
                        Log.d("removing", "removing " + task.listName)
                        FilteredLists.inboxTaskList.removeAt(i)
                        tempList.removeAt(i)
                        i--
                    }
                    i++
                }

            }

            dividersForTaskCount = 0

            tempList = ArrayList(FilteredLists.inboxTaskList)

            Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size))


            if (mode == TIME_SORT) {

                val endOfDay = LocalTime(23, 59, 59, 999)

                val overdue = TaskAdapter.newDivider("OVERDUE", DateTime(1970, 1, 1, 0, 0))
                val today = TaskAdapter.newDivider("TODAY", DateTime(DateTime.now()))
                val thisWeek = TaskAdapter.newDivider("WEEK", DateTime(DateTime.now().withTime(endOfDay)))
                val thisMonth = TaskAdapter.newDivider("MONTH", DateTime(DateTime.now().plusWeeks(1).minusDays(1).withTime(endOfDay)))
                val future = TaskAdapter.newDivider("FUTURE", DateTime(DateTime.now().plusMonths(1).minusDays(1).withTime(endOfDay)))

                if (tempList.any { tasks ->
                        tasks.nextReminderTime().isBefore(today.nextReminderTime())
                    }) {
                    FilteredLists.inboxTaskList.add(overdue)
                    dividersForTaskCount++
                }
                if (tempList.any { tasks -> tasks.nextReminderTime().isAfter(today.nextReminderTime()) && tasks.nextReminderTime().isBefore(thisWeek.nextReminderTime()) }) {
                    FilteredLists.inboxTaskList.add(today)
                    dividersForTaskCount++
                }
                if (tempList.any { tasks -> tasks.nextReminderTime().isAfter(thisWeek.nextReminderTime()) && tasks.nextReminderTime().isBefore(thisMonth.nextReminderTime()) }) {
                    FilteredLists.inboxTaskList.add(thisWeek)
                    dividersForTaskCount++
                }
                if (tempList.any { tasks -> tasks.nextReminderTime().isAfter(thisMonth.nextReminderTime()) && tasks.nextReminderTime().isBefore(future.nextReminderTime()) }) {
                    FilteredLists.inboxTaskList.add(thisMonth)
                    dividersForTaskCount++
                }
                if (tempList.any { tasks ->
                        tasks.nextReminderTime().isAfter(future.nextReminderTime())
                    }) {
                    FilteredLists.inboxTaskList.add(future)
                    dividersForTaskCount++
                }

            }

            FilteredLists.inboxTaskList.sortWith(Comparator { t1, t2 -> t1.compareTo(t2, filterMode) })
            FilteredLists.inboxTaskList.add(0, TaskAdapter.newHeader())

        }

    }



}


fun isDivider(tasks: Tasks?): Boolean {
    if (tasks == null) {
        return true //return true if no bottom
    }
    return ((tasks.listName == "OVERDUE")
            or (tasks.listName == "TODAY")
            or (tasks.listName == "WEEK")
            or (tasks.listName == "MONTH")
            or (tasks.listName == "FUTURE")
            or (tasks.listName == "INBOX_HEADER"))
}