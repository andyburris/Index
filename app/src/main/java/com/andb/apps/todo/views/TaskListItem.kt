package com.andb.apps.todo.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import com.andb.apps.todo.*
import com.andb.apps.todo.lists.TagList
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.settings.SettingsActivity
import com.google.android.material.chip.Chip
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_list_item.view.*
import java.util.*

class TaskListItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    lateinit var task: Tasks
    private val STATE_ZERO = intArrayOf(R.attr.state_on, -R.attr.state_off)
    private val STATE_ONE = intArrayOf(-R.attr.state_on, R.attr.state_off)
    private var sublistToggleState = SettingsActivity.subtaskDefaultShow


    init {
        inflate(context, R.layout.inbox_list_item, this)
    }

    fun setup(tasks: Tasks, pos: Int, inboxBrowseArchive: Int) {
        task = tasks
        setTasks()
        topLayout.setTags(tasks)
        topLayout.setTitle(task.listName)
        inboxCard.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("pos", pos)
            bundle.putInt("inboxBrowseArchive", inboxBrowseArchive)

            val activity = context as FragmentActivity
            val ft = activity.supportFragmentManager.beginTransaction()

            val taskView = TaskView()
            taskView.arguments = bundle
            ft.add(R.id.expandable_page_inbox, taskView)
            ft.commit()

            when (inboxBrowseArchive) {
                TaskAdapter.FROM_BROWSE -> BrowseFragment.mRecyclerView.expandItem(BrowseFragment.mAdapter.getItemId(pos))
                TaskAdapter.FROM_ARCHIVE -> {
                }
                else //inbox
                -> InboxFragment.mRecyclerView.expandItem(InboxFragment.mAdapter.getItemId(pos))
            }
        }
        setCyaneaBackground(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
    }



    private fun setTasks() {

        val checkBoxes = ArrayList(Arrays.asList<CheckBox>(item1, item2, item3))

        if (task.isListItems) {
            Log.d("items", task.listName + ", multipleItems: " + task.listItemsSize)

            for (i in 0..2) {
                if (i < task.listItemsSize) {

                    checkBoxes[i].text = task.listItems[i]
                    checkBoxes[i].setOnCheckedChangeListener { buttonView, isChecked -> task.editListItemsChecked(isChecked, i) }
                    checkBoxes[i].isChecked = task.getListItemsChecked(i)
                    checkBoxes[i].visibility = View.VISIBLE

                } else {
                    checkBoxes[i].visibility = View.GONE
                }
            }
            if (task.listItemsSize > 3) {
                moreTasks.visibility = View.VISIBLE
            } else {
                moreTasks.visibility = View.GONE
            }

            val constraintLayout = item1.parent as ConstraintLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            sublistIcon.visibility = View.VISIBLE
            if (SettingsActivity.subtaskDefaultShow) {
                constraintSet.constrainHeight(R.id.item1, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(R.id.item2, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(R.id.item3, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(R.id.listitempadding, 16)
                constraintSet.applyTo(constraintLayout)
                sublistIcon.setImageState(STATE_ZERO, true)
                sublistToggleState = true
            } else {
                constraintSet.constrainHeight(R.id.item1, 1)
                constraintSet.constrainHeight(R.id.item2, 1)
                constraintSet.constrainHeight(R.id.item3, 1)
                constraintSet.constrainHeight(R.id.listitempadding, 0)
                constraintSet.applyTo(constraintLayout)
                sublistIcon.setImageState(STATE_ONE, true)
                sublistToggleState = false
            }

            sublistIcon.setOnClickListener {
                sublistToggleState = !sublistToggleState

                if (sublistToggleState) {
                    sublistIcon.setImageState(STATE_ZERO, true)

                    TransitionManager.beginDelayedTransition(constraintLayout, TransitionSet().addTransition(ChangeBounds()))


                    constraintSet.constrainHeight(R.id.item1, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainHeight(R.id.item2, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainHeight(R.id.item3, ConstraintSet.WRAP_CONTENT)
                    constraintSet.constrainHeight(R.id.listitempadding, 16)
                    constraintSet.applyTo(constraintLayout)

                } else {
                    sublistIcon.setImageState(STATE_ONE, true)

                    TransitionManager.beginDelayedTransition(constraintLayout, TransitionSet().addTransition(ChangeBounds()))

                    constraintSet.constrainHeight(R.id.item1, 1)
                    constraintSet.constrainHeight(R.id.item2, 1)
                    constraintSet.constrainHeight(R.id.item3, 1)
                    constraintSet.constrainHeight(R.id.listitempadding, 0)
                    constraintSet.applyTo(constraintLayout)
                }
            }


        } else { //no checkboxes
            item1.visibility = View.GONE
            item2.visibility = View.GONE
            item3.visibility = View.GONE
            moreTasks.visibility = View.GONE
            sublistIcon.visibility = View.GONE
        }


    }



    fun setCyaneaBackground(color: Int) {
        var cl: ConstraintLayout = findViewById(R.id.inboxCard)
        cl.setBackgroundColor(color)
    }


}