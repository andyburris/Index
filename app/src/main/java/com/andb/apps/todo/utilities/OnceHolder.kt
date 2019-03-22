package com.andb.apps.todo.utilities

import android.content.Context
import android.content.Intent
import com.andb.apps.todo.App
import com.andb.apps.todo.Onboarding
import com.jaredrummler.cyanea.Cyanea
import jonathanfinerty.once.Once

object OnceHolder{
    const val appSetup = "app setup"

    @JvmStatic
    fun checkAppSetup(ctxt: Context){
        if(!Once.beenDone(Once.THIS_APP_INSTALL, appSetup)){
            appSetup(ctxt)
            Once.markDone(appSetup)
        }
    }

    fun appSetup(ctxt: Context){
        ctxt.startActivity(Intent(ctxt, Onboarding::class.java))
        Current.bufferProjects.add(ProjectsUtils.addProject("Tasks", Cyanea.instance.accent))
    }
}