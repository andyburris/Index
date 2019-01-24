package com.andb.apps.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.eventbus.AddTaskAddTagEvent
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.CyaneaDialog
import com.github.rongi.klaster.Klaster
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.add_task.*
import kotlinx.android.synthetic.main.add_task_list_item.view.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList


class AddTask : CyaneaAppCompatActivity() {

    var task = Tasks("", ArrayList<String>(), ArrayList<Boolean>(), ArrayList<Int>(), DateTime(3000, 1, 1, 0, 0, 30), false)

    lateinit var subtaskAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    lateinit var tagAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    var editing = false
    var editPos = -1
    var browse = false

    var dateSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        theme()
        setupEdit()
        setupName()
        setupRecyclers()
        setupButtons()
    }

    fun theme() {
        addTaskEditFrame.setBackgroundColor(Utilities.lighterDarker(cyanea.backgroundColor, .95f))
        addTaskName.apply {
            setHintTextColor(Utilities.lighterDarker(cyanea.backgroundColor, .7f))
            setTextColor(Utilities.lighterDarker(cyanea.backgroundColor, .5f))
            setCursorColor(this, Utilities.lighterDarker(cyanea.backgroundColor, .6f))
        }
        addTaskResetReminders.apply {
            visibility = View.GONE
        }
        checkDividers()
    }

    fun setupEdit() {
        editing = intent.getBooleanExtra("edit", false)
        editPos = intent.getIntExtra("editPos", -1)
        browse = intent.getBooleanExtra("browse", false)
        if (editing) {
            task = if (browse) FilteredLists.browseTaskList[editPos] else FilteredLists.inboxTaskList[editPos]
        }
    }

    fun setupName() {
        addTaskName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                task.listName = s.toString()
            }
        })
    }

    fun setupRecyclers() {
        val tagRecyclerView = addTaskTagsRV
        tagRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tagAdapter = tagAdapter()
        tagRecyclerView!!.adapter = tagAdapter

        val taskRecycler = addTaskSublistRV
        taskRecycler.layoutManager = LinearLayoutManager(this)
        subtaskAdapter = subtaskAdapter()
        taskRecycler!!.adapter = subtaskAdapter
        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(taskRecycler)
    }

    fun setupButtons() {
        addTaskAddListIcon.setOnClickListener {
            addSubtask()
            checkDividers()
        }
        addTaskAddTagsIcon.setOnClickListener {
            addTag()
            checkDividers()
        }
        addTaskAddDateIcon.setOnClickListener {
            pickDate()
        }
        addTaskAddTimeIcon.setOnClickListener {
            pickTime()
        }
        addTaskAddLocationIcon.setOnClickListener {
            Snackbar.make(addTaskAddLocationIcon.rootView, "Location reminders not available yet", Snackbar.LENGTH_LONG).also { it.animationMode = Snackbar.ANIMATION_MODE_SLIDE }.show()
        }
        addTaskResetReminders.setOnClickListener {
            task.dateTime = DateTime(3000, 1, 1, 0, 0, 59)
            addTaskTimeText.text = ""
            addTaskResetReminders.visibility = View.GONE
        }
        addTaskConfirm.setOnClickListener {
            save()
        }
    }

    fun checkDividers() {
        addTaskDivider1.visibility = if (task.isListItems) {
            View.VISIBLE
        } else {
            View.GONE
        }
        addTaskDivider2.visibility = if (task.isListTags) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun addSubtask() {
        task.listItems.add("")
        task.listItemsChecked.add(false)
        subtaskAdapter.notifyItemInserted(task.listItemsSize - 1)
    }

    fun addTag() {
        val selectTag = Intent(this, TagSelect::class.java)
        selectTag.putExtra("isTaskCreate", true)
        startActivity(selectTag)
    }

    fun pickDate() {
        val dialog = DatePickerDialog(this@AddTask, DatePickerDialog.OnDateSetListener { datePicker, i, i1, i2 ->
            dateSet = true
            task.dateTime = task.dateTime!!.withDate(i, i1 + 1, i2)
            if (task.dateTime.secondOfMinute != 0) {
                task.dateTime = task.dateTime!!.withTime(23, 59, 59, 0)
                addTaskTimeText.text = task.dateTime!!.toString("MMM d")
            } else {
                addTaskTimeText.text = task.dateTime!!.toString("MMM d, h:mm a")
            }

            addTaskResetReminders.visibility = View.VISIBLE

        }, DateTime.now().year, DateTime.now().monthOfYear - 1, DateTime.now().dayOfMonth)
        dialog.show()
        CyaneaDialog.setButtonStyle(dialog, DatePickerDialog.BUTTON_NEGATIVE, DatePickerDialog.BUTTON_POSITIVE)
    }

    fun pickTime() {
        val timePickerDialog = TimePickerDialog(this@AddTask, TimePickerDialog.OnTimeSetListener { timePicker, i, i1 ->
            val localTime = LocalTime().withHourOfDay(i).withMinuteOfHour(i1).withSecondOfMinute(0)
            if (!dateSet) {
                task.dateTime = DateTime(DateTime.now().year, DateTime.now().monthOfYear, DateTime.now().dayOfMonth, 23, 0)
                task.dateTime = task.dateTime!!.withTime(localTime)
                addTaskTimeText.text = task.dateTime!!.toString("h:mm a")
            } else {
                task.dateTime = task.dateTime!!.withTime(localTime)
                addTaskTimeText.text = task.dateTime!!.toString("MMM d, h:mm a")
            }
            addTaskResetReminders.visibility = View.VISIBLE
        }, DateTime.now().hourOfDay, DateTime.now().minuteOfHour, false)
        timePickerDialog.show()
        CyaneaDialog.setButtonStyle(timePickerDialog, TimePickerDialog.BUTTON_NEGATIVE, TimePickerDialog.BUTTON_POSITIVE)
    }

    fun validate(): Boolean {
        if (task.listName.isEmpty()) {
            Snackbar.make(addTaskEditFrame.rootView, "Please provide a name for the task", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            return false
        }

        val tempList: ArrayList<String> = ArrayList(task.listItems)
        for ((i, l) in tempList.withIndex()) {
            if (l.isEmpty()) {
                task.listItems.removeAt(i)
                task.listItemsChecked.removeAt(i)
            }
        }

        return true
    }

    fun save() {

        if(validate()) {

            if (editing) {
                val taskToReplace = if (browse) FilteredLists.browseTaskList[editPos] else FilteredLists.inboxTaskList[editPos]
                Current.taskList()[Current.taskList().indexOf(taskToReplace)]
                ProjectsUtils.update(task)
                EventBus.getDefault().post(UpdateEvent(!browse))
            } else {

                Current.taskList().add(task)
                AsyncTask.execute {
                    MainActivity.projectsDatabase.tasksDao().insertOnlySingleTask(task);
                }
                EventBus.getDefault().post(UpdateEvent())
            }

            finish()
        }
    }


    private fun tagAdapter() = Klaster.get()
            .itemCount { task.listTagsSize }
            .view(R.layout.task_view_tag_list_item, layoutInflater)
            .bind { _ ->
                val tag = Current.tagList()[task.listTags[adapterPosition]]
                itemView.apply {
                    tagImage.setColorFilter(tag.tagColor)
                    task_view_item_tag_name.text = tag.tagName
                    setOnClickListener {
                        task.listTags.removeAt(adapterPosition)
                        tagAdapter.notifyItemRemoved(adapterPosition)
                        checkDividers()
                    }
                }

            }
            .build()

    private fun subtaskAdapter() = Klaster.get()
            .itemCount { task.listItemsSize }
            .view(R.layout.add_task_list_item, layoutInflater)
            .bind { _ ->
                itemView.apply {
                    taskItemEditText.apply {
                        setText(task.listItems[adapterPosition])
                        addTextChangedListener(object : TextWatcher {
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                task.listItems[adapterPosition] = s.toString()
                            }

                            override fun afterTextChanged(s: Editable?) {}
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        })
                        backgroundTintList = ColorStateList.valueOf(Utilities.lighterDarker(cyanea.backgroundColor, .6f))
                        setTextColor(Utilities.lighterDarker(cyanea.backgroundColor, .7f))
                        setCursorColor(this, Utilities.lighterDarker(cyanea.backgroundColor, .6f))
                    }
                    removeListItem.setOnClickListener {
                        task.listItems.removeAt(adapterPosition)
                        task.listItemsChecked.removeAt(adapterPosition)
                        subtaskAdapter.notifyItemRemoved(adapterPosition)
                        checkDividers()
                    }
                    if (adapterPosition == task.listItemsSize - 1) {
                        (layoutParams as RecyclerView.LayoutParams).bottomMargin = Utilities.pxFromDp(8)
                    } else {
                        (layoutParams as RecyclerView.LayoutParams).bottomMargin = Utilities.pxFromDp(0)
                    }
                }
            }
            .build()

    fun setCursorColor(editText: EditText, color: Int) {
        try {
            // Get the cursor resource id
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(editText)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(editText.context, drawableResId)
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf(drawable, drawable)


            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field.get(editText)
            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)

        } catch (e: Exception) {
            Log.e("cursorSetFailed", "-> ", e)
        }
    }

    private var _ithCallback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
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
            subtaskAdapter.notifyItemMoved(fromPosition, toPosition)

            return false
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return ItemTouchHelper.Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddTaskAddTagEvent(event: AddTaskAddTagEvent) {
        if (!task.listTags.contains(event.tag)) {
            task.listTags.add(event.tag)
            tagAdapter.notifyItemInserted(task.listTagsSize - 1)
            checkDividers()
        } else {
            Snackbar.make(addTaskEditFrame.rootView, "The tag you selected has already been added to this task", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }
    }
}

