package com.andb.apps.todo

import android.os.Bundle
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.objects.Tags
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.inbox_header.*
import kotlinx.android.synthetic.main.test_layout.*

class TestActivity : CyaneaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)
        //folderButton.setup(listOf(Tags("Test 1", 0xFF8800, false, 0), Tags("Test 2", 0x00FFFF, false, 1)), ::returnTag)
    }



}