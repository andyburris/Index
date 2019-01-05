package com.andb.apps.todo

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.lists.ProjectList
import com.andb.apps.todo.objects.Project
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.project_list_item.view.*
import org.greenrobot.eventbus.EventBus

object Drawer {
    fun drawerRecycler(layoutInflater: LayoutInflater, view: View, context: Context) = Klaster.get()
            .itemCount(Current.allProjects().size + 1)
            .view(R.layout.project_list_item, layoutInflater)
            .bind { position ->

                val nameInput = EditText(context).let { editText ->
                    editText.apply {
                        hint = "Name"
                        val density = resources.displayMetrics.density
                        setPadding(Math.round(16*density), Math.round(8*density), Math.round(16*density), Math.round(8*density))
                    }
                    editText
                }

                if (position < Current.allProjects().size) {//project
                    itemView.apply {
                        addProjectIcon.visibility = View.GONE
                        projectNameRV.apply {
                            text = Current.allProjects()[position].name
                            setTypeface(typeface,
                                    if (position == Current.viewing())
                                        Typeface.BOLD
                                    else
                                        Typeface.NORMAL
                            )

                            setOnClickListener {
                                ProjectList.viewing = position
                                view.projectNameNav.text = Current.project().name
                                EventBus.getDefault().post(UpdateEvent(true))
                                //Toast.makeText(context, "Viewing " + Current.viewing(), Toast.LENGTH_LONG).show()
                                PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("project_viewing", Current.viewing()).apply()
                            }
                        }
                        projectMoreIcon.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                PopupMenu(context, itemView).apply {
                                    menuInflater.inflate(R.menu.project_more_menu, menu)
                                    setOnMenuItemClickListener { menuItem ->
                                        when(menuItem.itemId){
                                            R.id.editProject->{
                                                AlertDialog.Builder(context)
                                                        .setTitle(resources.getString(R.string.edit_project))
                                                        .setView(nameInput)
                                                        .setPositiveButton("OK") { _, _ ->
                                                            ProjectList.projectList[position].name = nameInput.text.toString()
                                                            view.projectNameNav.text = Current.project().name
                                                            MainActivity.projectAdapter.notifyDataSetChanged()
                                                            //ProjectsUtils.update()
                                                        }
                                            }
                                            R.id.deleteProject->{
                                                AlertDialog.Builder(context)
                                                        .setTitle(resources.getString(R.string.delete_project))
                                                        .setNegativeButton("Cancel") { _, _ ->
                                                        }
                                                        .setPositiveButton("Delete"){_, _->
                                                            if(ProjectList.projectList.size>0) {//MAYBEDO: Let reset with new project
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

                                                            }else{

                                                            }

                                                        }
                                            }
                                        }
                                        true
                                    }
                                }.show()
                            }
                        }
                    }

                } else {//add project
                    itemView.addProjectIcon.visibility = View.VISIBLE
                    itemView.projectMoreIcon.visibility = View.GONE
                    itemView.projectNameRV.apply {
                        text = resources.getString(R.string.add_project)
                        setOnClickListener {

                            AlertDialog.Builder(context)
                                    .setTitle(resources.getString(R.string.add_project))
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
                                    .show()
                        }
                    }
                }

            }.build()


}