/*    lateinit var editingTask: Tasks
    var editing: Boolean = false

    var itemsList = ArrayList<String>(listOf("", ""))
    var tagsList = ArrayList<Int>()
    var taskDateTime: DateTime? = null

    internal var notified: Boolean = false
    internal var timeHasBeenSet: Boolean = false

    lateinit var taskRecycler: RecyclerView
    lateinit var subtaskAdapter: AddTaskAdapter
    lateinit var tagAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_task)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        EventBus.getDefault().register(this)

        addTaskResetReminders.visibility = View.GONE
        addTaskResetReminders.setOnClickListener {
            taskDateTime = DateTime(3000, 1, 1, 0, 0, 59)
            addTaskResetReminders.visibility = View.GONE
            dateTimeText.text = resources.getText(R.string.add_time_text)
            timeHasBeenSet = false
        }

        val bundle = intent.extras
        if (bundle!!.containsKey("edit"))
            editing = bundle.getBoolean("edit")

        if (editing) {

            prepareForEditing(bundle)


        } else {
            taskDateTime = DateTime(3000, 1, 1, 0, 0, 59)
            if (!Filters.getCurrentFilter().isEmpty()) {
                tagsList = ArrayList(Filters.getCurrentFilter())
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
        if (taskDateTime != DateTime(3000, 1, 1, 0, 0, 59)) {
            dateTimeText.text = taskDateTime!!.toString("MMM d, h:mm a")
            addTaskResetReminders.visibility = View.VISIBLE
        }

        if (editingTask.isListItems) {
            itemsList = ArrayList(editingTask.allListItems)
        }

        if (editingTask.isListTags) {
            tagsList = ArrayList(editingTask.listTags)
        }


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
            subtaskAdapter!!.focused = true
            subtaskAdapter!!.notifyItemInserted(itemsList.size - 1)
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

                addTaskResetReminders.visibility = View.VISIBLE
            }, DateTime.now().year, DateTime.now().monthOfYear - 1, DateTime.now().dayOfMonth)
            dialog.show()
            CyaneaDialog.setButtonStyle(dialog, DatePickerDialog.BUTTON_NEGATIVE, DatePickerDialog.BUTTON_POSITIVE)

        }

        timeButton.setOnClickListener {


        }

        dateTimeText.setOnClickListener {
            taskDateTime = DateTime(3000, 1, 1, 0, 0, 59)
            dateTimeText.text = "Add time"
        }

    }


    private fun fabAddList() {
        fab.setOnClickListener {
            if (checkFull()) {
                saveTask()
                finish()
            } else {
                Snackbar.make(findViewById(R.id.add_task_scroll_view), "Please fill in or remove any blank fields", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            }
        }
    }


    private fun saveTask() {
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
            val task = Tasks(taskName.text.toString(), items, checked, tags, taskDateTime, notified, editingTask.listKey, editingTask.projectId, false)
            Current.taskList().apply {
                set(indexOf(editingTask), task)
            }
            ProjectsUtils.update(task)
            EventBus.getDefault().post(UpdateEvent(true))
        } else {
            val task = Tasks(taskName.text.toString(), items, checked, tags, taskDateTime, false)
            Current.taskList().add(task)
            AsyncTask.execute{
                MainActivity.projectsDatabase.tasksDao().insertOnlySingleTask(task)
            }
            EventBus.getDefault().post(UpdateEvent(true))
        }

    }


    private fun checkFull(): Boolean {
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
    private var _ithCallback: ItemTouchHelper.Callback = object : ItemTouchHelper.Callback() {
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
            subtaskAdapter.notifyItemMoved(fromPosition, toPosition)
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
    }*/




