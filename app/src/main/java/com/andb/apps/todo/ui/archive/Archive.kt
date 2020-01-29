package com.andb.apps.todo.ui.archive

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.MainActivity
import com.andb.apps.todo.R
import com.andb.apps.todo.ui.inbox.TaskAdapter
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.filtering.filterArchive
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Values.swipeThreshold
import com.andb.apps.todo.util.Vibes
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.activity_archive.*
import kotlinx.android.synthetic.main.bottom_sheet_container.*
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult
import me.saket.inboxrecyclerview.page.PullToCollapseListener

class Archive : Fragment() {
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private val expandablePageLayout by lazy { view!!.parent as ExpandablePageLayout }
    lateinit var backingAdapter: RecyclerView.Adapter<*>

    // Extend the Callback class
    private val _ithCallback = object : ItemTouchHelper.Callback() {

        var vibedOnSwipe: Boolean = false

        //and in your implementation of
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d("swipeAction", "swiped")

            val task = mAdapter.taskList[viewHolder.adapterPosition]
            if (direction == ItemTouchHelper.RIGHT) {//restore
                task.isArchived = false
                ProjectsUtils.update(task)
            } else if (direction == ItemTouchHelper.LEFT) {//delete permanently
                AsyncTask.execute {
                    Current.database().tasksDao().deleteTask(task)
                }

            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {


            if (dX > 0) { //restore
                val deleteIcon = requireContext().getDrawable(R.drawable.ic_move_to_inbox_black_24dp)!!.mutate()
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                val intrinsicWidth = deleteIcon.intrinsicWidth
                val intrinsicHeight = deleteIcon.intrinsicHeight
                val background = GradientDrawable()
                val newDx = dX * 6 / 10
                val alpha: Float
                if (newDx > swipeThreshold)
                    alpha = 1f
                else
                    alpha = newDx / swipeThreshold
                val backgroundColor = Color.argb(Math.round(alpha * 255), 27, 125, 27)


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

                if (newDx >= swipeThreshold && (!vibedOnSwipe)) {
                    Vibes.vibrate()
                    vibedOnSwipe = true
                }
                if (newDx <= swipeThreshold && vibedOnSwipe) {
                    vibedOnSwipe = false
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)
            } else { //delete
                val deleteIcon = requireContext().getDrawable(R.drawable.ic_delete_black_24dp)!!.mutate()
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                val intrinsicWidth = deleteIcon.intrinsicWidth
                val intrinsicHeight = deleteIcon.intrinsicHeight
                val background = GradientDrawable()

                val newDx = dX * 6 / 10
                /*float swipeThreshold = viewHolder.itemView.getWidth() - Values.swipeThreshold;*/
                val alpha: Float
                if (-newDx > swipeThreshold)
                    alpha = 1f
                else
                    alpha = -newDx / swipeThreshold
                val backgroundColor = Color.argb(Math.round(alpha * 255), 217, 48, 37)


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
                val deleteIconLeft = itemView.right - intrinsicWidth * 2
                val deleteIconRight = itemView.right - intrinsicWidth
                val deleteIconBottom = deleteIconTop + intrinsicHeight
                // Draw the delete icon
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon.draw(c)

                if (-newDx >= swipeThreshold && (!vibedOnSwipe)) {
                    Vibes.vibrate()
                    vibedOnSwipe = true
                }
                if (-newDx <= swipeThreshold && vibedOnSwipe) {
                    vibedOnSwipe = false
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive)
            }
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(
                ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.activity_archive, container?.parent as ViewGroup?, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Cyanea.instance.tint(toolbar.menu, requireActivity())
        archiveRecycler.setBackgroundColor(Cyanea.instance.backgroundColor)
        prepareRecyclerView()

        expandablePageLayout.apply {
            pullToCollapseInterceptor = { downX, downY, upwardPull ->
                val directionInt = if (upwardPull) +1 else -1
                val canScrollFurther = archiveRecycler.canScrollVertically(directionInt)
                if (canScrollFurther) InterceptResult.INTERCEPTED else InterceptResult.IGNORED
            }



            addOnPullListener(object : PullToCollapseListener.OnPullListener {
                override fun onPull(deltaY: Float, currentTranslationY: Float, upwardPull: Boolean, deltaUpwardPull: Boolean, collapseEligible: Boolean) {}

                override fun onRelease(collapseEligible: Boolean) {
                    if (collapseEligible) {
                        (activity as MainActivity).bottom_sheet_container.visibility = View.VISIBLE
                        backingAdapter.notifyDataSetChanged()
                    }
                }

            })
        }

    }

    fun setupScroll(adapter: RecyclerView.Adapter<*>) {
        backingAdapter = adapter
        (activity as MainActivity).bottom_sheet_container.visibility = View.GONE//otherwise bottom sheet intercepts touches on bottom
    }


    private fun prepareRecyclerView() {
        Log.d("recycler", "preparing archive rView")
        mRecyclerView = archiveRecycler

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true)

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(context)
        mRecyclerView.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = TaskAdapter(activity as MainActivity)
        tasksDao().all.observe(this, listObserver)
        ProjectList.getKey().observe(this, projectObserver)
        mAdapter.setHasStableIds(true)
        mRecyclerView.adapter = mAdapter

        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(mRecyclerView)

        mRecyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isMaxScrollReached(recyclerView)/*at end of scroll*/) {
                } else {
                }
            }
        })

        archiveRecycler.requestFocus()


    }

    val listObserver = Observer<List<Task>> { newTasks ->
        mAdapter.update(newTasks.filterArchive(true))
    }

    val projectObserver = Observer<Int>{
        tasksDao().all.removeObserver(listObserver)
        tasksDao().all.observe(this, listObserver)
    }

    lateinit var mRecyclerView: InboxRecyclerView
    lateinit var mAdapter: TaskAdapter

    private fun isMaxScrollReached(recyclerView: RecyclerView): Boolean {
        val maxScroll = recyclerView.computeVerticalScrollRange()
        val currentScroll = recyclerView.computeVerticalScrollOffset() + recyclerView.computeVerticalScrollExtent()
        return currentScroll >= maxScroll
    }


}
