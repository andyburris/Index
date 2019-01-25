package com.andb.apps.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.eventbus.AddTaskAddTagEvent
import com.andb.apps.todo.filtering.FilteredLists
import com.andb.apps.todo.notifications.NotificationHandler
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.CyaneaDialog
import com.github.rongi.klaster.Klaster
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.add_task.view.*
import kotlinx.android.synthetic.main.add_task_list_item.view.*
import kotlinx.android.synthetic.main.content_add_task.view.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList


class AddTask(ctxt: Context) : FrameLayout(ctxt) {


    lateinit var subtaskAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    lateinit var tagAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    lateinit var task: Tasks
    var editing: Boolean = false
    var viewPos: Int = -1;
    var browse: Boolean = false

    var dateSet = false

    init {
        //LayoutInflater.from(context).inflate(R.layout.add_task, parent, false)
        inflate(context, R.layout.add_task, this)
        EventBus.getDefault().register(this)
    }

    @JvmOverloads
    fun setup(browse: Boolean, viewPos: Int, task: Tasks = Tasks("", ArrayList<String>(), ArrayList<Boolean>(), ArrayList<Int>(), DateTime(3000, 1, 1, 0, 0, 30), false), editing: Boolean = false) {
        this.task = task
        this.editing = editing
        this.viewPos = viewPos
        this.browse = browse

        sortView()
        theme()
        setupName()
        setupTime()
        setupRecyclers()
        setupButtons()
    }


    fun theme() {
        addTaskEditFrame.setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, .95f))
        addTaskName.apply {
            setHintTextColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, .7f))
            setTextColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, .5f))
            setCursorColor(this, Utilities.lighterDarker(Cyanea.instance.backgroundColor, .6f))
        }
        addTaskResetReminders.apply {
            visibility = View.GONE
        }
        checkDividers()
    }


    fun setupName() {
        addTaskName.apply {
            setText(task.listName)
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    task.listName = s.toString()
                }
            })
        }
    }

    fun setupTime(){
        if(task.hasDate()){
            dateSet = true
            if(task.hasTime()){
                addTaskTimeText.text = task.dateTime!!.toString("MMM d, h:mm a")
            }else{
                addTaskTimeText.text = task.dateTime!!.toString("MMM d")
            }
            resetTimeButton.visibility = View.VISIBLE
            sortView()
        }else{
            addTaskTimeText.text = ""
        }
    }

    fun setupRecyclers() {
        val tagRecyclerView = addTaskTagsRV
        tagRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        tagAdapter = tagAdapter()
        tagRecyclerView!!.adapter = tagAdapter

        val taskRecycler = addTaskSublistRV
        taskRecycler.layoutManager = LinearLayoutManager(context)
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
        val selectTag = Intent(context, TagSelect::class.java)
        selectTag.putExtra("isTaskCreate", true)
        context.startActivity(selectTag)
    }

    fun pickDate() {
        val dialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { datePicker, i, i1, i2 ->
            dateSet = true
            task.dateTime = task.dateTime!!.withDate(i, i1 + 1, i2)
            if (task.dateTime.secondOfMinute != 0) {
                task.dateTime = task.dateTime!!.withTime(23, 59, 59, 0)
                addTaskTimeText.text = task.dateTime!!.toString("MMM d")
            } else {
                addTaskTimeText.text = task.dateTime!!.toString("MMM d, h:mm a")
            }

            addTaskResetReminders.visibility = View.VISIBLE
            sortView()

        }, DateTime.now().year, DateTime.now().monthOfYear - 1, DateTime.now().dayOfMonth)
        dialog.show()
        CyaneaDialog.setButtonStyle(dialog, DatePickerDialog.BUTTON_NEGATIVE, DatePickerDialog.BUTTON_POSITIVE)
    }

    fun pickTime() {
        val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { timePicker, i, i1 ->
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
            sortView()
        }, DateTime.now().hourOfDay, DateTime.now().minuteOfHour, false)
        timePickerDialog.show()
        CyaneaDialog.setButtonStyle(timePickerDialog, TimePickerDialog.BUTTON_NEGATIVE, TimePickerDialog.BUTTON_POSITIVE)
    }

    fun sortView(){
        if(browse){

        }else{
            //InboxFragment.setFilterMode()
        }
    }

    fun validate(): Boolean {
        if (task.listName.isEmpty()) {
            Snackbar.make(addTaskEditFrame.rootView, "Please provide a name for the task", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            return false
        }
        when (task.listName) {
            "OVERDUE", "TODAY", "WEEK", "MONTH", "FUTURE", "ADD_TASK_PLACEHOLDER" -> {
                Snackbar.make(addTaskEditFrame.rootView, "Sorry, those names are reserved for the app's use. Please choose a different wording or capitalization", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                return false
            }
        }

        val tempList: ArrayList<Int> = ArrayList()
        for ((i, l) in task.listItems.withIndex()) {
            if (l.isEmpty()) {
                tempList.add(i)
            }
        }
        for (i in tempList.size - 1 downTo 0) {
            task.listItems.removeAt(tempList[i])
        }

        return true
    }

    fun save() {

        if (validate()) {

            if (editing) {
                finishEdit()
                ProjectsUtils.update(task)
                NotificationHandler.resetNotifications(context)
            } else {

                Current.taskList().add(task)
                finishAdd()
                AsyncTask.execute {
                    MainActivity.projectsDatabase.tasksDao().insertOnlySingleTask(task)
                }
                NotificationHandler.resetNotifications(context)

            }
        }
    }

    fun finishAdd() {
        if (browse) {
            FilteredLists.browseTaskList.apply {
                removeAt(viewPos)
                add(viewPos, task)
            }
            BrowseFragment.mAdapter.notifyItemChanged(viewPos)
            InboxFragment.mAdapter.notifyDataSetChanged()
        } else {
            FilteredLists.inboxTaskList.apply {
                removeAt(viewPos)
                add(viewPos, task)
            }
            InboxFragment.mAdapter.notifyItemChanged(viewPos)
            BrowseFragment.mAdapter.notifyDataSetChanged()
        }
    }

    fun finishEdit() {
        val taskToReplace: Tasks
        task.isEditing = false
        if (browse) {
            taskToReplace = FilteredLists.browseTaskList[viewPos]
            FilteredLists.browseTaskList[viewPos] = task
            BrowseFragment.mAdapter.notifyItemChanged(viewPos)
        } else {
            taskToReplace = FilteredLists.inboxTaskList[viewPos]
            FilteredLists.inboxTaskList[viewPos] = task
            InboxFragment.mAdapter.notifyItemChanged(viewPos)
        }
        Log.d("replaceTask", taskToReplace.toString())
        //Current.taskList()[Current.taskList().indexOf(taskToReplace)] = task
    }


    private fun tagAdapter() = Klaster.get()
            .itemCount { task.listTagsSize }
            .view(R.layout.task_view_tag_list_item, LayoutInflater.from(context))
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
            .view(R.layout.add_task_list_item, LayoutInflater.from(context))
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
                        backgroundTintList = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, .6f))
                        setTextColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, .7f))
                        setCursorColor(this, Utilities.lighterDarker(Cyanea.instance.backgroundColor, .6f))
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








