package com.andb.apps.todo.views

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.todo.R
import com.andb.apps.todo.TagSelect
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.inbox_header.view.*

class InboxHeader : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        inflate(context, R.layout.inbox_header, this)
    }

    fun setup(taskCount: Int, path: String = Filters.path()) {
        task_path_text.text = path
        task_count_text.text = taskCountText(taskCount)
        setupButton()
    }

    fun setupButton() {
        val bgdrawable = resources.getDrawable(R.drawable.rounded_button_background).mutate()
        bgdrawable.setColorFilter(Cyanea.instance.accent, PorterDuff.Mode.SRC_ATOP)

        tag_button.apply {
            background = bgdrawable
            backgroundTintList = ColorStateList.valueOf(Cyanea.instance.accent)


            val drawable = resources.getDrawable(R.drawable.ic_label_black_24dp).mutate()
            if (Utilities.lightOnBackground(Cyanea.instance.accent)) {
                val color = -0x1
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                setTextColor(color)
            } else {
                val color = -0x1000000
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                setTextColor(color)
            }
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

            setOnClickListener {
                val intent = Intent(context, TagSelect::class.java)
                intent.putExtra("isTagLink", false)
                context.startActivity(intent)
            }
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