package com.andb.apps.todo.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

class SectionedLine(context: Context, attributeSet: AttributeSet) : View(context, attributeSet){
    var colors = ArrayList<Int>()

    init {

    }

    fun setColors(vararg colors: Int){
        this.colors = ArrayList(colors.toList())
        updateLine()
    }

    fun updateLine(){
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

/*        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)*/

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val width: Int = resolveSize(desiredWidth, widthMeasureSpec)
        val height: Int = resolveSize(desiredHeight, heightMeasureSpec)


        setMeasuredDimension(width, height)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if(canvas!=null){
            for ((i, c) in colors.withIndex()){
                val rect = Rect()
                rect.left = 0
                rect.right = width
                rect.top = (height/colors.size)*i
                rect.bottom = (height/colors.size)*(i+1)

                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = c
                canvas.drawRect(rect, paint)
            }
        }
    }
}