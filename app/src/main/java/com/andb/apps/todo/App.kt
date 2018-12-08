package com.andb.apps.todo

import android.app.Application
import com.jaredrummler.cyanea.Cyanea

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        Cyanea.init(this, resources)
    }
}