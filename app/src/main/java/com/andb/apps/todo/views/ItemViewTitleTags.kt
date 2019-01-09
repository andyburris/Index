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

    fun setOverflow(view: View) {
        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->

            val nameRight = taskName2.right
            val c1Left = chip1.left
            val c2Left = chip2.left
            val c3Left = chip3.left

            if (nameRight > c1Left) {
                chip1.visibility = View.GONE
                chipsVisible = 2
            }
            if (nameRight > c2Left) {
                chip2.visibility = View.GONE
                chipsVisible = 1
            }
            if (nameRight > c3Left) {
                chip3.visibility = View.GONE
                chipsVisible = 0
            }

            //Log.d("clipping chips", "$chipsVisible visible")
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
                    val tagtemp = Current.tagList().get(task.getListTags(i))
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