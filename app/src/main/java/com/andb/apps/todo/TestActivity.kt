package com.andb.apps.todo

import android.os.Bundle
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity

class TestActivity : CyaneaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)
        //folderButton.setup(listOf(Tag("Test 1", 0xFF8800, false, 0), Tag("Test 2", 0x00FFFF, false, 1)), ::returnTag)
    }



}