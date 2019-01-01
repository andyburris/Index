package com.andb.apps.todo

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.afollestad.materialcab.MaterialCab
import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.lists.TaskList
import com.andb.apps.todo.lists.interfaces.TaskListInterface
import com.andb.apps.todo.objects.Tasks
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.CyaneaFragment

import org.joda.time.DateTime

import java.util.ArrayList
import java.util.Collections
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.saket.inboxrecyclerview.InboxRecyclerView

import kotlinx.android.synthetic.main.fragment_inbox.*
import kotlinx.android.synthetic.main.fragment_inbox.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [InboxFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [InboxFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class InboxFragment : CyaneaFragment() {

    private val contextualToolbar: ActionMode? = null
    var selected = false


    private var mListener: OnFragmentInteractionListener? = null



    internal var isSwiping = false
    // Extend the Callback class
    private val _ithCallback = object : ItemTouchHelper.Callback() {
        //and in your implementation of
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d("swipeAction", "swiped")

            TransitionManager.beginDelayedTransition(mRecyclerView, ChangeBounds())
            val layoutParams = mRecyclerView.getChildAt(viewHolder.adapterPosition).layoutParams
            layoutParams.height = 0
            mRecyclerView.getChildAt(viewHolder.adapterPosition).layoutParams = layoutParams
            if (InboxFragment.filterMode == 0) {
                Log.d("removing", "removing date sorted " + Integer.toString(viewHolder.adapterPosition))
                removeWithDivider(viewHolder.adapterPosition)
            } else {
                Log.d("removing", "removing alphabetical " + Integer.toString(viewHolder.adapterPosition))
                removeTask(viewHolder.adapterPosition, false)
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

            val deleteIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_done_all_black_24dp)!!.mutate()
            deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            val intrinsicWidth = deleteIcon.intrinsicWidth
            val intrinsicHeight = deleteIcon.intrinsicHeight
            val background = GradientDrawable()
            val backgroundColor = Color.parseColor("#1B7D1B")


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

            var newDx = dX * 9 / 10
            if (newDx >= 300f) {
                newDx = 300f
            }

            super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                isSwiping = isCurrentlyActive
            }
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (viewHolder.itemViewType == 0) {
                ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,
                        ItemTouchHelper.RIGHT)
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

        taskCountText = view.findViewById(R.id.task_count_text)
        currentPathText = view.findViewById(R.id.task_path_text)

        setPathText(Filters.subtitle)
        setTaskCountText(TaskList.taskList.size)

        val tagButton = view.findViewById<Button>(R.id.tag_button)
        val bgdrawable = resources.getDrawable(R.drawable.rounded_button_background).mutate()
        bgdrawable.setColorFilter(Cyanea.instance.accent, PorterDuff.Mode.SRC_ATOP)
        tagButton.background = bgdrawable
        tagButton.backgroundTintList = ColorStateList.valueOf(Cyanea.instance.accent)


        val drawable = resources.getDrawable(R.drawable.ic_label_black_24dp).mutate()
        if (Utilities.lightOnBackground(Cyanea.instance.accent)) {
            val color = -0x1
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            tagButton.setTextColor(color)
        } else {
            val color = -0x1000000
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            tagButton.setTextColor(color)
        }
        tagButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)


        mRecyclerView.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        mRecyclerView.addOnItemTouchListener(object : RecyclerTouchListener(context, mRecyclerView, object : RecyclerTouchListener.ClickListener {

            override fun onClick(view: View, position: Int) {}

            override fun onLongClick(view: View, position: Int) {
                if (!isSwiping && (mAdapter.getItemViewType(position) == 0) and !selected) {
                    //contextualToolbar = InboxFragment.this.getActivity().startActionMode(setCallback(position));
                    MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub){
                        title = FilteredLists.inboxTaskList[position].listName
                        backgroundColor = cyanea.accent
                        titleColor = Utilities.textFromBackground(cyanea.accent)
                        menuRes = R.menu.toolbar_inbox_long_press
                        contentInsetStart = 96

                        slideDown()
                        onSelection {
                            when (it.itemId){
                                R.id.editTask ->{
                                    val editTask = Intent(context, AddTask::class.java)
                                    editTask.putExtra("edit", true)
                                    editTask.putExtra("editPos", position)
                                    editTask.putExtra("browse", false)
                                    startActivity(editTask)
                                    MaterialCab.destroy()
                                    true
                                }else -> false
                            }

                        }
                        onCreate { cab, menu ->
                            activity?.window?.statusBarColor = cyanea.accentDark
                        }
                        onDestroy {
                            selected = false
                            activity?.window?.statusBarColor = 0x33333333
                            true
                        }
                    }
                    view.isSelected = true
                    mAdapter.isSelected = true
                    mAdapter.notifyItemChanged(position)
                    selected = true
                }
            }
        }) {

        })



        tagButton.setOnClickListener {
            val intent = Intent(context, TagSelect::class.java)
            intent.putExtra("isTagLink", false)
            startActivity(intent)
        }

        return view


    }



    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }


    fun prepareRecyclerView(view: View) {


        mRecyclerView = view.findViewById<View>(R.id.inboxRecycler) as InboxRecyclerView

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true)

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(view.context)
        mRecyclerView.layoutManager = mLayoutManager

        // specify an adapter (see also next example)

        mAdapter = TaskAdapter(FilteredLists.inboxTaskList, TaskAdapter.FROM_INBOX)

        mAdapter.setHasStableIds(true)

        mRecyclerView.adapter = mAdapter

        mRecyclerView.setNested(true)



        mRecyclerView.setExpandablePage(view.expandable_page_inbox)

        view.expandable_page_inbox.addStateChangeCallbacks(TaskView.TaskViewPageCallbacks(activity!!))

        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(mRecyclerView)


    }

    private fun removeWithDivider(position: Int) {

        val startTime = System.nanoTime()


        val dividerPosition = position - 1
        val belowDividerPosition = position + 1
        Log.d("removing", "Above: " + FilteredLists.inboxTaskList[dividerPosition].listName + "\n")
        Log.d("removing", "Clicked: " + FilteredLists.inboxTaskList[position].listName + "\n")
        //Log.d("removing", "Below: " + FilteredLists.inboxTaskList.get(belowDividerPosition).getListName() + "\n");

        val tasks = FilteredLists.inboxTaskList[position]
        val dividerTask = FilteredLists.inboxTaskList[dividerPosition]


        if ((dividerTask.listName == "OVERDUE")
                or (dividerTask.listName == "TODAY")
                or (dividerTask.listName == "WEEK")
                or (dividerTask.listName == "MONTH")
                or (dividerTask.listName == "FUTURE")) {

            if (belowDividerPosition < FilteredLists.inboxTaskList.size) {
                Log.d("size", Integer.toString(FilteredLists.inboxTaskList.size) + ", " + belowDividerPosition)
                val belowDividerTask = FilteredLists.inboxTaskList[belowDividerPosition]
                if ((belowDividerTask.listName == "OVERDUE")
                        or (belowDividerTask.listName == "TODAY")
                        or (belowDividerTask.listName == "WEEK")
                        or (belowDividerTask.listName == "MONTH")
                        or (belowDividerTask.listName == "FUTURE")) {

                    removeTask(position, true) //sandwiched by dividers, remove top one
                    if (dividerPosition == 0) {
                        mAdapter.notifyItemChanged(0) //updates top divider if necessary to redo padding
                    }

                } else { //more tasks below this
                    removeTask(position, false)
                }
            } else { //last in list w/ divider above this
                removeTask(position, true)
            }


        } else {
            removeTask(position, false)
        }

        if (FilteredLists.browseTaskList.contains(tasks)) {
            FilteredLists.browseTaskList.remove(tasks)
        }

        BrowseFragment.mAdapter.notifyDataSetChanged()

        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1000000  //divide by 1000000 to get milliseconds.

        Log.d("removeTaskTime", java.lang.Long.toString(duration) + " milliseconds")


    }

    private fun removeTask(position: Int, above: Boolean) {

        Log.d("removeTask", "removing task")


        val tasks = FilteredLists.inboxTaskList[position]

        TaskListInterface.removeTask(tasks)

        FilteredLists.inboxTaskList.removeAt(position)

        mAdapter.notifyItemRemoved(position)

        val dividerPosition = position - 1

        if (above) {


            FilteredLists.inboxTaskList.removeAt(dividerPosition)

            mAdapter.notifyItemRemoved(dividerPosition)
        }

        mAdapter.notifyItemRangeChanged(dividerPosition, mAdapter.itemCount)


    }

    companion object {


        private var filterMode = 0 //0=date, 1=alphabetical, more to come


        lateinit var mRecyclerView: InboxRecyclerView
        lateinit var mAdapter: TaskAdapter

        private var taskCountText: TextView? = null
        private var currentPathText: TextView? = null

        private var noTasks: TextView? = null

        fun newInstance(): InboxFragment {
            return InboxFragment()
        }


        fun getFilterMode(): Int{
            return filterMode
        }
        fun setFilterMode(mode: Int) {

            filterMode = mode

            var tempList = ArrayList(FilteredLists.inboxTaskList)

            Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size))

            run {
                var i = 0
                while (i < tempList.size) {
                    val task = tempList[i]
                    if ((task.listName == "OVERDUE") or (task.listName == "TODAY") or (task.listName == "WEEK") or (task.listName == "MONTH") or (task.listName == "FUTURE")) {
                        Log.d("removing", "removing " + task.listName)
                        FilteredLists.inboxTaskList.removeAt(i)
                        tempList.removeAt(i)
                        i--
                    }
                    i++
                }
            }

            tempList = ArrayList(FilteredLists.inboxTaskList)

            Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size))


            if (mode == 0) {


                var overdue = true
                var today = true
                var thisWeek = true
                var thisMonth = true
                var future = true


                Log.d("loopStart", "Size: " + Integer.toString(FilteredLists.inboxTaskList.size))

                var i = 0

                for (task in tempList) {


                    Log.d("loopStart", "loop through " + Integer.toString(i))
                    val taskDateTime = DateTime(task.dateTime)
                    if (taskDateTime.isBefore(DateTime.now())) {
                        if (overdue) {
                            Log.d("addDivider", "adding OVERDUE from " + task.listName + ", " + task.dateTime.toString())
                            val tasks = Tasks("OVERDUE", ArrayList(), ArrayList(), ArrayList(), DateTime(1970, 1, 1, 0, 0), false)

                            FilteredLists.inboxTaskList.add(i, tasks)


                            overdue = false
                        }
                    } else if (taskDateTime.isBefore(DateTime.now().withTime(23, 59, 59, 999))) {
                        if (today) {
                            Log.d("addDivider", "adding TODAY from " + task.listName + ", " + task.dateTime.toString())
                            Log.d("addDivider", task.listName)
                            val tasks = Tasks("TODAY", ArrayList(), ArrayList(), ArrayList(), DateTime(DateTime.now()), false)//drop one category to show at top

                            FilteredLists.inboxTaskList.add(i, tasks)


                            today = false
                        }
                    } else if (taskDateTime.isBefore(DateTime.now().plusWeeks(1).minusDays(1).withTime(23, 59, 59, 999))) {
                        if (thisWeek) {
                            Log.d("addDivider", "adding WEEK from " + task.listName + ", " + task.dateTime.toString() + " at position " + Integer.toString(i))
                            val tasks = Tasks("WEEK", ArrayList(), ArrayList(), ArrayList(), DateTime(DateTime.now().withTime(23, 59, 59, 999)), false)

                            FilteredLists.inboxTaskList.add(i, tasks)


                            thisWeek = false
                        }
                    } else if (taskDateTime.isBefore(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999))) {
                        if (thisMonth) {
                            Log.d("addDivider", "adding MONTH from " + task.listName + ", " + task.dateTime.toString())
                            val tasks = Tasks("MONTH", ArrayList(), ArrayList(), ArrayList(), DateTime(DateTime.now().plusWeeks(1).minusDays(1).withTime(23, 59, 59, 999)), false)

                            FilteredLists.inboxTaskList.add(i, tasks)

                            thisMonth = false
                        }

                    } else if (taskDateTime.isAfter(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999))) {
                        if (future) {
                            Log.d("addDivider", "adding FUTURE from " + task.listName + ", " + task.dateTime.toString())
                            val tasks = Tasks("FUTURE", ArrayList(), ArrayList(), ArrayList(), DateTime(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999)), false)

                            FilteredLists.inboxTaskList.add(i, tasks)


                            future = false
                        }

                    }

                    i++
                }

                Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size))


                Collections.sort(FilteredLists.inboxTaskList) { o1, o2 ->
                    if (o1.dateTime == null) {
                        o1.dateTime = DateTime(1970, 1, 1, 0, 0, 0)
                    }
                    if (o2.dateTime == null) {
                        o2.dateTime = DateTime(1970, 1, 1, 0, 0, 0)
                    }

                    o1.dateTime.compareTo(o2.dateTime)
                }


            } else if (mode == 1) {


                Collections.sort(FilteredLists.inboxTaskList) { o1, o2 -> o1.listName.compareTo(o2.listName) }


            }


        }

        fun refreshWithAnim() {
            mAdapter.notifyDataSetChanged()
            mRecyclerView.scheduleLayoutAnimation()
        }

        fun setTaskCountText(numTasks: Int) {
            var toApply: String
            if (numTasks != 1) {
                toApply = " TASKS"
            } else {
                toApply = " TASK"
            }
            toApply = Integer.toString(numTasks) + toApply
            taskCountText!!.text = toApply

        }

        fun setPathText(text: String) {
            currentPathText!!.text = text
        }
    }

}// Required empty public constructor