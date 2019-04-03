package com.andb.apps.todo.views

import android.content.Context
import android.graphics.Paint
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.todo.R
import com.andb.apps.todo.filtering.filterProjectTags
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_list_item.view.*
import java.util.*
import kotlin.collections.ArrayList

val STATE_ZERO = intArrayOf(R.attr.state_on, -R.attr.state_off)
val STATE_ONE = intArrayOf(-R.attr.state_on, R.attr.state_off)

class TaskListItem : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    lateinit var task: Tasks
    var expanded = false

    var clickListener: ((long: Boolean) -> Unit)? = null
    var expandCollapseListener: ((expand: Boolean) -> Unit)? = null
    var checkListener: ((position: Int, checked: Boolean)->Unit)? = null


    init {
        inflate(context, R.layout.inbox_list_item, this)
    }

    fun setup(task: Tasks, expanded: Boolean) {
        this.task = task
        this.expanded = expanded
        setTasks()
        topLayout.setTags(task)
        topLayout.setTitle(task.listName)
        topLayout.setOverflow(this)

        inboxCard.setOnClickListener {
            clickListener?.invoke(false)
        }
        inboxCard.setOnLongClickListener {
            clickListener?.invoke(true)
            true
        }

        setCyaneaBackground(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
    }

    fun updateOverflow(topLayout: ItemViewTitleTags) {

        if (Current.taskListAll().contains(task)) {//safeguard against update after list has switched but task reference is kept
            if (topLayout.chipsVisible < task.listTagsSize) {
                val colors = ArrayList<Int>()
                for (i in topLayout.chipsVisible until topLayout.chipsVisible + 3) {
                    val reversedPos = task.listTags.size - (i + 1)//to show most nested tags first
                    if (reversedPos > -1) {
                        val tagPos = task.listTags[reversedPos]
                        colors.add(Current.tagListAll().filterProjectTags()[tagPos].tagColor)
                    }
                }
                extraTagsLine.setColors(*colors.toIntArray())
            } else {
                extraTagsLine.setColors()
            }


        }
    }


    private fun setTasks() {

        val checkBoxes = ArrayList(Arrays.asList<CheckBox>(item1, item2, item3))

        if (task.isListItems) {
            Log.d("items", task.listName + ", multipleItems: " + task.listItems.size)

            for (i in 0..2) {
                val scale: Float = this.resources.displayMetrics.density
                val checkBox: CheckBox = checkBoxes[i]
                checkBox.setPadding(Math.round(4f * scale + 0.5f), checkBox.paddingTop, checkBox.paddingRight, checkBox.paddingBottom)

                if (i < task.listItems.size) {

                    checkBox.text = task.listItems[i]
                    checkBox.setOnCheckedChangeListener { _, _ -> } //don't call checkListener on initial check
                    checkBox.isChecked = task.listItemsChecked[i]
                    checkBox.paintFlags = if (!checkBox.isChecked) checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    checkBox.visibility = View.VISIBLE

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        checkBox.paintFlags = if (!isChecked) checkBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() else checkBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        checkListener?.invoke(i, isChecked)
                    }

                } else {
                    checkBox.visibility = View.GONE
                }
            }
            if (task.listItems.size > 3) {
                moreTasks.visibility = View.VISIBLE
            } else {
                moreTasks.visibility = View.GONE
            }
            sublistIcon.visibility = View.VISIBLE

            setupCollapse()


        } else { //no checkboxes
            item1.visibility = View.GONE
            item2.visibility = View.GONE
            item3.visibility = View.GONE
            moreTasks.visibility = View.GONE
            sublistIcon.visibility = View.GONE
        }


    }

    fun setupCollapse() {

        if (expanded) {
            expandSublist(false)
        } else {
            collapseSublist(false)
        }
        sublistIcon.setOnClickListener {
            expanded = !expanded
            expandCollapseListener?.invoke(expanded)
            if (expanded) {
                expandSublist()
            } else {
                collapseSublist()
            }

        }
    }

    fun expandSublist(animate: Boolean = true) {
        Log.d("expandSublist", "expanding")
        expand(animate)
        sublistIcon.setImageState(STATE_ZERO, true)
    }

    fun collapseSublist(animate: Boolean = true) {
        Log.d("collapseSublist", "collapsing")
        collapse(animate)
        sublistIcon.setImageState(STATE_ONE, true)
    }

    var collapsedHeight = Utilities.pxFromDp(56)

    fun expand(animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootView as ViewGroup, ChangeBounds())
        }
        layoutParams = layoutParams.also { it.height = ViewGroup.LayoutParams.WRAP_CONTENT }
    }

    fun collapse(animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(rootView as ViewGroup, ChangeBounds())
        }
        layoutParams = layoutParams.also { it.height = collapsedHeight }
    }

    fun setCyaneaBackground(color: Int) {
        inboxCard.setBackgroundColor(color)
    }


}