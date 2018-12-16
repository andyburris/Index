package com.andb.apps.todo.views

import android.content.Context
import android.util.AttributeSet
import android.util.LayoutDirection
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
        var childLeft = paddingLeft
        var childRight = paddingRight
        var cc = childCount
        if (overflowMode == 0) {
            for (i in cc until 0) {
                val child = getChildAt(i) as Chip

                if (child.visibility == View.GONE) {
                    continue
                }


                val lp = child.layoutParams
                var leftMargin = 0
                var rightMargin = 0
                if (lp is ViewGroup.MarginLayoutParams) {
                    leftMargin += lp.marginStart
                    rightMargin += lp.marginEnd
                }

                childLeft = childRight + rightMargin + child.width

                Log.d("betterChipGroup", "Width: $width, childLeft: $childLeft")

                // Updates Flowlayout's max right bound if current child's right bound exceeds it.
                if (childLeft > width) {
                    child.visibility = View.INVISIBLE
                    cc--
                }

                childRight += leftMargin + rightMargin + child.width

            }

            childrenVisible = cc

        } else {
            childrenVisible = cc
        }
    }

    fun align(align: Int){
        when(align){
            0->layoutDirection = View.LAYOUT_DIRECTION_LTR
            1->layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }

}