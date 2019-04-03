package com.andb.apps.todo

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.utilities.viewModelFactory
import com.andb.apps.todo.views.STATE_ONE
import com.andb.apps.todo.views.STATE_ZERO
import com.github.rongi.klaster.Klaster
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.activity_task_view.*
import kotlinx.android.synthetic.main.content_task_view.*
import kotlinx.android.synthetic.main.inbox_checklist_list_item.view.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks


class TaskView : Fragment() {

    private val expandablePageLayout by lazy { view!!.parent as ExpandablePageLayout }
    val mAdapter by lazy { subtaskAdapter() }
    lateinit var viewModel: TaskViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments

        val key = bundle!!.getInt("key")

        if(!Current.taskListAll().map { it.listKey }.contains(key)){
            (activity as MainActivity).inboxFragment.mRecyclerView.collapse()//TODO: null if return to activity
            return
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory { TaskViewViewModel(key) })
            .get(TaskViewViewModel::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_task_view, container?.parent as ViewGroup?, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task_view_parent.setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
        collapseAndChangeAppBar(requireActivity().findViewById(R.id.toolbar), requireActivity().findViewById(R.id.fab))

        prepareRecyclerViews()

        task_view_task_name.text = viewModel.task().listName.toUpperCase()

        if (viewModel.task().timeReminders.isEmpty()) { //no time
            taskViewTimeText.visibility = View.GONE
            taskViewTimeIcon.visibility = View.GONE
        } else {
            taskViewTimeText.text = viewModel.task().nextReminderTime()
                .toString("hh:mm a | EEEE, MMMM d")
            taskViewTimeIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_event_black_24dp))
        }

        expandablePageLayout.pullToCollapseInterceptor = { downX, downY, upwardPull ->
            val directionInt = if (upwardPull) +1 else -1
            val canScrollFurther = taskViewScrollLayout.canScrollVertically(directionInt)
            //Log.d("canScrollFurther", "$canScrollFurther")
            if (canScrollFurther) InterceptResult.INTERCEPTED else InterceptResult.IGNORED
        }

    }


    fun collapseAndChangeAppBar(toolbar: Toolbar, fab: FloatingActionButton) {

        val newIcon = resources.getDrawable(R.drawable.anim_collapse_clear)
        newIcon.setColorFilter(Utilities.textFromBackground(Cyanea.instance.primary), PorterDuff.Mode.SRC_ATOP)
        toolbar.navigationIcon = newIcon.also { (it as Animatable).start() }

        (activity as MainActivity).drawer.bottomSheetBehavior.setBottomSheetCallback((activity as MainActivity).drawer.collapsedSheetCallback)

        //fab.setImageDrawable(resources.getDrawable(R.drawable.anim_add_done).mutate())
        fab.setImageState(STATE_ZERO, true)

        toolbar.menu.setGroupVisible(R.id.toolbar_task_view, true)
        toolbar.menu.setGroupVisible(R.id.toolbar_main, false)
    }

    fun subtaskAdapter() = Klaster.get()
        .itemCount { viewModel.task().listItems.size }
        .view(R.layout.inbox_checklist_list_item, layoutInflater)
        .bind { pos ->
            itemView.listTextView.apply {
                backgroundTintList = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
                text = viewModel.task().listItems[pos]
                isChecked = viewModel.task().listItemsChecked[pos]
                paintFlags = if (!isChecked) paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.setChecked(pos, isChecked)
                    paintFlags = if (!isChecked) paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

        }
        .build()

    fun tagAdapter() = Klaster.get()
        .itemCount { viewModel.task().listTags.size }
        .view(R.layout.task_view_tag_list_item, layoutInflater)
        .bind { pos ->
            val tag = Current.tagListAll()[viewModel.task().listTags[pos]]
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

            viewModel.reorder(fromPosition, toPosition)

            mAdapter.notifyItemMoved(fromPosition, toPosition)
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


    companion object {
        var anyExpanded: Boolean = false
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

            val newIcon = activity.resources.getDrawable(R.drawable.anim_collapse_clear_reverse)
            newIcon.setColorFilter(Utilities.textFromBackground(Cyanea.instance.primary), PorterDuff.Mode.SRC_ATOP)
            toolbar.navigationIcon = newIcon.also { (it as Animatable).start() }

            //fab.setImageDrawable(activity.getDrawable(R.drawable.anim_add_done_reverse)?.mutate())
            fab.setImageState(STATE_ONE, true)
            toolbar.menu.setGroupVisible(R.id.toolbar_task_view, false)
            toolbar.menu.setGroupVisible(R.id.toolbar_main, true)
        }

        override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
            super.onPageAboutToCollapse(collapseAnimDuration)
            anyExpanded = false
        }

    }


}
