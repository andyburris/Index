package com.andb.apps.todo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.andb.apps.todo.objects.reminders.LocationFence
import com.andb.apps.todo.views.LocationPicker
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.test_layout.*

class TestActivity : CyaneaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)
    }


}