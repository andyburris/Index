package com.andb.apps.todo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.MaterialCab
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.utilities.Values
import com.andb.apps.todo.utilities.Values.swipeFriction
import com.andb.apps.todo.utilities.Values.swipeThreshold
import com.andb.apps.todo.utilities.Vibes
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.fragment_inbox.view.*
import me.saket.inboxrecyclerview.InboxRecyclerView

/*Sort keys*/
const val SORT_TIME = 32982
const val SORT_ALPHA = 87432

class InboxFragment : Fragment() {

    internal var isSwiping = false
    var filterMode = SORT_TIME

    var editingId = -1
    var adding = false
    fun isEditing(): Boolean = editingId != -1 || adding

    lateinit var mRecyclerView: InboxRecyclerView
    lateinit var mAdapter: TaskAdapter
    val inboxViewModel: InboxViewModel by lazy {
        ViewModelProviders.of(this).get(InboxViewModel::class.java)
    }


    var tasks = ArrayList<Tasks>()

    // Extend the Callback class
    private val _ithCallback = object : ItemTouchHelper.Callback() {

        var vibedOnSwipe: Boolean = false

        //and in your implementation of
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d("swipeAction", "swiped")

            val task = tasks.get(viewHolder.adapterPosition)
            task.apply {
                if (viewHolder.itemViewType != TaskAdapter.ADD_EDIT_TASK_PLACEHOLDER) {
                    mAdapter.notifyItemRemoved(viewHolder.adapterPosition)//so it does not have to wait for database processing
                    inboxViewModel.archiveTask(context, this)
                } else {
                    if (adding) {
                        mAdapter.notifyItemRemoved(viewHolder.adapterPosition)//so it does not have to wait for database processing
                        inboxViewModel.archiveTask(context, this)
                        adding = false
                        editingId = -1
                    } else {
                        (viewHolder.itemView as AddTask).save()
                    }
                }
            }

        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

            if (viewHolder.itemViewType == TaskAdapter.TASK_VIEW_ITEM) {
                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_done_all_black_24dp)!!.mutate()
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

                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear_black_24dp)!!.mutate()

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


        mRecyclerView.addOnItemTouchListener(object :
            RecyclerTouchListener(context, mRecyclerView, object :
                RecyclerTouchListener.ClickListener {

                override fun onClick(view: View, position: Int) {}

                override fun onLongClick(view: View, position: Int) {
                    if (!isSwiping
                        && (mAdapter.getItemViewType(position) == 0)
                        && mAdapter.selected == -1
                        && !mAdapter.taskList.any { tasks -> tasks.isEditing }) {

                        Vibes.vibrate()
                        MaterialCab.attach(activity as AppCompatActivity, R.id.cab_stub) {
                            title = mAdapter.taskList[position].listName
                            backgroundColor = Cyanea.instance.accent
                            titleColor = Utilities.textFromBackground(Cyanea.instance.accent)
                            menuRes = R.menu.toolbar_inbox_long_press
                            contentInsetStart = 96

                            slideDown()
                            onSelection {
                                when (it.itemId) {
                                    R.id.editTask -> {
                                        mAdapter.taskList[position].isEditing = true
                                        MaterialCab.destroy()
                                        true
                                    }
                                    else -> false
                                }

                            }
                            onCreate { _, _ ->
                                activity?.window?.statusBarColor = Cyanea.instance.accentDark
                                menu?.forEach { m ->
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
        mAdapter = TaskAdapter(activity as MainActivity)
        mAdapter.setHasStableIds(true)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.isNestedScrollingEnabled = false
        //mAdapter.update(tasksDao().all)
        inboxViewModel.getTasks().observe(viewLifecycleOwner, listObserver)
        mRecyclerView.setExpandablePage(view.expandable_page_inbox)

        view.expandable_page_inbox.addStateChangeCallbacks(TaskView.TaskViewPageCallbacks(requireActivity()))

        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(mRecyclerView)



    }

    private val listObserver = Observer<List<Tasks>> { newTasks ->
        if(newTasks!=null) {
            Log.d("taskRefresh", "refreshing tasks with ${newTasks.size} tasks")
            tasks = ArrayList(newTasks)
            mAdapter.update(newTasks.also {
                if (isEditing()) it.find { it.listKey == editingId }?.isEditing = true
            })
        }
    }


    internal class CrashFixLinearLayoutManager(ctxt: Context) : LinearLayoutManager(ctxt) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return true
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