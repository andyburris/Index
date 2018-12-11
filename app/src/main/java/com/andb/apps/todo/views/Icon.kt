package com.andb.apps.todo.views

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.andb.apps.todo.R
import com.andb.apps.todo.Utilities
import com.jaredrummler.cyanea.Cyanea

class Icon(context: Context, attributeSet: AttributeSet) : AppCompatImageView(context, attributeSet){


/*    constructor(ctxt: Context) : super(ctxt)
    constructor(ctxt: Context, attributeSet: AttributeSet) : super(ctxt, attributeSet)
    constructor(ctxt: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(ctxt, attributeSet, defStyleAttr)*/

    init {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.Icon, 0, 0).apply {
            try {
                setCyaneaBackground(getInteger(R.styleable.Icon_cyaneaBackground, 0))
            }finally {
                recycle()
            }
        }

    }

    fun setCyaneaBackground(bg: Int){
        when(bg){
            0-> setColorFilter(if(Utilities.lightOnBackground(Cyanea.instance.backgroundColor)) Color.WHITE else Color.BLACK)
            1-> setColorFilter(if(Utilities.lightOnBackground(Cyanea.instance.primary)) Color.WHITE else Color.BLACK)
            2-> setColorFilter(if(Utilities.lightOnBackground(Cyanea.instance.accent)) Color.WHITE else Color.BLACK)
        }
    }
}