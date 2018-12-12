package com.andb.apps.todo.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import com.andb.apps.todo.R
import com.andb.apps.todo.Utilities
import com.jaredrummler.cyanea.Cyanea

class CyaneaTextView(context: Context, attributeSet: AttributeSet) : TextView(context, attributeSet){
    init {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.CyaneaTextView, 0, 0).apply {
            try {
                setCyaneaBackground(getInteger(R.styleable.CyaneaTextView_cyaneaTextBackground, 0), this@CyaneaTextView.alpha)
            }finally {
                recycle()
            }
        }
    }

     fun setCyaneaBackground(bg: Int, alpha: Float){
        when(bg){
            0-> setTextColor(
                    if(Utilities.lightOnBackground(Cyanea.instance.backgroundColor))
                        Utilities.colorWithAlpha(Color.WHITE, alpha)
                    else
                        Utilities.colorWithAlpha(Color.BLACK, alpha))
            1-> setTextColor(
                    if(Utilities.lightOnBackground(Cyanea.instance.primary))
                        Utilities.colorWithAlpha(Color.WHITE, alpha)
                    else
                        Utilities.colorWithAlpha(Color.BLACK, alpha))
            2-> setTextColor(
                    if(Utilities.lightOnBackground(Cyanea.instance.accent))
                        Utilities.colorWithAlpha(Color.WHITE, alpha)
                    else Utilities.colorWithAlpha(Color.BLACK, alpha))
        }
    }
}