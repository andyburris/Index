package com.andb.apps.todo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
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
import com.andb.apps.todo.notifications.NotificationHandler
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.ReminderPicker
import com.github.rongi.klaster.Klaster
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.add_task.view.*
import kotlinx.android.synthetic.main.add_task_list_item.view.*
import kotlinx.android.synthetic.main.task_view_tag_list_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList


class AddTask(ctxt: Context, val activity: MainActivity) : FrameLayout(ctxt) {


    private lateinit var subtaskAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private lateinit var tagAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    private lateinit var task: Tasks

    private var dateSet = false

    init {
        inflate(context, R.layout.add_task, this)
        EventBus.getDefault().register(this)
    }

    fun setup(task: Tasks) {
        this.task = task
        theme()
        setupName()
        setupRecyclers()
        setupButtons()
        setReminders()
    }


    private fun theme() {
        addTaskEditFrame.setBackgroundColor(Utilities.lighterDarker(Cyanea.instance.backgroundColor, .95f))
        addTaskName.apply {
            setHintTextColor(Utilities.sidedLighterDarker(Cyanea.instance.backgroundColor, .7f))
            setTextColor(Utilities.sidedLighterDarker(Cyanea.instance.backgroundColor, .5f))
            setCursorColor(this, Utilities.sidedLighterDarker(Cyanea.instance.backgroundColor, .6f))
        }
        checkDividers()
    }


    private fun setupName() {
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


    private fun setupRecyclers() {
        val tagRecyclerView = addTaskTagsRV
        tagRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        tagAdapter = tagAdapter()
        tagRecyclerView.adapter = tagAdapter

        val taskRecycler = addTaskSublistRV
        taskRecycler.layoutManager = LinearLayoutManager(context)
        subtaskAdapter = subtaskAdapter()
        taskRecycler.adapter = subtaskAdapter
        val ith = ItemTouchHelper(_ithCallback)
        ith.attachToRecyclerView(taskRecycler)
    }

    private fun setupButtons() {
        addTaskAddListIcon.setOnClickListener {
            addSubtask()
            checkDividers()
        }
        addTaskAddTagsIcon.setOnClickListener {
            addTag()
            checkDividers()
        }
        addTaskAddReminderIcon.setOnClickListener {
            addReminder()
        }
        addTaskConfirm.setOnClickListener {
            save()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setReminders() {
        addTaskRemindersText.text = (task.timeReminders.size + task.locationReminders.size).toString() + " " + context.getString(R.string.reminder_picker_title)
    }

    private fun addReminder() {
        val view = ReminderPicker(context)
        val dialog = AlertDialog.Builder(context).setView(view).show().also {
            it.window?.setBackgroundDrawable(null)
            it.setOnCancelListener {
                setReminders()
            }
        }
        view.setup(task, dialog)
    }

    private fun checkDividers() {
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

    private fun addSubtask() {
        task.listItems.add("")
        task.listItemsChecked.add(false)
        subtaskAdapter.notifyItemInserted(task.listItems.size - 1)
    }

    private fun addTag() {
        val selectTag = Intent(context, TagSelect::class.java)
        selectTag.putExtra("isTaskCreate", true)
        context.startActivity(selectTag)
    }


    private fun validate(): Boolean {
        if (task.listName.isEmpty()) {
            Snackbar.make(addTaskEditFrame.rootView, "Please provide a name for the task", Snackbar.LENGTH_LONG)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            return false
        }
        when (task.listName) {
            "OVERDUE", "TODAY", "WEEK", "MONTH", "FUTURE", "ADD_TASK_PLACEHOLDER" -> {
                Snackbar.make(addTaskEditFrame.rootView, "Sorry, those names are reserved for the app's use. Please choose a different wording or capitalization", Snackbar.LENGTH_LONG)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
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
            update()
            ProjectsUtils.update(task)
            NotificationHandler.resetNotifications()
        }
    }

    private fun update() {
        task.isEditing = false
        activity.inboxFragment.adding = false
        activity.inboxFragment.editingId = -1
    }


    private fun tagAdapter() = Klaster.get()
        .itemCount { task.listTagsSize }
        .view(R.layout.task_view_tag_list_item, LayoutInflater.from(context))
        .bind { _ ->
            val tag = Current.tagListAll()[task.listTags[adapterPosition]]
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

    private fun subtaskAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> = Klaster.get()
        .itemCount { task.listItems.size }
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
                if (adapterPosition == task.listItems.size - 1) {
                    (layoutParams as RecyclerView.LayoutParams).bottomMargin = Utilities.pxFromDp(8)
                } else {
                    (layoutParams as RecyclerView.LayoutParams).bottomMargin = Utilities.pxFromDp(0)
                }
            }
        }
        .build()


    private fun setCursorColor(editText: EditText, color: Int) {
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
            return ItemTouchHelper.Callback.makeFlag(
                ItemTouchHelper.ACTION_STATE_DRAG,
                ItemTouchHelper.DOWN or ItemTouchHelper.UP
            )
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
            Snackbar.make(addTaskEditFrame.rootView, "The tag you selected has already been added to this task", Snackbar.LENGTH_LONG)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        }
    }


}








