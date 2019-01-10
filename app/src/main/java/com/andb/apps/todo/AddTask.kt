package com.andb.apps.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.eventbus.AddTaskAddTagEvent
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.github.rongi.klaster.Klaster
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.content_add_task.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList


class AddTask : CyaneaAppCompatActivity() {


    lateinit var editingTask: Tasks
    var editing: Boolean = false

    var itemsList = ArrayList<String>(listOf("", ""))
    var tagsList = ArrayList<Int>()
    var taskDateTime: DateTime? = null

    internal var notified: Boolean = false
    internal var timeHasBeenSet: Boolean = false

    lateinit var taskRecycler: RecyclerView
    lateinit var mAdapter: AddTaskAdapter
    lateinit var tagAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_task)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        EventBus.getDefault().register(this)

        resetTimeButton.visibility = View.GONE
        resetTimeButton.setOnClickListener {
            taskDateTime = DateTime(3000, 1, 1, 0, 0)
            resetTimeButton.visibility = View.GONE
            dateTimeText.text = resources.getText(R.string.add_time_text)
            timeHasBeenSet = false
        }

        val bundle = intent.extras
        if (bundle!!.containsKey("edit"))
            editing = bundle.getBoolean("edit")

        if (editing) {

            prepareForEditing(bundle)


        } else {
            taskDateTime = DateTime(3000, 1, 1, 0, 0)
            if (!Filters.getCurrentFilter().isEmpty()) {
                tagsList = Filters.getCurrentFilter()
            }

        }
        prepareItemsRecyclerView()
        prepareTagsRecyclerView()

        switchList(editing)

        checkAddListItem()

        checkAddTagItem()

        checkAddTime()

        fabAddList()


    }


    fun prepareForEditing(bundle: Bundle) {


        val browse = bundle.getBoolean("browse")
        val editPos = bundle.getInt("editPos")

        if (browse) {
            editingTask = FilteredLists.browseTaskList.get(editPos)
        } else {
            editingTask = FilteredLists.inboxTaskList.get(editPos)
        }

        notified = editingTask.isNotified
        itemsList = editingTask.listItems


        taskName.setText(editingTask.listName)
        taskDateTime = editingTask.dateTime
        if (taskDateTime != DateTime(3000, 1, 1, 0, 0)) {
            dateTimeText.text = taskDateTime!!.toString("MMM d, h:mm a")
            resetTimeButton.visibility = View.VISIBLE
        }

        if (editingTask.isListItems) {
            itemsList = ArrayList(editingTask.allListItems)
        }

        if (editingTask.isListTags) {
            tagsList = editingTask.listTags
        }


    }

    private fun tagAdapter() = Klaster.get()
            .itemCount { tagsList.size }
            .view(R.layout.task_view_tag_list_item, layoutInflater)
            .bind { position ->
                val tag = Current.tagList().get(tagsList[adapterPosition])
                itemView.tagImage.setColorFilter(tag.tagColor)
                itemView.task_view_item_tag_name.text = tag.tagName
                itemView.setOnClickListener {
                    tagsList.removeAt(adapterPosition)
                    tagAdapter.notifyItemRemoved(adapterPosition)
                }
            }

            .build()


    fun prepareItemsRecyclerView() {
        taskRecycler = itemRecyclerView

        // use a linear layout manager
        taskRecycler.layoutManager = LinearLayoutManager(this)

        // specify an adapter (see also next example)
        mAdapter = AddTaskAdapter(itemsList)
        taskRecycler!!.adapter = mAdapter

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(taskRecycler)


    }

    fun prepareTagsRecyclerView() {


        val tagRecyclerView = tagRecyclerView

        // use a linear layout manager
        tagRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // specify an adapter (see also next example)
        tagAdapter = tagAdapter()
        tagRecyclerView!!.adapter = tagAdapter
    }


    fun switchList(editing: Boolean) {

        if (editing) {
            if (editingTask.isListItems) {
                switch_task.isChecked = true
                itemRecyclerView.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE
            }
        }

        switch_task.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                TransitionManager.beginDelayedTransition(add_task_card)
                itemRecyclerView.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE

            } else {
                TransitionManager.beginDelayedTransition(add_task_card)
                itemRecyclerView.visibility = View.GONE
                addButton.visibility = View.GONE
            }
        }
    }


    fun checkAddListItem() {

        addButton.setOnClickListener {
            itemsList.add("")
            mAdapter!!.focused = true
            mAdapter!!.notifyItemInserted(itemsList.size - 1)
            taskRecycler!!.smoothScrollToPosition(itemsList.size - 1)
        }
    }

    fun checkAddTagItem() {
        tagAddButton.setOnClickListener {
            val selectTag = Intent(this@AddTask, TagSelect::class.java)
            selectTag.putExtra("isTaskCreate", true)
            startActivity(selectTag)
        }
    }

    fun checkAddTime() {


        dateButton.setOnClickListener {
            val dialog = DatePickerDialog(this@AddTask, DatePickerDialog.OnDateSetListener { datePicker, i, i1, i2 ->
                val dateTimeText = findViewById<View>(R.id.dateTimeText) as TextView

                taskDateTime = taskDateTime!!.withDate(i, i1 + 1, i2)
                if (!timeHasBeenSet) {
                    taskDateTime = taskDateTime!!.withTime(23, 59, 59, 0)
                    dateTimeText.text = taskDateTime!!.toString("MMM d")

                } else {
                    dateTimeText.text = taskDateTime!!.toString("MMM d, h:mm a")
                }
                Log.d("dateTime", taskDateTime!!.toString())

                if (editing) {
                    if (taskDateTime!!.isAfter(DateTime.now()))
                        notified = false
                    else if (taskDateTime!!.isBefore(editingTask.dateTime))
                        notified = editingTask.isNotified
                }

                resetTimeButton.visibility = View.VISIBLE
            }, DateTime.now().year, DateTime.now().monthOfYear - 1, DateTime.now().dayOfMonth)
            dialog.show()
        }

        timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(this@AddTask, TimePickerDialog.OnTimeSetListener { timePicker, i, i1 ->
                timeHasBeenSet = true
                var localTime = LocalTime().withHourOfDay(i).withMinuteOfHour(i1)
                localTime = localTime.secondOfMinute().setCopy(0)
                if (taskDateTime!!.isEqual(DateTime(3000, 1, 1, 0, 0))) {
                    taskDateTime = DateTime(DateTime.now().year, DateTime.now().monthOfYear, DateTime.now().dayOfMonth, 23, 0)
                    Log.d("dateTime", taskDateTime!!.toString())
                    taskDateTime = taskDateTime!!.withTime(localTime)
                    dateTimeText.text = taskDateTime!!.toString("h:mm a")


                } else {
                    taskDateTime = taskDateTime!!.withTime(localTime)
                    dateTimeText.text = taskDateTime!!.toString("MMM d, h:mm a")

                }

                if (editing) {
                    if (taskDateTime!!.isAfter(DateTime.now()))
                        notified = false
                    else if (taskDateTime!!.isBefore(editingTask.dateTime))
                        notified = editingTask.isNotified
                }

                Log.d("dateTime", taskDateTime!!.toString("h:mm:ss"))

                resetTimeButton.visibility = View.VISIBLE
            }, DateTime.now().hourOfDay, DateTime.now().minuteOfHour, false)
            timePickerDialog.show()

            /*                TimePickerFragmentDialog.newInstance(DateTimeBuilder.newInstance()
                        .withMinDate(timeMin))
                        .show(getSupportFragmentManager(), "TimePickerFragmentDialog");*/
        }

        dateTimeText.setOnClickListener {
            taskDateTime = DateTime(3000, 1, 1, 0, 0)
            dateTimeText.text = "Add time"
        }

    }


    fun fabAddList() {
        fab.setOnClickListener {
            if (checkFull()) {
                saveTask()
                finish()
            } else {
                Snackbar.make(findViewById(R.id.add_task_scroll_view), "Please fill in or remove any blank fields", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            }
        }
    }


    fun saveTask() {
        val taskName = findViewById<View>(R.id.taskName) as EditText
        var items = ArrayList<String>()
        val checked = ArrayList<Boolean>()
        val tags = tagsList
        if (switch_task.isChecked) {
            Log.d("Adding Items", "Adding Items")
            items = ArrayList(itemsList)
        } else {
            //AddTaskAdapter.taskList.clear();
        }


        if (editing) {
            Current.taskList().apply {
                set(indexOf(editingTask), Tasks(taskName.text.toString(), items, checked, tags, taskDateTime, notified, editingTask.listKey))
            }
            ProjectsUtils.update()
            EventBus.getDefault().post(UpdateEvent(true))
        } else {
            Current.taskList().add(Tasks(taskName.text.toString(), items, checked, tags, taskDateTime, false))
            ProjectsUtils.update()
            EventBus.getDefault().post(UpdateEvent(true))
        }

    }


    fun checkFull(): Boolean {
        Log.d("taskName", "gets here")

        var full = true
        val taskName = findViewById<View>(R.id.taskName) as EditText
        val switch_task = findViewById<View>(R.id.switch_task) as Switch
        if (TextUtils.isEmpty(taskName.text)) {
            full = false
            Log.d("taskName", "full = false")
        } else if (switch_task.isChecked) {


            for (i in itemsList.indices) {
                if (itemsList[i].isEmpty()) {
                    full = false
                    break
                } else {
                    full = true
                }
            }
        }

        return full
    }


    // Extend the Callback class
    internal var _ithCallback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
        //and in your implementation of
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Log.d("dragDropStart", itemsList[fromPosition])
                    Log.d("dragDropStart", itemsList[toPosition])
                    Collections.swap(itemsList, i, i + 1)
                    Log.d("dragDropEnd", itemsList[fromPosition])
                    Log.d("dragDropEnd", itemsList[toPosition])
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Log.d("dragDropStart", itemsList[fromPosition])
                    Log.d("dragDropStart", itemsList[toPosition])
                    Collections.swap(itemsList, i, i - 1)
                    Log.d("dragDropEnd", itemsList[fromPosition])
                    Log.d("dragDropEnd", itemsList[toPosition])
                }
            }
            Log.d("dragDropStart", itemsList[fromPosition])
            Log.d("dragDropStart", itemsList[toPosition])
            mAdapter!!.notifyItemMoved(fromPosition, toPosition)
            Log.d("dragDropEnd", itemsList[fromPosition])
            Log.d("dragDropEnd", itemsList[toPosition])

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


    fun addTag(tagPosition: Int) { //return from tagSelect
        if (tagsList.contains(tagPosition)) {
            Snackbar.make(findViewById(R.id.add_task_scroll_view), "The tag you selected has already been added to this task", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        } else {
            tagsList.add(tagPosition)
            Log.d("addingValue", "adding #$tagPosition")
            tagAdapter.notifyDataSetChanged()
        }
    }

    public override fun onPause() {
        super.onPause()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddTaskAddTagEvent(event: AddTaskAddTagEvent) {
        addTag(event.tag)
    }


}

