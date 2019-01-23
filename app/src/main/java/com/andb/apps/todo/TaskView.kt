package com.andb.apps.todo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.TaskListItem
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.CyaneaFragment
import kotlinx.android.synthetic.main.activity_task_view.*
import kotlinx.android.synthetic.main.content_task_view.*
import kotlinx.android.synthetic.main.inbox_checklist_list_item.view.*
import kotlinx.android.synthetic.main.inbox_list_item.*
import kotlinx.android.synthetic.main.inbox_list_item.view.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import java.lang.ClassCastException
import java.lang.Exception
import java.util.*

class TaskView : CyaneaFragment() {


    private val expandablePageLayout by lazy { view!!.parent as ExpandablePageLayout }
    val mAdapter by lazy { subtaskAdapter() }
    lateinit var expandedItem: TaskListItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments

        position = bundle!!.getInt("pos")
        inboxBrowseArchive = bundle.getInt("inboxBrowseArchive")

        when (inboxBrowseArchive) {
            TaskAdapter.FROM_BROWSE -> {
                task = FilteredLists.browseTaskList[position]
                expandedItem = BrowseFragment.mRecyclerView[position] as TaskListItem
            }
            TaskAdapter.FROM_ARCHIVE -> task = Current.project().archiveList[position]
            else //inbox
            -> {
                task = FilteredLists.inboxTaskList[position]
                expandedItem = InboxFragment.mRecyclerView[position] as TaskListItem
            }
        }


    }


    fun onItemsChanged(){
        expandedItem.apply {
            val checkBoxes = ArrayList(Arrays.asList<CheckBox>(item1, item2, item3))
            for ((i, c) in checkBoxes.withIndex()){
                c.text = task.listItems[i]
                c.isChecked = task.listItemsChecked[i]
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_task_view, container!!.parent as ViewGroup, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        task_view_parent.setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
        collapseAndChangeAppBar(activity!!.findViewById(R.id.toolbar), activity!!.findViewById(R.id.fab))


        Log.d("onePosUpError", task.listName)


        task_view_task_name.text = task.listName.toUpperCase()

        if (!task.isListTime) { //no time
            taskViewTimeText!!.visibility = View.GONE
            taskViewTimeIcon!!.visibility = View.GONE
        } else if (task.dateTime.secondOfMinute == 59) { //date only
            taskViewTimeText!!.text = task.dateTime.toString("EEEE, MMMM d")
            taskViewTimeIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_event_black_24dp))
        } else {
            taskViewTimeText!!.text = task.dateTime.toString("hh:mm a| EEEE, MMMM d")
        }

        prepareRecyclerViews(task)

        expandablePageLayout.pullToCollapseInterceptor = { downX, downY, upwardPull ->
            val directionInt = if (upwardPull) +1 else -1
            val canScrollFurther = taskViewScrollLayout.canScrollVertically(directionInt)
            if (canScrollFurther) InterceptResult.INTERCEPTED else InterceptResult.IGNORED
        }
    }

    fun collapseAndChangeAppBar(toolbar: Toolbar, fab: FloatingActionButton) {
        oldNavIcon = toolbar.navigationIcon!!.mutate()

        val newIcon = resources.getDrawable(R.drawable.ic_clear_black_24dp)
        newIcon.setColorFilter(Utilities.textFromBackground(cyanea.primary), PorterDuff.Mode.SRC_ATOP)
        toolbar.navigationIcon = newIcon

        TransitionManager.beginDelayedTransition(toolbar.rootView as ViewGroup, ChangeBounds())

        Drawer.bottomSheetBehavior.peekHeight = Utilities.pxFromDp(136 - 48)
        Drawer.bottomSheetBehavior.setBottomSheetCallback(Drawer.collapsedSheetCallback)

        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_done_all_black_24dp).mutate())

        toolbar.menu.setGroupVisible(R.id.toolbar_task_view, true)
        toolbar.menu.setGroupVisible(R.id.toolbar_main, false)
    }

    fun subtaskAdapter() = Klaster.get()
            .itemCount { task.listItemsSize }
            .view(R.layout.inbox_checklist_list_item, layoutInflater)
            .bind { pos ->
                itemView.listTextView.apply {
                    backgroundTintList = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
                    text = task.getListItems(pos)
                    isChecked = task.getListItemsChecked(pos)
                    paintFlags = if (!isChecked) paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setOnCheckedChangeListener { _, isChecked ->
                        task.editListItemsChecked(isChecked, pos)
                        paintFlags = if (!isChecked) paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        onItemsChanged()
                        AsyncTask.execute {
                            ProjectsUtils.update(task)
                        }
                    }
                }

            }
            .build()

    fun tagAdapter() = Klaster.get()
            .itemCount { task.listTagsSize }
            .view(R.layout.task_view_tag_list_item, layoutInflater)
            .bind { pos ->
                val tag = Current.project().tagList[task.getListTags(pos)]
                itemView.tagImage.setColorFilter(tag.tagColor)
                itemView.task_view_item_tag_name.text = tag.tagName
            }
            .build()


    fun prepareRecyclerViews(task: Tasks) {
        val mRecyclerView = taskViewRecycler
        // use a linear layout manager
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        // specify an adapter (see also next example)
        mRecyclerView.adapter = mAdapter
        ItemTouchHelper(_ithDragCallback).attachToRecyclerView(mRecyclerView)


        val tRecyclerView = taskViewTagRecycler
        // use a linear layout manager
        tRecyclerView.layoutManager = LinearLayoutManager(context)

        // specify an adapter (see also next example)
        val tAdapter = tagAdapter()
        tRecyclerView.adapter = tAdapter
    }

    private var _ithDragCallback = object : ItemTouchHelper.Callback() {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition


            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(task.listItems, i, i + 1)
                    Collections.swap(task.listItemsChecked, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(task.listItems, i, i - 1)
                    Collections.swap(task.listItemsChecked, i, i - 1)
                }
            }
            mAdapter.notifyItemMoved(fromPosition, toPosition)
            onItemsChanged()
            ProjectsUtils.update(task)
            return false

        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
        override fun isLongPressDragEnabled(): Boolean {
            return true
        }
        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }
        //defines the enabled move directions in each state (idle, swiping, dragging).
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }
    }


    override fun onPause() {
        super.onPause()
        InboxFragment.mAdapter.notifyDataSetChanged()
        BrowseFragment.mAdapter.notifyDataSetChanged()
    }

    companion object {

        lateinit var oldNavIcon: Drawable
        var oldMargin: Int = 0
        var pageState = 0
        internal var position: Int = 0
        internal var inboxBrowseArchive: Int = 0 //0 is inbox, 1 is browse, 2 is archive
        lateinit var task: Tasks


        fun editFromToolbar(ctxt: Context) {
            when (inboxBrowseArchive) {
                TaskAdapter.FROM_BROWSE -> {
                    val editTask = Intent(ctxt, AddTask::class.java)
                    editTask.putExtra("edit", true)
                    editTask.putExtra("editPos", position)
                    editTask.putExtra("browse", true)
                    ctxt.startActivity(editTask)
                }
                TaskAdapter.FROM_ARCHIVE -> {
                }
                else //inbox
                -> {
                    val editTask = Intent(ctxt, AddTask::class.java)
                    editTask.putExtra("edit", true)
                    editTask.putExtra("editPos", position)
                    editTask.putExtra("browse", false)
                    ctxt.startActivity(editTask)
                }
            }
        }

        fun taskDone() {
            Current.archiveTaskList().add(task)
            when (inboxBrowseArchive) {
                TaskAdapter.FROM_BROWSE -> {
                    Current.taskList().apply {
                        removeAt(indexOf(task))
                    }
                }
                TaskAdapter.FROM_ARCHIVE -> {
                }
                else //inbox
                -> {
                    InboxFragment.removeWithDivider(position)
                }
            }
            InboxFragment.mRecyclerView.collapse()
            BrowseFragment.mRecyclerView.collapse()
        }

    }


    class TaskViewPageCallbacks(val activity: Activity) : SimplePageStateChangeCallbacks() {
        override fun onPageAboutToExpand(expandAnimDuration: Long) {
            super.onPageAboutToExpand(expandAnimDuration)
            pageState = 1
        }

        override fun onPageExpanded() {
            super.onPageExpanded()
            pageState = 2
        }

        override fun onPageCollapsed() {
            super.onPageCollapsed()
            pageState = 0


            val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
            val fab: FloatingActionButton = activity.findViewById(R.id.fab)
            Drawer.bottomSheetBehavior.setBottomSheetCallback(Drawer.normalSheetCallback)

            android.transition.TransitionManager.beginDelayedTransition(toolbar.getRootView() as ViewGroup, android.transition.ChangeBounds())
            Drawer.bottomSheetBehavior.peekHeight = Utilities.pxFromDp(136)
            toolbar.navigationIcon = TaskView.oldNavIcon

            fab.setImageDrawable(activity.getDrawable(R.drawable.ic_add_black_24dp)?.mutate())
            toolbar.menu.setGroupVisible(R.id.toolbar_task_view, false)
            toolbar.menu.setGroupVisible(R.id.toolbar_main, true)
        }

        override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
            super.onPageAboutToCollapse(collapseAnimDuration)
            pageState = 3
        }

    }


}
