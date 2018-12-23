package com.andb.apps.todo

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.lists.ArchiveTaskList
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.views.CyaneaTextView
import com.andb.apps.todo.views.Icon
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.CyaneaFragment

import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.lists.TagList
import com.andb.apps.todo.lists.TaskList
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.content_task_view.*
import kotlinx.android.synthetic.main.inbox_checklist_list_item.*
import kotlinx.android.synthetic.main.inbox_checklist_list_item.view.*
import kotlinx.android.synthetic.main.task_list_item.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*

class TaskView : CyaneaFragment() {

    internal var position: Int = 0
    internal var inboxBrowseArchive: Int = 0 //0 is inbox, 1 is browse, 2 is archive
    internal var viewPos = 0
    lateinit var task: Tasks




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments

        position = bundle!!.getInt("pos")
        inboxBrowseArchive = bundle.getInt("inboxBrowseArchive")

        when (inboxBrowseArchive) {
            TaskAdapter.FROM_BROWSE -> task = FilteredLists.browseTaskList[position]
            TaskAdapter.FROM_ARCHIVE -> task = ArchiveTaskList.taskList[position]
            else //inbox
            -> task = FilteredLists.inboxTaskList[position]
        }


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_task_view, container!!.parent as ViewGroup, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bg = view.findViewById<CoordinatorLayout>(R.id.task_view_parent)
        bg.setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
        //collapseAndChangeAppBar(activity!!.findViewById(R.id.toolbar), activity!!.findViewById(R.id.fab), activity!!.findViewById(R.id.tabs))


        Log.d("onePosUpError", task.listName)


        task_view_task_name.text = task.listName.toUpperCase()

        if (!task.isListTime) { //no time
            taskViewTimeText!!.visibility = View.GONE
            taskViewTimeIcon!!.visibility = View.GONE
        } else if (task.dateTime.secondOfMinute == 59) { //date only
            taskViewTimeText!!.text = task.dateTime.toString("EEEE, MMMM d")
            taskViewTimeIcon!!.setImageDrawable(resources.getDrawable(R.drawable.ic_event_black_24dp))
        } else {
            taskViewTimeText!!.text = task.dateTime.toString("hh:mm | EEEE, MMMM d")
        }

        prepareRecyclerViews(task)


    }

/*    fun collapseAndChangeAppBar(toolbar: Toolbar, fab: FloatingActionButton, tabLayout: TabLayout) {
        oldNavIcon = toolbar.navigationIcon!!.mutate()
        oldMargin = (toolbar.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp)

        //TransitionManager.beginDelayedTransition(tabLayout, new ChangeBounds());
        var layoutParams = tabLayout.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.height = 1
        tabLayout.layoutParams = layoutParams

        //TransitionManager.beginDelayedTransition((ViewGroup) toolbar.getRootView(), new ChangeBounds());
        layoutParams = toolbar.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin = 0
        toolbar.layoutParams = layoutParams

        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_done_all_black_24dp).mutate())


    }*/

    fun subtaskAdapter()  = Klaster.get()
            .itemCount { task.listItemsSize }
            .view(R.layout.inbox_checklist_list_item, layoutInflater)
            .bind { pos ->
                itemView.listTextView.text = task.getListItems(pos)
                itemView.listTextView.isChecked = task.getListItemsChecked(pos)
                itemView.listTextView.setOnCheckedChangeListener { buttonView, isChecked ->
                    task.editListItemsChecked(isChecked, pos)
                    AsyncTask.execute {
                        MainActivity.tasksDatabase.tasksDao().updateTask(task)
                    }
                }
            }
            .build()

    fun tagAdapter() = Klaster.get()
            .itemCount { task.listTagsSize }
            .view(R.layout.task_view_tag_list_item, layoutInflater)
            .bind {pos ->
                val tag = TagList.getItem(task.getListTags(pos))
                itemView.tagImage.setColorFilter(tag.tagColor)
                itemView.task_view_item_tag_name.text = tag.tagName
            }
            .build()


    fun prepareRecyclerViews(task: Tasks) {
        val mRecyclerView = taskViewRecycler
        // use a linear layout manager
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        // specify an adapter (see also next example)
        val mAdapter = subtaskAdapter()
        mRecyclerView.adapter = mAdapter


        val tRecyclerView = taskViewTagRecycler
        // use a linear layout manager
        tRecyclerView.layoutManager = LinearLayoutManager(context)

        // specify an adapter (see also next example)
        val tAdapter = tagAdapter()
        tRecyclerView.adapter = tAdapter
    }


    override fun onPause() {
        super.onPause()
        InboxFragment.mAdapter.notifyDataSetChanged()
        BrowseFragment.mAdapter.notifyDataSetChanged()
    }

    companion object {

        lateinit var oldNavIcon: Drawable
        var oldMargin: Int = 0
    }


}
