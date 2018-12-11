package com.andb.apps.todo

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.andb.apps.todo.views.Icon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.inflator.CyaneaViewProcessor
import com.jaredrummler.cyanea.inflator.decor.CyaneaDecorator
import com.jaredrummler.cyanea.inflator.decor.FontDecorator

class App : Application(), CyaneaDecorator.Provider, CyaneaViewProcessor.Provider{
    override fun onCreate() {
        super.onCreate()
        Cyanea.init(this, resources)
    }

    override fun getViewProcessors(): Array<CyaneaViewProcessor<out View>> = arrayOf(
            // Add a view processor to manipulate a view when inflated.

            object : CyaneaViewProcessor<CardView>(){
                override fun getType(): Class<CardView> = CardView::class.java
                override fun process(view: CardView, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setCardBackgroundColor(ColorStateList.valueOf(Utilities.lighterDarker(Cyanea.instance.backgroundColor, 1.2f)))
                }
            },

            object : CyaneaViewProcessor<FloatingActionButton>(){
                override fun getType(): Class<FloatingActionButton> = FloatingActionButton::class.java
                override fun process(view: FloatingActionButton, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setBackgroundColor(Cyanea.instance.accent)
                    view.setColorFilter( if (Utilities.lightOnBackground(Cyanea.instance.accent)) Color.WHITE else Color.BLACK )
                }
            },

            object : CyaneaViewProcessor<TextView>(){
                override fun getType(): Class<TextView> = TextView::class.java
                override fun process(view: TextView, attrs: AttributeSet?, cyanea: Cyanea) {
                }
            }




    )

    override fun getDecorators(): Array<CyaneaDecorator> = arrayOf(
            // Add a decorator to apply custom attributes to any view
            FontDecorator()
    )
}