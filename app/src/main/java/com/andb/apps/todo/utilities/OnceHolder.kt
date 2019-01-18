package com.andb.apps.todo.utilities

import com.andb.apps.todo.App
import com.jaredrummler.cyanea.Cyanea
import jonathanfinerty.once.Once

object OnceHolder{
    const val appSetup = "app setup"

    fun init(app: App){

    }

    fun checkAppSetup(){
        if(!Once.beenDone(Once.THIS_APP_INSTALL, appSetup)){
            appSetup()
            Once.markDone(appSetup)
        }
    }

    fun appSetup(){
        ProjectsUtils.addProject("Tasks", Cyanea.instance.accent)
    }
}