package com.andb.apps.todo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.settings.SettingsActivity
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.views.CyaneaDialog
import com.github.rongi.klaster.Klaster
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.project_create_edit_layout.view.*
import kotlinx.android.synthetic.main.project_switcher_item.view.*
import org.greenrobot.eventbus.EventBus

class Drawer : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        setupMenu(context!!, view)
        projectAdapter = drawerRecycler(inflater, view, context!!)
        view.project_switcher_recycler.apply {
            adapter = projectAdapter
            layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)
        }
        view.project_switcher_frame.setBackgroundColor(Cyanea.instance.backgroundColor)

        addEditLayout = inflater.inflate(R.layout.project_create_edit_layout, view as ViewGroup, false)

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
                if (adapterPosition < Current.allProjects().size) {//project
                    itemView.apply {
                        val imageShape = GradientDrawable()
                        imageShape.color = ColorStateList.valueOf(Current.allProjects()[adapterPosition].color)
                        imageShape.cornerRadius = if (Current.viewing() == adapterPosition) 16f else 92f
                        project_circle.apply {
                            setImageDrawable(imageShape)
                            elevation = if (Current.viewing() == adapterPosition) 4f else 0f
                            background = imageShape//for shadow drawing
                        }

                        project_add_icon.visibility = View.GONE
                        project_text.apply {
                            visibility = View.VISIBLE
                            text = Current.allProjects()[adapterPosition].name
                        }
                        project_add_divider.visibility = View.GONE
                        project_frame.apply {
                            setOnClickListener {
                                ProjectList.viewing = adapterPosition
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                view.toolbar_project_name.text = Current.project().name
                                Filters.backTagFilters.clear()
                                Filters.homeViewAdd()
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
                                                editAlertDialog(context, adapterPosition).show()
                                            }
                                            R.id.deleteProject -> {
                                                deleteAlertDialog(context, view, adapterPosition).show()
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
                            text = Current.allProjects()[adapterPosition].taskList.size.toString()
                            elevation = if (Current.viewing() == adapterPosition) 4f else 0f
                        }
                    }
                } else {//add project
                    itemView.apply {
                        project_text.visibility = View.INVISIBLE
                        project_task_count.visibility = View.INVISIBLE
                        project_add_icon.visibility = View.VISIBLE
                        project_frame.setOnClickListener {
                            addAlertDialog(context, view).show()
                        }
                        project_circle.setColorFilter(Color.DKGRAY)
                        project_add_divider.visibility = View.VISIBLE
                    }
                }

            }.build()


    fun addAlertDialog(context: Context, view: View): android.app.AlertDialog.Builder {


        addEditLayout.apply {
            projectColorPreview.color = selectedColor
            //TODO: Set color to preview on select
            projectColorPreview.setOnClickListener {
                Log.d("clicked", "addProjectDialog color")
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
                .setView(addEditLayout)
                .setPositiveButton("OK") { dialog, which ->


                    val project = ProjectsUtils.addProject(addEditLayout.projectEditText.text.toString(), selectedColor)
                    ProjectList.viewing = ProjectList.projectList.indexOf(project)
                    projectAdapter.notifyDataSetChanged()
                    EventBus.getDefault().post(UpdateEvent(false))

                    AsyncTask.execute {
                        MainActivity.projectsDatabase.projectsDao().insertOnlySingleProject(project)
                    }
                }
        return dialog
    }


    fun editAlertDialog(context: Context, position: Int): android.app.AlertDialog.Builder {


        selectedColor = Current.allProjects()[position].color
        addEditLayout.apply {
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
                .setView(addEditLayout)
                .setPositiveButton("OK") { dialog, which ->
                    ProjectList.projectList[position].apply {
                        name = addEditLayout.projectEditText.text.toString()
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
                    val project = Current.allProjects()[position]
                    AsyncTask.execute {
                        MainActivity.projectsDatabase.projectsDao().deleteProject(project)
                    }
                    ProjectList.projectList.removeAt(position)

                    if (Current.viewing() >= position) {
                        if(position!=0) {
                            ProjectList.viewing = position - 1
                        }
                        EventBus.getDefault().post(UpdateEvent(false))
                    }

                    for (i in Current.viewing() until Current.allProjects().size){
                        val p = Current.allProjects()[i]
                        p.index--
                    }

                    projectAdapter.notifyItemRemoved(position)

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

        lateinit var addEditLayout: View

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


            override fun onSlide(bottomSheet: View, slideOffsetFirst: Float) {
                //slideOffset 0.0 at bottom, 1.0 at top
                var slideOffset = slideOffsetFirst

                if (slideOffsetFirst > 1f){//on non drag tab collapse causes problems, fixes for sheet overdrag and icon overrotate
                    bottomSheet.top = Resources.getSystem().displayMetrics.heightPixels - bottomSheet.height
                    slideOffset = 1f
                }

                bottomSheet.apply {
                    (parent as CoordinatorLayout).bottomSheetDim.alpha = slideOffset
                    (toolbar as ViewGroup).apply {
                        getChildAt(0).rotation = 180 * slideOffset
                    }
                    val tabIndicatorColor: Int = Utilities.colorFromAlpha(Cyanea.instance.accent, Cyanea.instance.primary, 1 - slideOffset)
                    val alphaSelected: Float = 1f - (1f * slideOffset)
                    val alphaDeselected: Float = .54f - (.54f * slideOffset)

                    tabs.apply {
                        setSelectedTabIndicatorColor(tabIndicatorColor)
                        setTabTextColors(App.colorAlpha(Cyanea.instance.primary, alphaDeselected), App.colorAlpha(Cyanea.instance.primary, alphaSelected))

                        (layoutParams as ConstraintLayout.LayoutParams).apply {
                            val margin = Utilities.pxFromDp(88)
                            val fabHeight = Utilities.pxFromDp(36)
                            val tabHeight = Utilities.pxFromDp(48)
                            height = tabHeight - (tabHeight * slideOffset).toInt()
                            topMargin = margin - (slideOffset * (margin - fabHeight)).toInt() + (tabHeight - height)
                        }
                    }

                    Log.d("bottomSheetScroll", "Scroll: $slideOffsetFirst")

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
