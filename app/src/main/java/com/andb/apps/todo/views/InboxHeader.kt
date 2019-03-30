package com.andb.apps.todo.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.todo.R
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.filtering.filterTags
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.Utilities
import kotlinx.android.synthetic.main.folder_picker.view.*
import kotlinx.android.synthetic.main.inbox_header.view.*

class InboxHeader : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflate(context, R.layout.inbox_header, this)
    }

    fun setup(taskCount: Int, expandedEditing: Pair<Boolean, Boolean>, path: String = Filters.path()) {
        task_path_text.text = path
        task_count_text.text = taskCountText(taskCount)
        val tagList = Current.tagListAll().filterTags()/* listOf(Tags("Test 1", 0xFF8800, false, 0), Tags("Test 2", 0x00FFFF, false, 1))*/
        folderButton.apply {
            setup(tagList, expandedEditing, ::returnTag)
            addExpandCollapseListener {e->
                (layoutParams as ConstraintLayout.LayoutParams).bottomMargin = Utilities.pxFromDp(if(e) 8 else 24 )
                folderCardHolder.cardElevation = Utilities.pxFromDp(if(e) 4 else 8).toFloat()
            }
        }

    }


    fun returnTag(tag: Tags, longClick: Boolean) {
        //Snackbar.make(this, "Returned ${tag.tagName}", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
        if(!longClick) {
            Filters.tagForward(tag)
        }else{
            Filters.tagReset(tag)
        }
    }

    fun taskCountText(tasks: Int): String {

        val toApply: String = if (tasks != 1) {
            " TASKS"
        } else {
            " TASK"
        }
        return Integer.toString(tasks) + toApply

    }

}