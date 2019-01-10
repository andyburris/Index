package com.andb.apps.todo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.github.rongi.klaster.Klaster
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.project_switcher_item.view.*
import org.greenrobot.eventbus.EventBus

object Drawer {

    fun setupMenu(context: Context, activity: AppCompatActivity, view: View) {
        view.archive_bg.apply {
            setBackgroundColor(Cyanea.instance.backgroundColor)
            setOnClickListener {
                val intent = Intent(context, Archive::class.java)
                val rect = Rect()
                this.getGlobalVisibleRect(rect)
                val bounds = rect.flattenToString()
                intent.putExtra("expandRect", bounds)
                context.startActivity(intent)
            }
        }
        view.import_export_bg.apply {
            setBackgroundColor(Cyanea.instance.backgroundColor)
            setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Import or export tasks, tags, and links")
                        .setNegativeButton("Export") { _, _ -> ImportExport.exportTasks(context) }
                        .setPositiveButton("Import") { _, _ -> ImportExport.importTasks(context) }

                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
        view.settings_bg.apply {
            setBackgroundColor(Cyanea.instance.backgroundColor)
            setOnClickListener {
                val intent = Intent(context, SettingsActivity::class.java)
                val rect = Rect()
                this.getGlobalVisibleRect(rect)
                val bounds = rect.flattenToString()
                intent.putExtra("expandRect", bounds)
                context.startActivity(intent)
            }
        }
    }

    fun drawerRecycler(layoutInflater: LayoutInflater, view: View, context: Context) = Klaster.get()
            .itemCount { Current.allProjects().size + 1 }
            .view(R.layout.project_switcher_item, layoutInflater)
            .bind { position ->
                if (position < Current.allProjects().size) {//project
                    itemView.apply {
                        project_add_icon.visibility = View.GONE
                        project_text.apply {
                            visibility = View.VISIBLE
                            text = Current.allProjects()[position].name
                        }
                        project_add_divider.visibility = View.GONE
                        project_circle.
                        project_frame.apply {
                            setOnClickListener {
                                ProjectList.viewing = position
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                EventBus.getDefault().post(UpdateEvent(true))
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("project_viewing", Current.viewing()).apply()
                            }
                            setOnLongClickListener {
                                PopupMenu(context, itemView).apply {
                                    menuInflater.inflate(R.menu.project_more_menu, menu)
                                    setOnMenuItemClickListener { menuItem ->
                                        when (menuItem.itemId) {
                                            R.id.editProject -> {
                                                editAlertDialog(context, view, nameInput(context), position).show()
                                            }
                                            R.id.deleteProject -> {
                                                deleteAlertDialog(context, view, position).show()
                                            }
                                        }
                                        true
                                    }
                                }.show()
                                true
                            }
                        }
                    }
                } else {//add project
                    itemView.apply {
                        project_text.visibility = View.INVISIBLE
                        project_add_icon.visibility = View.VISIBLE
                        project_frame.setOnClickListener {
                            addAlertDialog(context, this, nameInput(context)).show()
                        }
                        project_add_divider.visibility = View.VISIBLE
                    }
                }

            }.build()

    fun nameInput(context: Context) = EditText(context).let { editText ->
        editText.apply {
            hint = "Name"
            val density = resources.displayMetrics.density
            setPadding(Math.round(16 * density), Math.round(8 * density), Math.round(16 * density), Math.round(8 * density))
        }
        editText
    }

    fun addAlertDialog(context: Context, view: View, nameInput: EditText) = AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.add_project))
            .setView(nameInput)
            .setPositiveButton("OK") { dialog, which ->
                val project = Project(ProjectsUtils.keyGenerator(), nameInput.text.toString(), ArrayList(), ArrayList(), ArrayList())
                ProjectList.projectList.add(project)
                ProjectList.viewing = ProjectList.projectList.indexOf(project)
                view.projectNameNav.text = Current.project().name
                MainActivity.projectAdapter.notifyDataSetChanged()
                EventBus.getDefault().post(UpdateEvent(false))
                AsyncTask.execute {
                    MainActivity.projectsDatabase.projectsDao().insertOnlySingleProject(Current.project())
                }
            }


    fun editAlertDialog(context: Context, view: View, nameInput: EditText, position: Int) = AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.edit_project))
            .setView(nameInput)
            .setPositiveButton("OK") { _, _ ->
                ProjectList.projectList[position].name = nameInput.text.toString()
                view.projectNameNav.text = Current.project().name
                MainActivity.projectAdapter.notifyDataSetChanged()
                ProjectsUtils.update()
            }


    fun deleteAlertDialog(context: Context, view: View, position: Int) = AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.delete_project))
            .setNegativeButton("Cancel") { _, _ ->
            }
            .setPositiveButton("Delete") { _, _ ->
                if (ProjectList.projectList.size > 0) {//MAYBEDO: Let reset with new project
                    AsyncTask.execute {
                        MainActivity.projectsDatabase.projectsDao().deleteProject(Current.allProjects()[position])
                    }
                    ProjectList.projectList.removeAt(position)
                    if (Current.viewing() == position) {
                        ProjectList.viewing = position - 1
                    }
                    view.projectNameNav.text = Current.project().name
                    MainActivity.projectAdapter.notifyDataSetChanged()
                    EventBus.getDefault().post(UpdateEvent(false))

                } else {
                    Toast.makeText(context, "Can't delete final project", Toast.LENGTH_LONG).show()
                }

            }

    lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var selectedTab: Int = 0

    @JvmStatic
    val normalSheetCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    MainActivity.expanded = true
                    (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.isClickable = true
                    bottomSheet.tabs.apply {//disable selection
                        clearOnTabSelectedListeners()
                        tabRippleColor = ColorStateList.valueOf( Cyanea.instance.primary)
                        selectedTab = selectedTabPosition
                    }
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    MainActivity.expanded = false
                    (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.isClickable = false
                    bottomSheet.tabs.apply {//reenable selection
                        addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(MainActivity.mViewPager))
                        tabRippleColor = ColorStateList.valueOf(Cyanea.instance.accent)
                        getTabAt(selectedTab)?.select()
                    }
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            //slideOffset 0.0 at bottom, 1.0 at top
            (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.alpha = slideOffset
            (bottomSheet.toolbar as ViewGroup).getChildAt(0).apply {
                this.rotation = 180*slideOffset
                val tabIndicatorColor: Int = Utilities.colorFromAlpha(Cyanea.instance.accent, Cyanea.instance.primary, 1-slideOffset)
                //Log.d("tabColorFade", Integer.toHexString(tabIndicatorColor))
                bottomSheet.tabs.setSelectedTabIndicatorColor(tabIndicatorColor)
                val alphaSelected: Float = 1f-(.7f*slideOffset)
                val alphaDeselected: Float = .54f-(.24f*slideOffset)
                bottomSheet.tabs.setTabTextColors(App.colorAlpha(Cyanea.instance.primary, alphaDeselected), App.colorAlpha(Cyanea.instance.primary, alphaSelected))
            }

        }
    }

    @JvmStatic
    val collapsedSheetCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback(){
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}
