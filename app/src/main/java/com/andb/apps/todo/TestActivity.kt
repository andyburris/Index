package com.andb.apps.todo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.andb.apps.todo.views.LocationFence
import com.andb.apps.todo.views.LocationPicker
import com.google.android.material.snackbar.Snackbar
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.test_layout.*

class TestActivity : CyaneaAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_layout)
        testButton.setOnClickListener {
            pickLocation()
        }
        pickLocation()
        /*testButton2.setOnClickListener {
            sendNotification(this, "TEST NOTIFICATION")
        }*/
    }

    private fun pickLocation() {
        Log.d("locationPick", "picking location")
        val view = LocationPicker(this)
        val dialog = AlertDialog.Builder(this).setView(view).show().also {
            it.window?.setBackgroundDrawable(null)
            it.setOnCancelListener {
                //reminderAdapter.notifyDataSetChanged()
            }
        }
        view.setup(dialog.onSaveInstanceState(), ::returnLocation)
    }

    private fun returnLocation(locationFence: LocationFence) {
        Snackbar.make(testButton.rootView, "lat: ${locationFence.lat}, long: ${locationFence.long}, rad: ${locationFence.radius}", Snackbar.LENGTH_LONG)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
    }


}