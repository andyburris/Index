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
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.utilities.Values
import com.google.android.material.chip.Chip
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.view_title_tags.view.*
import org.joda.time.DateTime
import java.util.*

class ItemViewTitleTags : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    var chipsVisible = 3
    var hasTime: Boolean = false

    init {
        inflate(context, R.layout.view_title_tags, this)
    }

    fun setTitle(name: String) {
        taskName2.text = name
    }

    fun setOverflow(view: TaskListItem) {
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            //Log.d("updateOverflow", "updating ${view.task.listName}, width: ${view.width}")
            checkOverflow()
            view.updateOverflow(this)
        }

        view.viewTreeObserver.addOnGlobalLayoutListener {
            //needed for return from different activity(i.e tagselect)
            //Log.d("updateOverflow", "updating ${view.task.listName}, width: ${view.width}")
            checkOverflow()
            view.updateOverflow(this)
        }

    }

    fun checkOverflow(noOverflowIcon: Int = 0) {
        val nameRight = taskName2.right
        val c1Left = chip1.left
        val c2Left = chip2.left
        val c3Left = chip3.left

        chipsVisible = 3

        if (nameRight - noOverflowIcon > c1Left) {
            chip1.visibility = View.GONE
            chipsVisible = 2
        }
        if (nameRight - noOverflowIcon > c2Left) {
            chip2.visibility = View.GONE
            chipsVisible = 1
        }
        if (nameRight - noOverflowIcon > c3Left) {
            chip3.visibility = View.GONE
            chipsVisible = 0
        }

        if (chipsVisible > 0 && hasTime) {
            chipsVisible--
        }
    }

    fun setTags(task: Tasks) {

        val c1: Chip = chip1 as Chip
        val c2: Chip = chip2 as Chip
        val c3: Chip = chip3 as Chip


        if (task.isListTags) {
            Log.d("tags", "multipleTags")


            val tagsList = ArrayList(Arrays.asList<Chip>(c3, c2, c1))

            if (task.hasDate()) {
                val drawable = resources.getDrawable(R.drawable.ic_event_black_24dp).also { it.setColorFilter(Utilities.colorWithAlpha(Utilities.textFromBackground(Cyanea.instance.backgroundColor), .7f), PorterDuff.Mode.SRC_ATOP) }
                val datePattern: String = when (task.dateTime.toLocalDate()) {
                    DateTime.now().toLocalDate() -> "h:mm a"
                    else -> "MMM d"
                }
                val dateTimeDisplay = if (task.hasTime()) {
                                        task.dateTime
                                      } else {
                                        task.dateTime.withTime(SettingsActivity.timeToNotifyForDateOnly.toLocalTime())
                                      }
                c3.apply {
                    chipIcon = drawable
                    text = dateTimeDisplay.toString(datePattern)
                    chipStrokeColor = ColorStateList.valueOf(Utilities.colorWithAlpha(Utilities.textFromBackground(Cyanea.instance.backgroundColor), .4f))
                    chipBackgroundColor = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, Values.cardLighter))
                }
                tagsList.remove(c3)
                hasTime = true
            } else {
                hasTime = false
            }

            for (i in tagsList.indices) {

                if (i < task.allListTags.size) {
                    val reversedPos = task.listTags.size - (i + 1)//to show most nested tags first TODO: Most nested first as option
                    val tagtemp = Current.tagList()[task.listTags[reversedPos]]
                    val chiptemp = tagsList[i]

                    chiptemp.apply {
                        text = tagtemp.tagName
                        val drawable = chipIcon!!.mutate()
                        drawable.setColorFilter(tagtemp.tagColor, PorterDuff.Mode.SRC_ATOP)
                        chipIcon = drawable
                        chipStrokeColor = ColorStateList.valueOf(Utilities.colorWithAlpha(Utilities.textFromBackground(Cyanea.instance.backgroundColor), .4f))
                        chipBackgroundColor = ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f))
                        visibility = View.VISIBLE
                    }


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