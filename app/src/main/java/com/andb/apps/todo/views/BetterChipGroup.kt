package com.andb.apps.todo.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.andb.apps.todo.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class BetterChipGroup(context: Context, attributeSet: AttributeSet) : ChipGroup(context, attributeSet) {

    var overflow: Int = 0
    var align: Int = 0
    var childrenVisible: Int = -1

    init {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.BetterChipGroup, 0, 0).apply {
            try {
                align = getInteger(R.styleable.BetterChipGroup_align, 0)
                overflow = getInteger(R.styleable.BetterChipGroup_overflowChips, 1)
            } finally {
                recycle()
            }
        }
    }


    fun setOverflowMode(overflowMode: Int) {
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            var childTop: Int
            var cc = childCount
            var childrenShowing = false
            for (i in cc - 1 downTo 0) {
                if (getChildAt(i).visibility == View.VISIBLE) {
                    childrenShowing = true
                    continue
                }
            }



            if (overflowMode == 0 && childrenShowing) {
                childTop = getChildAt(cc - 1).top

                for (i in cc - 1 downTo 0) {
                    val child = getChildAt(i) as Chip

                    if (child.visibility == View.GONE) {
                        continue
                    }
                    if (child.top < childTop) {
                        child.visibility = View.GONE
                    }


                }
                childrenVisible = cc


            }
        }

    }

    fun align(align: Int) {
        when (align) {
            0 -> layoutDirection = View.LAYOUT_DIRECTION_LTR
            1 -> layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }


}