package com.andb.apps.todo

import android.os.Bundle
import android.os.PersistableBundle
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.views.TaskListItem
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.test_layout.*
import org.joda.time.DateTime
import java.util.*

class TestActivity : CyaneaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val task = Tasks("Test Task", ArrayList(0), ArrayList(0), ArrayList(Arrays.asList(0, 3)), DateTime(1970, 1, 1, 1, 1), true)
        setContentView(R.layout.test_layout)
        testListItem.setup(task, 0, 2)

    }
}