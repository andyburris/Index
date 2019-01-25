package com.andb.apps.todo.views

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager
import com.andb.apps.todo.TaskView
import android.view.MotionEvent



class InboxRVViewPager : ViewPager {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var currentPage = 0

    init {
        addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position;
            }
        })
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (TaskView.pageState != 0) {
            false
        } else {
            super.canScrollHorizontally(direction)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return if (TaskView.pageState != 0) {
            false
        } else {
            super.onInterceptTouchEvent(event)
        }    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return if (TaskView.pageState != 0) {
            false
        } else {
            super.onTouchEvent(event)
        }
    }
}