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

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

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
        setTags()
        //measureTitleTagBalance()
        taskName.text = task.listName
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

    fun measureTitleTagBalance() {
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            var barrier: Int

            //check if non-clipped bounds intersect
            var nameRight: Int = taskName.right
            var chipsLeft: Int = chipGroup.right

            //if intersect, find balance point
            if (nameRight > chipsLeft) {
                barrier = chipsLeft + (nameRight - chipsLeft) / 2


                var lastLeft = moreTags.left
                //cut chips to fit balance
                chipGroup.left = barrier
                for (i in chipGroup.children) {
                    if (i.left < barrier) {
                        i.visibility = View.INVISIBLE
                    } else if (i.left < lastLeft) { // leftmost visible for text
                        lastLeft = i.left
                    }
                }

                //cut text
                taskName.width = barrier - taskName.left


            }
        }




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

    private fun setTags() {

        val chip1: Chip = chipGroup[0] as Chip
        val chip2: Chip = chipGroup[1] as Chip
        val chip3: Chip = chipGroup[2] as Chip


        if (task.isListTags) {
            Log.d("tags", "multipleTags")


            val tagsList = ArrayList(Arrays.asList<Chip>(chip3, chip2, chip1))

            for (i in tagsList.indices) {

                if (i < task.allListTags.size) {
                    val tagtemp = TagList.getItem(task.getListTags(i))
                    val chiptemp = tagsList[i]

                    chiptemp.text = tagtemp.tagName
                    val drawable = chiptemp.chipIcon!!.mutate()
                    drawable.setColorFilter(tagtemp.tagColor, PorterDuff.Mode.SRC_ATOP)
                    chiptemp.chipIcon = drawable
                    chiptemp.chipBackgroundColor = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
                    chiptemp.visibility = View.VISIBLE

                } else {
                    tagsList[i].visibility = View.INVISIBLE
                }

            }


        } else {
            chipGroup.visibility = View.GONE
            moreTags.visibility = View.GONE
        }

    }

    fun setCyaneaBackground(color: Int) {
        var cl: ConstraintLayout = findViewById(R.id.inboxCard)
        cl.setBackgroundColor(color)
    }


}