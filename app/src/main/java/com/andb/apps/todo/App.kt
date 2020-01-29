package com.andb.apps.todo

import android.app.Application
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.cardview.widget.CardView
import androidx.room.Room
import com.andb.apps.todo.data.local.Database
import com.andb.apps.todo.ui.addtag.AddTagViewModel
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.util.Vibes
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.inflator.CyaneaViewProcessor
import com.jaredrummler.cyanea.inflator.decor.CyaneaDecorator
import com.jaredrummler.cyanea.inflator.decor.FontDecorator
import com.pixplicity.easyprefs.library.Prefs
import jonathanfinerty.once.Once
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application(), CyaneaDecorator.Provider, CyaneaViewProcessor.Provider {

    private val koinModule = module {
        single {
            Room.databaseBuilder(androidContext(), Database::class.java, "IndexDatabase")
                    .fallbackToDestructiveMigration()
                    .build()
        }
        single { val db: Database = get(); db.projectsDao() }
        single { val db: Database = get(); db.tagsDao() }
        single { val db: Database = get(); db.tasksDao() }

        viewModel { id: String -> AddTagViewModel() }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(koinModule)
        }

        Cyanea.init(this, resources)
        Vibes.init(this)
        Once.initialise(this)

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
                }

            },

            object : CyaneaViewProcessor<AppCompatCheckBox>() {
                override fun getType(): Class<AppCompatCheckBox> = AppCompatCheckBox::class.java
                override fun process(view: AppCompatCheckBox, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setTextColor(ColorStateList.valueOf(colorAlpha(Cyanea.instance.backgroundColor, .8f, .54f)))
                }
            },

            object : CyaneaViewProcessor<TabLayout>() {
                override fun getType(): Class<TabLayout> = TabLayout::class.java
                override fun process(view: TabLayout, attrs: AttributeSet?, cyanea: Cyanea) {
                    view.setTabTextColors(if (Utilities.lightOnBackground(cyanea.primary)) Utilities.colorWithAlpha(Color.WHITE, .54f) else Utilities.colorWithAlpha(Color.BLACK, .54f), if (Utilities.lightOnBackground(cyanea.primary)) Color.WHITE else Color.BLACK)
                    view.setBackgroundColor(cyanea.primary)
                    view.setSelectedTabIndicatorColor(cyanea.accent)
                    view.tabRippleColor = ColorStateList.valueOf(cyanea.accent)
                }
            }


    )

    override fun getDecorators(): Array<CyaneaDecorator> = arrayOf(
            // Add a decorator to apply custom attributes to any view
            FontDecorator()
    )

    companion object {
        fun colorAlpha(bg: Int, alpha: Float, alphaDark: Float = alpha): Int {
            return if (Utilities.lightOnBackground(bg))
                Utilities.colorWithAlpha(Color.WHITE, alpha)
            else
                Utilities.colorWithAlpha(Color.BLACK, alphaDark)
        }
    }

}