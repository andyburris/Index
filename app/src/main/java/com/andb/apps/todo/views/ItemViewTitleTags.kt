package com.andb.apps.todo.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.todo.R
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.Utilities
import com.google.android.material.chip.Chip
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_list_item.view.*
import kotlinx.android.synthetic.main.view_title_tags.view.*
import java.util.*

class ItemViewTitleTags : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    var chipsVisible = 3

    init {
        inflate(context, R.layout.view_title_tags, this)
    }

    fun setTitle(name: String) {
        taskName2.text = name
    }

    fun setOverflow(view: TaskListItem) {
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->

            //hide and show to prevent overflow taking up space that would have been enough to fit last chip
            //checkOverflow(view.moreTags.width)
            Log.d("updateOverflow", "updating ${view.task.listName}")
            checkOverflow()
            view.updateOverflow(this)


        }

        view.viewTreeObserver.addOnGlobalLayoutListener {
            //hide and show to prevent overflow taking up space that would have been enough to fit last chip
            //checkOverflow(view.moreTags.width)
            Log.d("updateOverflow", "updating ${view.task.listName}")
            checkOverflow()
            view.updateOverflow(this)
        }

    }

    fun checkOverflow(noOverflowIcon: Int = 0){
        val nameRight = taskName2.right
        val c1Left = chip1.left
        val c2Left = chip2.left
        val c3Left = chip3.left

        if (nameRight-noOverflowIcon > c1Left) {
            chip1.visibility = View.GONE
            chipsVisible = 2
        }
        if (nameRight-noOverflowIcon > c2Left) {
            chip2.visibility = View.GONE
            chipsVisible = 1
        }
        if (nameRight-noOverflowIcon > c3Left) {
            chip3.visibility = View.GONE
            chipsVisible = 0
        }
    }

    fun setTags(task: Tasks) {

        val c1: Chip = chip1 as Chip
        val c2: Chip = chip2 as Chip
        val c3: Chip = chip3 as Chip


        if (task.isListTags) {
            Log.d("tags", "multipleTags")


            val tagsList = ArrayList(Arrays.asList<Chip>(c3, c2, c1))

            for (i in tagsList.indices) {

                if (i < task.allListTags.size) {
                    val reversedPos = task.listTags.size-(i+1)//to show most nested tags first TODO: Most nested first as option
                    val tagtemp = Current.tagList()[task.listTags[reversedPos]]
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
            c1.visibility = View.GONE
            c2.visibility = View.GONE
            c3.visibility = View.GONE
        }

    }
}