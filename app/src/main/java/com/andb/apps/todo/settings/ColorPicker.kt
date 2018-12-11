package com.andb.apps.todo.settings

import android.app.Activity
import android.content.Context
import android.preference.DialogPreference
import android.preference.Preference
import androidx.fragment.app.FragmentActivity
import com.andb.apps.todo.R
import com.jaredrummler.android.colorpicker.ColorPanelView
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.android.colorpicker.ColorPreference
import com.jaredrummler.cyanea.Cyanea
import de.Maxr1998.modernpreferences.PreferencesAdapter

class ColorPicker constructor(val activity: FragmentActivity): de.Maxr1998.modernpreferences.Preference("theme_color") {

    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        super.bindViews(holder)
        var cpv = (holder.widget as ColorPanelView)
        cpv.color = Cyanea.instance.accent
    }

    override fun onClick(holder: PreferencesAdapter.ViewHolder) {
        super.onClick(holder)
        ColorPickerDialog.newBuilder().setColor(Cyanea.instance.accent).show(activity)
    }

    override fun getWidgetLayoutResource(): Int = R.layout.pref_color_picker
}