package com.andb.apps.todo

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.cardview.widget.CardView
import com.andb.apps.todo.views.TaskListItem
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.inflator.CyaneaViewProcessor
import com.jaredrummler.cyanea.inflator.decor.CyaneaDecorator
import com.jaredrummler.cyanea.inflator.decor.FontDecorator

class App : Application(), CyaneaDecorator.Provider, CyaneaViewProcessor.Provider {
    override fun onCreate() {
        super.onCreate()
        Cyanea.init(this, resources)
    }

    override fun getViewProcessors(): Array<CyaneaViewProcessor<out View>> = arrayOf(
            // Add a view processor to manipulate a view when inflated.

            object : CyaneaViewProcessor<CardView>() {
                override fun getType(): Class<CardView> = CardView::class.java
                override fun process(view: CardView, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setCardBackgroundColor(ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f)))
                }
            },

            object : CyaneaViewProcessor<FloatingActionButton>() {
                override fun getType(): Class<FloatingActionButton> = FloatingActionButton::class.java
                override fun process(view: FloatingActionButton, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setBackgroundColor(Cyanea.instance.accent)
                    view.setColorFilter(colorAlpha(Cyanea.instance.accent, 1f, 1f))
                }
            },

            object : CyaneaViewProcessor<BottomAppBar>() {
                override fun getType(): Class<BottomAppBar> = BottomAppBar::class.java
                override fun process(view: BottomAppBar, attrs: AttributeSet?, cyanea: Cyanea) {
                    var drawable = view.navigationIcon?.mutate()
                    drawable?.setColorFilter(colorAlpha(Cyanea.instance.primary, .8f, .54f), PorterDuff.Mode.SRC_ATOP)
                    view.navigationIcon = drawable

                    drawable = view.overflowIcon?.mutate()
                    drawable?.setColorFilter(colorAlpha(Cyanea.instance.primary, .8f, .54f), PorterDuff.Mode.SRC_ATOP)
                    view.overflowIcon = drawable
                }

            },

            object : CyaneaViewProcessor<AppCompatCheckBox>(){
                override fun getType(): Class<AppCompatCheckBox> = AppCompatCheckBox::class.java
                override fun process(view: AppCompatCheckBox, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setTextColor(ColorStateList.valueOf(colorAlpha(Cyanea.instance.backgroundColor, 0.8f, 0.54f)))
                }
            }/*,

            object : CyaneaViewProcessor<TabLayout>(){
                override fun getType(): Class<TabLayout> = TabLayout::class.java
                override fun process(view: TabLayout, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setTabTextColors(if (Utilities.lightOnBackground(cyanea.primary)) Color.WHITE else Color.BLACK, if (Utilities.lightOnBackground(cyanea.primary)) Utilities.colorWithAlpha(Color.WHITE, .54f) else Utilities.colorWithAlpha(Color.BLACK, .54f))
                }
            }*/



    )

    override fun getDecorators(): Array<CyaneaDecorator> = arrayOf(
            // Add a decorator to apply custom attributes to any view
            FontDecorator()
    )

    fun colorAlpha(bg: Int, aLight: Float, aDark: Float): Int{
        return if (Utilities.lightOnBackground(bg))
            Utilities.colorWithAlpha(Color.WHITE, aLight)
        else
            Utilities.colorWithAlpha(Color.BLACK, aDark)
    }
}