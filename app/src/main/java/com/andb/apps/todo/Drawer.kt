package com.andb.apps.todo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.CyaneaDialog
import com.github.rongi.klaster.Klaster
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.bottom_sheet_container.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.project_create_edit_layout.*
import kotlinx.android.synthetic.main.project_create_edit_layout.view.*
import kotlinx.android.synthetic.main.project_switcher_item.view.*
import org.greenrobot.eventbus.EventBus

class Drawer : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        setupMenu(context!!, view)
        projectAdapter = drawerRecycler(inflater, view, context!!)
        view.project_switcher_recycler.apply {
            adapter = projectAdapter
            layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        }
        view.project_switcher_frame.setBackgroundColor(Cyanea.instance.backgroundColor)


        return view
    }

    fun setupMenu(context: Context, view: View) {
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
                val builder = CyaneaDialog.Builder(context)
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
                        val imageShape = GradientDrawable()
                        imageShape.color = ColorStateList.valueOf(Current.allProjects()[position].color)
                        imageShape.cornerRadius = if (Current.viewing() == position) 16f else 92f
                        project_circle.apply {
                            setImageDrawable(imageShape)
                            elevation = if (Current.viewing() == position) 4f else 0f
                            background = imageShape//for shadow drawing
                        }

                        project_add_icon.visibility = View.GONE
                        project_text.apply {
                            visibility = View.VISIBLE
                            text = Current.allProjects()[position].name
                        }
                        project_add_divider.visibility = View.GONE
                        project_frame.apply {
                            setOnClickListener {
                                ProjectList.viewing = position
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                view.toolbar_project_name.text = Current.project().name
                                EventBus.getDefault().post(UpdateEvent(false))
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("project_viewing", Current.viewing()).apply()
                                projectAdapter.notifyDataSetChanged()
                            }
                            setOnLongClickListener {
                                PopupMenu(context, itemView).apply {
                                    menuInflater.inflate(R.menu.project_more_menu, menu)
                                    setOnMenuItemClickListener { menuItem ->
                                        when (menuItem.itemId) {
                                            R.id.editProject -> {
                                                editAlertDialog(context, view,  position).show()
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
                        project_task_count.apply {
                            visibility = View.VISIBLE
                            text = Current.allProjects()[position].taskList.size.toString()
                            elevation = if (Current.viewing() == position) 4f else 0f
                        }
                    }
                } else {//add project
                    itemView.apply {
                        project_text.visibility = View.INVISIBLE
                        project_task_count.visibility = View.INVISIBLE
                        project_add_icon.visibility = View.VISIBLE
                        project_frame.setOnClickListener {
                            addAlertDialog(context, this)
                        }
                        project_circle.setColorFilter(Color.DKGRAY)
                        project_add_divider.visibility = View.VISIBLE
                    }
                }

            }.build()




    fun addAlertDialog(context: Context, view: View): android.app.AlertDialog.Builder {

        val inflater = LayoutInflater.from(context)
        val addLayout: View = inflater.inflate(R.layout.project_create_edit_layout, view as ViewGroup, false)
        addLayout.apply {
            projectColorPreview.color = selectedColor
            projectColorPreview.setOnClickListener {
                ColorPickerDialog.newBuilder()
                        .setColor(selectedColor)
                        .setAllowCustom(true)
                        .setShowAlphaSlider(true)
                        .setDialogId(DIALOG_ID)
                        .show(activity)
            }
        }

        val dialog = CyaneaDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.add_project))
                .setView(R.layout.project_create_edit_layout)
                .setPositiveButton("OK") { dialog, which ->
                    val project = Project(ProjectsUtils.keyGenerator(), projectEditText.text.toString(), ArrayList(), ArrayList(), ArrayList(), selectedColor)
                    ProjectList.projectList.add(project)
                    ProjectList.viewing = ProjectList.projectList.indexOf(project)
                    projectAdapter.notifyDataSetChanged()
                    EventBus.getDefault().post(UpdateEvent(false))
                    AsyncTask.execute {
                        MainActivity.projectsDatabase.projectsDao().insertOnlySingleProject(Current.project())
                    }
                }
        return dialog
    }


    fun editAlertDialog(context: Context, view: View,  position: Int): android.app.AlertDialog.Builder {

        val inflater = LayoutInflater.from(context)
        val editLayout: View = inflater.inflate(R.layout.project_create_edit_layout, view as ViewGroup, false)

        selectedColor = Current.allProjects()[position].color
        editLayout.apply {
            projectEditText.setText(Current.allProjects()[position].name)
            projectColorPreview.color = selectedColor
            projectColorPreview.setOnClickListener {
                ColorPickerDialog.newBuilder()
                        .setColor(selectedColor)
                        .setAllowCustom(true)
                        .setShowAlphaSlider(true)
                        .setDialogId(DIALOG_ID)
                        .show(activity)
            }
        }

        val dialog = CyaneaDialog.Builder(context)
                .setTitle(context.resources.getString(R.string.edit_project))
                .setView(editLayout)
                .setPositiveButton("OK") { dialog, which ->
                    ProjectList.projectList[position].apply {
                        name = editLayout.projectEditText.text.toString()
                        color = selectedColor
                    }
                    projectAdapter.notifyDataSetChanged()
                    ProjectsUtils.update(Current.allProjects()[position])
                }

        return dialog
    }


    fun deleteAlertDialog(context: Context, view: View, position: Int) = CyaneaDialog.Builder(context)
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
                    projectAdapter.notifyDataSetChanged()
                    EventBus.getDefault().post(UpdateEvent(false))

                } else {
                    Toast.makeText(context, "Can't delete final project", Toast.LENGTH_LONG).show()
                }

            }



    companion object {
        lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
        var selectedTab: Int = 0

        const val DIALOG_ID = 1
        @JvmStatic
        var selectedColor: Int = Cyanea.instance.accent

        lateinit var projectAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

        @JvmStatic
        val normalSheetCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        MainActivity.expanded = true
                        (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.isClickable = true
                        bottomSheet.tabs.apply {
                            //disable selection
                            clearOnTabSelectedListeners()
                            tabRippleColor = ColorStateList.valueOf(Cyanea.instance.primary)
                            selectedTab = selectedTabPosition
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        MainActivity.expanded = false
                        (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.isClickable = false
                        bottomSheet.tabs.apply {
                            //reenable selection
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
                    this.rotation = 180 * slideOffset
                    val tabIndicatorColor: Int = Utilities.colorFromAlpha(Cyanea.instance.accent, Cyanea.instance.primary, 1 - slideOffset)
                    bottomSheet.tabs.setSelectedTabIndicatorColor(tabIndicatorColor)
                    val alphaSelected: Float = 1f - (1f * slideOffset)
                    val alphaDeselected: Float = .54f - (.54f * slideOffset)
                    bottomSheet.tabs.setTabTextColors(App.colorAlpha(Cyanea.instance.primary, alphaDeselected), App.colorAlpha(Cyanea.instance.primary, alphaSelected))
                }

            }
        }

        @JvmStatic
        val collapsedSheetCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }
}
