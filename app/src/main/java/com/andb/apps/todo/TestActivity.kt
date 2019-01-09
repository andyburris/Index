package com.andb.apps.todo

import android.graphics.PorterDuff
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.views.TaskListItem
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.test_layout.*
import org.joda.time.DateTime
import java.util.*

class TestActivity : CyaneaAppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val task = Tasks("Test Task", ArrayList(0), ArrayList(0), ArrayList(Arrays.asList(0, 3)), DateTime(1970, 1, 1, 1, 1), true)
        setContentView(R.layout.test_layout)


        //testListItem.setup(task, 0, 2)

    }




}