package com.andb.apps.todo.ui.drawer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.ImportExport
import com.andb.apps.todo.MainActivity
import com.andb.apps.todo.R
import com.andb.apps.todo.databases.projectsDao
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.data.model.Project
import com.andb.apps.todo.ui.settings.SettingsActivity
import com.andb.apps.todo.utilities.*
import com.andb.apps.todo.util.cyanea.CyaneaDialog
import com.andb.apps.todo.util.getToolbarNavigationButton
import com.github.rongi.klaster.Klaster
import com.github.rongi.klaster.KlasterViewHolder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.cyanea.Cyanea
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import kotlinx.android.synthetic.main.project_create_edit_layout.view.*
import kotlinx.android.synthetic.main.project_switcher_item.view.*
import me.saket.inboxrecyclerview.page.ExpandablePageLayout

const val DRAWER_DIALOG_ID = 1

class Drawer : Fragment() {

    var expanded = false

    lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    var selectedColor: Int = Cyanea.instance.accent

    lateinit var addEditLayout: View
    lateinit var projectAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    lateinit var archiveAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private val drawerViewModel: DrawerViewModel by lazy {
        ViewModelProviders.of(this).get(DrawerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_layout, container?.parent as ViewGroup?, false)

        setupMenu(requireContext(), view)
        projectAdapter = drawerRecycler(inflater, requireContext())
        view.project_switcher_recycler.apply {
            adapter = projectAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        view.project_switcher_frame.setBackgroundColor(Cyanea.instance.backgroundColor)

        addEditLayout = inflater.inflate(R.layout.project_create_edit_layout, view as ViewGroup, false)

        return view
    }

    fun setup(){

        drawerViewModel.projectsAndKey.observe(viewLifecycleOwner, Observer{ pair->
            if(pair.first.isNotEmpty()) {
                update(pair.first)
                if(pair.first.map { it.id }.contains(pair.second)){
                    toolbar_project_name.text = pair.first.first { it.id==pair.second }.name
                }
            }
        })
    }

    fun setupArchive(expandablePageLayout: ExpandablePageLayout){
        drawerArchiveItem.apply {
            setExpandablePage(expandablePageLayout)
        }
    }

    fun openArchive(view: View, item: KlasterViewHolder, adapter: RecyclerView.Adapter<*>){

        val archive = (activity as MainActivity).archive
        val fa = context as FragmentActivity
        fa.supportFragmentManager
            .beginTransaction()
            .replace(R.id.expandable_page_archive, archive)
            .also {
                it.runOnCommit {
                    archive.setupScroll(adapter)
                }
            }
            .commit()

        view.drawerArchiveItem.expandItem(item.itemId)

    }

    private fun setupMenu(context: Context, view: View) {
        view.drawerArchiveItem.apply {
            layoutManager = LinearLayoutManager(context)
            archiveAdapter = Klaster.get().itemCount(1).view(R.layout.drawer_archive_item, layoutInflater).bindEmpty {
                itemView.setOnClickListener {
                    openArchive(view, this, archiveAdapter)
                }
            }.build().also { it.setHasStableIds(true) }
            adapter = archiveAdapter
            setBackgroundColor(Cyanea.instance.backgroundColor)

        }
        view.import_export_bg.apply {
            setBackgroundColor(Cyanea.instance.backgroundColor)
            setOnClickListener {
                val builder = CyaneaDialog.Builder(context)
                builder.setMessage("Import or export tasks, tags, and links")
                    .setNegativeButton("Export") { _, _ -> ImportExport.exportTasks(context, Pair(Current.taskListAll(), Current.tagListAll().sortedBy { it.index })) }
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

    val recyclerProjectList = ArrayList<Project>()
    private fun drawerRecycler(layoutInflater: LayoutInflater, context: Context) =
        Klaster.get()
            .itemCount { recyclerProjectList.size + 1 }
            .view(R.layout.project_switcher_item, layoutInflater)
            .bind { _ ->
                if (adapterPosition < recyclerProjectList.size) {//project
                    val project = recyclerProjectList[adapterPosition]
                    itemView.apply {
                        val imageShape = GradientDrawable()
                        imageShape.color = ColorStateList.valueOf(project.color)
                        imageShape.cornerRadius = if (Current.projectKey() == project.id) 24f else 92f
                        project_circle.apply {
                            setImageDrawable(imageShape)
                            elevation = if (Current.projectKey() == project.id) 4f else 0f
                            background = imageShape//for shadow drawing
                        }

                        project_add_icon.visibility = View.GONE
                        project_text.apply {
                            visibility = View.VISIBLE
                            text = project.name
                        }
                        project_add_divider.visibility = View.GONE
                        project_frame.apply {
                            setOnClickListener {
                                ProjectList.setKey(project.id)
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                Filters.homeViewAdd()
                                projectAdapter.notifyDataSetChanged()
                            }
                            setOnLongClickListener {
                                PopupMenu(context, itemView).apply {
                                    menuInflater.inflate(R.menu.project_more_menu, menu)
                                    setOnMenuItemClickListener { menuItem ->
                                        when (menuItem.itemId) {
                                            R.id.editProject -> {
                                                addEditLayout.apply { (parent as ViewGroup?)?.removeView(this) }
                                                editAlertDialog(context, project).show()
                                            }
                                            R.id.deleteProject -> {
                                                deleteAlertDialog(context, adapterPosition, project).show()
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
                            text = Current.taskListAll(project.id).filter { !it.isArchived }.size.toString()
                            elevation = if (Current.projectKey() == project.id) 4f else 0f
                        }
                    }
                } else {//add project
                    itemView.apply {
                        project_text.visibility = View.INVISIBLE
                        project_task_count.visibility = View.INVISIBLE
                        project_add_icon.visibility = View.VISIBLE
                        project_frame.setOnClickListener {
                            addEditLayout.apply { (parent as ViewGroup?)?.removeView(this) }
                            addAlertDialog(context).show()
                        }
                        project_circle.setColorFilter(Color.DKGRAY)
                        project_add_divider.visibility = View.VISIBLE
                    }
                }

            }.build()

    private fun dispatchUpdates(newItems: List<Project>, diffResult: DiffUtil.DiffResult) {
        Log.d("dipatchUpdates", "newItems size: ${newItems.size}")
        diffResult.dispatchUpdatesTo(projectAdapter)
    }

    fun update(newList: List<Project>) {
        val oldItems: List<Project> = ArrayList()
        recyclerProjectList.apply {
            clear()
            addAll(newList)
        }
        if(oldItems.isNotEmpty()&&newList.isNotEmpty()) {
            val handler = Handler(Looper.getMainLooper())
            Thread(Runnable {
                val diffResult = DiffUtil.calculateDiff(DrawerAdapterDiffCallback(oldItems, newList))
                handler.post {
                    dispatchUpdates(newList, diffResult)
                }
            }).start()
        }else{
            projectAdapter.notifyDataSetChanged()
        }
    }

    internal class DrawerAdapterDiffCallback(private val oldProjects: List<Project>, private val newProjects: List<Project>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldProject = oldProjects[oldItemPosition]
            val newProject = newProjects[newItemPosition]

            return oldProject.id == newProject.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldProject = oldProjects[oldItemPosition]
            val newProject = newProjects[newItemPosition]

            return oldProject == newProject
        }

        override fun getNewListSize(): Int {
            return newProjects.size
        }

        override fun getOldListSize(): Int {
            return oldProjects.size
        }

    }

    private fun addAlertDialog(context: Context): android.app.AlertDialog.Builder {


        addEditLayout.apply {
            selectedColor = Cyanea.instance.accent
            projectColorPreview.color = selectedColor
            projectEditText.clearComposingText()
            projectColorPreview.setOnClickListener {
                Log.d("clicked", "addProjectDialog color")
                val dialog = ColorPickerDialog.newBuilder()
                    .setColor(selectedColor)
                    .setAllowCustom(true)
                    .setShowAlphaSlider(true)
                    .setDialogId(DRAWER_DIALOG_ID)
                    .create()
                dialog.show(requireActivity().supportFragmentManager, "color-picker-dialog")
                requireActivity().supportFragmentManager.executePendingTransactions()
                CyaneaDialog.setColorPickerButtonStyle(dialog.dialog as androidx.appcompat.app.AlertDialog, AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE)
            }
        }

        return CyaneaDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.add_project))
            .setView(addEditLayout)
            .setPositiveButton("OK") { _, _ ->


                val project = ProjectsUtils.addProject(addEditLayout.projectEditText.text.toString(), selectedColor)
                ProjectList.setKey(project.id)
                projectAdapter.notifyDataSetChanged()

            }
            .setOnCancelListener {

            }
    }


    private fun editAlertDialog(context: Context, project: Project): android.app.AlertDialog.Builder {


        selectedColor = project.color
        addEditLayout.apply {
            projectEditText.setText(project.name)
            projectColorPreview.color = selectedColor
            projectColorPreview.setOnClickListener {
                val dialog = ColorPickerDialog.newBuilder()
                    .setColor(selectedColor)
                    .setAllowCustom(true)
                    .setShowAlphaSlider(true)
                    .setDialogId(DRAWER_DIALOG_ID)
                    .create()
                dialog.show(requireActivity().supportFragmentManager, "color-picker-dialog")
                requireActivity().supportFragmentManager.executePendingTransactions()
                CyaneaDialog.setColorPickerButtonStyle(dialog.dialog as androidx.appcompat.app.AlertDialog, AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEUTRAL)
            }
        }

        return CyaneaDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.edit_project))
            .setView(addEditLayout)
            .setPositiveButton("OK") { _, _ ->
                project.apply {
                    name = addEditLayout.projectEditText.text.toString()
                    color = selectedColor
                }
                projectAdapter.notifyDataSetChanged()
                ProjectsUtils.update(project)
            }
    }


    private fun deleteAlertDialog(context: Context, position: Int, project: Project) =
        CyaneaDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.delete_project))
            .setNegativeButton("Cancel") { _, _ ->
            }
            .setPositiveButton("Delete") { _, _ ->
                val handler = Handler()
                AsyncTask.execute {
                    if (projectsDao().allStatic.size <= 1) {
                        Current.database().projectsDao()
                            .insertOnlySingleProject(Project(ProjectsUtils.keyGenerator(), "Task", Cyanea.instance.accent, 0))
                    }
                    Current.database().projectsDao().deleteProject(project)


                    if (Current.projectKey() >= position) {
                        if (position != 0) {
                            val newKey = Current.allProjects()[position - 1].id
                            ProjectList.postKey(newKey)
                            Prefs.putInt("project_viewing", newKey)
                        }
                    }

                    for (i in Current.projectKey() until Current.allProjects().size) {
                        val p = Current.allProjects()[i]
                        p.index--
                        ProjectsUtils.update(p)
                    }

                    handler.post {
                        projectAdapter.notifyItemRemoved(position)
                    }

                }

            }


    val normalSheetCallback: BottomSheetBehavior.BottomSheetCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    expanded = true
                    (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.isClickable = true

                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    expanded = false
                    (bottomSheet.parent as CoordinatorLayout).bottomSheetDim.isClickable = false
                }

            }
        }


