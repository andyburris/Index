package com.andb.apps.todo

import android.app.Activity
import android.content.Context
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.*
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.CyaneaFragment
import kotlinx.android.synthetic.main.activity_task_view.*
import kotlinx.android.synthetic.main.content_task_view.*
import kotlinx.android.synthetic.main.inbox_checklist_list_item.view.*
import kotlinx.android.synthetic.main.inbox_list_item.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import java.util.*
import kotlin.collections.ArrayList


class TaskView : Fragment() {

    private val expandablePageLayout by lazy { view!!.parent as ExpandablePageLayout }
    val mAdapter by lazy { subtaskAdapter() }
    lateinit var viewModel: TaskViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments

        val key = bundle!!.getInt("key")
        viewModel = ViewModelProviders.of(this, viewModelFactory { TaskViewViewModel(key) })
            .get(TaskViewViewModel::class.java)


        editFromToolbarFun = ::editFromToolbar
        taskDoneFun = ::taskDone

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.viewModelTask.observe(viewLifecycleOwner, taskObserver)
        return inflater.inflate(R.layout.activity_task_view, container!!.parent as ViewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        task_view_parent.setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
        collapseAndChangeAppBar(activity!!.findViewById(R.id.toolbar), activity!!.findViewById(R.id.fab))

        prepareRecyclerViews()

        expandablePageLayout.pullToCollapseInterceptor = { downX, downY, upwardPull ->
            val directionInt = if (upwardPull) +1 else -1
            val canScrollFurther = taskViewScrollLayout.canScrollVertically(directionInt)
            if (canScrollFurther) InterceptResult.INTERCEPTED else InterceptResult.IGNORED
        }

    }

    private val taskObserver = Observer<Tasks>{ task->
        task_view_task_name.text = task.listName.toUpperCase()

        if (task.timeReminders.isEmpty()) { //no time
            taskViewTimeText!!.visibility = View.GONE
            taskViewTimeIcon!!.visibility = View.GONE
        } else {
            taskViewTimeText!!.text = task.nextReminderTime().toString("hh:mm a | EEEE, MMMM d")
            taskViewTimeIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_event_black_24dp))
        }

        subTasks.clearWith(task.listItems.mapIndexed { index, s ->  Pair(s, task.listItemsChecked[index] ) })
        subTags.clearWith(task.listTags)
        mAdapter.notifyDataSetChanged()
    }

    fun collapseAndChangeAppBar(toolbar: Toolbar, fab: FloatingActionButton) {
        oldNavIcon = toolbar.navigationIcon!!.mutate()

        val newIcon = resources.getDrawable(R.drawable.ic_clear_black_24dp)
        newIcon.setColorFilter(Utilities.textFromBackground(Cyanea.instance.primary), PorterDuff.Mode.SRC_ATOP)
        toolbar.navigationIcon = newIcon

        TransitionManager.beginDelayedTransition(toolbar.rootView as ViewGroup, ChangeBounds())

        (activity as MainActivity).drawer.bottomSheetBehavior.setBottomSheetCallback((activity as MainActivity).drawer.collapsedSheetCallback)

        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_done_all_black_24dp).mutate())

        toolbar.menu.setGroupVisible(R.id.toolbar_task_view, true)
        toolbar.menu.setGroupVisible(R.id.toolbar_main, false)
    }

    val subTasks = ArrayList<Pair<String, Boolean>>()
    fun subtaskAdapter() = Klaster.get()
        .itemCount { subTasks.size }
        .view(R.layout.inbox_checklist_list_item, layoutInflater)
        .bind { pos ->
            itemView.listTextView.apply {
                backgroundTintList = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
                text = subTasks[pos].first
                isChecked = subTasks[pos].second
                paintFlags = if (!isChecked) paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.viewModelTask.value?.editListItemsChecked(isChecked, pos)
                    paintFlags = if (!isChecked) paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

        }
        .build()

    val subTags = ArrayList<Int>()
    fun tagAdapter() = Klaster.get()
        .itemCount { subTags.size }
        .view(R.layout.task_view_tag_list_item, layoutInflater)
        .bind { pos ->
            val tag = Current.tagListAll()[subTags[pos]]
            itemView.tagImage.setColorFilter(tag.tagColor)
            itemView.task_view_item_tag_name.text = tag.tagName
        }
        .build()


    fun prepareRecyclerViews() {
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



            mAdapter.notifyItemMoved(fromPosition, toPosition)
            ProjectsUtils.update(viewModel.viewModelTask.value!!)
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
            return ItemTouchHelper.Callback.makeFlag(
                ItemTouchHelper.ACTION_STATE_DRAG,
                ItemTouchHelper.DOWN or ItemTouchHelper.UP
            )
        }
    }


    fun taskDone() {
        viewModel.viewModelTask.value?.isArchived = true
        (activity as MainActivity).inboxFragment.mRecyclerView.collapse()
    }

    fun editFromToolbar() {
        (activity as MainActivity).inboxFragment.mRecyclerView.collapse()
        viewModel.viewModelTask.value?.isEditing = true

    }

    companion object {
        lateinit var oldNavIcon: Drawable
        var anyExpanded: Boolean = false
        private lateinit var editFromToolbarFun: () -> Unit
        private lateinit var taskDoneFun: () -> Unit
        fun editFromToolbarStat() {
            editFromToolbarFun()
        }

        fun taskDoneStat() {
            taskDoneFun()
        }
    }

    class TaskViewPageCallbacks(val activity: Activity) : SimplePageStateChangeCallbacks() {


        override fun onPageAboutToExpand(expandAnimDuration: Long) {
            super.onPageAboutToExpand(expandAnimDuration)
            anyExpanded = true
        }

        override fun onPageExpanded() {
            super.onPageExpanded()
            anyExpanded = true
        }

        override fun onPageCollapsed() {
            super.onPageCollapsed()
            anyExpanded = false

            val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
            val fab: FloatingActionButton = activity.findViewById(R.id.fab)
            (activity as MainActivity).drawer.bottomSheetBehavior.setBottomSheetCallback(activity.drawer.normalSheetCallback)

            android.transition.TransitionManager.beginDelayedTransition(toolbar.getRootView() as ViewGroup, android.transition.ChangeBounds())
            toolbar.navigationIcon = oldNavIcon

            fab.setImageDrawable(activity.getDrawable(R.drawable.ic_add_black_24dp)?.mutate())
            toolbar.menu.setGroupVisible(R.id.toolbar_task_view, false)
            toolbar.menu.setGroupVisible(R.id.toolbar_main, true)
        }

        override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
            super.onPageAboutToCollapse(collapseAnimDuration)
            anyExpanded = false
        }

    }


}
