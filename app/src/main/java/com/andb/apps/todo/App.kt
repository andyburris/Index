package com.andb.apps.todo

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
                    view.setColorFilter(if (Utilities.lightOnBackground(Cyanea.instance.accent)) Color.WHITE else Color.BLACK)
                }
            },

            object : CyaneaViewProcessor<BottomAppBar>() {
                override fun getType(): Class<BottomAppBar> = BottomAppBar::class.java
                override fun process(view: BottomAppBar, attrs: AttributeSet?, cyanea: Cyanea) {
                    var drawable = view.navigationIcon?.mutate()
                    drawable?.setColorFilter((if (Utilities.lightOnBackground(Cyanea.instance.primary)) Utilities.colorWithAlpha(Color.WHITE, 0.8f) else Utilities.colorWithAlpha(Color.BLACK, 0.54f)), PorterDuff.Mode.SRC_ATOP)
                    view.navigationIcon = drawable

                    drawable = view.overflowIcon?.mutate()
                    drawable?.setColorFilter((if (Utilities.lightOnBackground(Cyanea.instance.primary)) Utilities.colorWithAlpha(Color.WHITE, 0.8f) else Utilities.colorWithAlpha(Color.BLACK, 0.54f)), PorterDuff.Mode.SRC_ATOP)
                    view.overflowIcon = drawable
                }

            }


    )

    override fun getDecorators(): Array<CyaneaDecorator> = arrayOf(
            // Add a decorator to apply custom attributes to any view
            FontDecorator()
    )
}