        override fun onSlide(bottomSheet: View, slideOffset: Float) {


            bottomSheet.apply {
                (parent as CoordinatorLayout).bottomSheetDim.alpha = slideOffset
            }

            val toolbarColor = Utilities.colorBetween(Cyanea.instance.primary, Cyanea.instance.backgroundColor, slideOffset)
            val textIconColor = if(slideOffset>.5f) {
                Utilities.textFromBackground(Cyanea.instance.backgroundColor, .54f, .8f)
            }
            else {
                Utilities.textFromBackground(Cyanea.instance.primary, .54f, .8f)
            }
            toolbar.apply {
                getToolbarNavigationButton()?.rotation = 180-(180 * slideOffset)
/*                for(item in menu.iterator()){
                    item.icon = item.icon?.mutate().also {
                        it?.setColorFilter(textIconColor, PorterDuff.Mode.SRC_ATOP)
                    }
                }*/
                backgroundTint = ColorStateList.valueOf(toolbarColor)
            }

            //Cyanea.instance.tint(toolbar.menu, requireActivity(), true)

            toolbar_text.setTextColor(textIconColor)
            toolbar_project_name.setTextColor(textIconColor)




        }


    }

    val collapsedSheetCallback: BottomSheetBehavior.BottomSheetCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

}
