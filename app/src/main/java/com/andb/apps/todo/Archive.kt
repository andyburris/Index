package com.andb.apps.todo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.databases.tasksDao
import com.andb.apps.todo.filtering.filterArchive
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Values.swipeThreshold
import com.andb.apps.todo.utilities.Vibes
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.activity_archive.*
import kotlinx.android.synthetic.main.activity_main.*
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.PullCollapsibleActivity
import me.saket.inboxrecyclerview.page.InterceptResult

class Archive : PullCollapsibleActivity() {
    private var mLayoutManager: RecyclerView.LayoutManager? = null

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
                val deleteIcon = getDrawable(R.drawable.ic_move_to_inbox_black_24dp)!!.mutate()
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
                val deleteIcon = getDrawable(R.drawable.ic_delete_black_24dp)!!.mutate()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setTheme(R.style.AppThemeLightCollapse)

        setContentView(R.layout.activity_archive)
        if (intent.hasExtra("expandRect")) {
            val expandRect = Rect.unflattenFromString(intent.extras!!.getString("expandRect"))
            if (expandRect != null) {
                expandFrom(expandRect)
            } else {
                expandFromTop()
            }
        } else {
            expandFromTop()
        }

        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp)
        setSupportActionBar(toolbar)

        archiveRecycler.setBackgroundColor(Cyanea.instance.backgroundColor)
        prepareRecyclerView()

        val expandablePageLayout = expandable_page_archive
        expandablePageLayout.pullToCollapseInterceptor = { downX, downY, upwardPull ->
            val directionInt = if (upwardPull) +1 else -1
            val canScrollFurther = archiveRecycler.canScrollVertically(directionInt)
            if (canScrollFurther) InterceptResult.INTERCEPTED else InterceptResult.IGNORED
        }

    }


    fun prepareRecyclerView() {
        Log.d("recycler", "preparing archive rView")
        mRecyclerView = findViewById<View>(R.id.archiveRecycler) as InboxRecyclerView

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true)

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = TaskAdapter(this)
        tasksDao().all.observe(this, listObserver)
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

    }

    val listObserver = Observer<List<Tasks>> { newTasks ->
        mAdapter.update(newTasks.filterArchive())
    }

    lateinit var mRecyclerView: InboxRecyclerView
    lateinit var mAdapter: TaskAdapter

    private fun isMaxScrollReached(recyclerView: RecyclerView): Boolean {
        val maxScroll = recyclerView.computeVerticalScrollRange()
        val currentScroll = recyclerView.computeVerticalScrollOffset() + recyclerView.computeVerticalScrollExtent()
        return currentScroll >= maxScroll
    }


}
