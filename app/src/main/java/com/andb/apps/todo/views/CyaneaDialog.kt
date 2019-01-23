package com.andb.apps.todo.views

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.Cyanea

class CyaneaDialog(context: Context) : AlertDialog(context) {

    class Builder(context: Context) : AlertDialog.Builder(context){


        override fun create(): AlertDialog {
            val dialog: AlertDialog = super.create()
            dialog.setOnShowListener {
                setButtonStyle(dialog, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL, DialogInterface.BUTTON_POSITIVE)
            }
            return dialog
        }

    }

    companion object {
        fun setButtonStyle(alertDialog: AlertDialog, vararg which: Int){
            alertDialog.apply {
                for (b in which){
                    getButton(b).apply {
                        setBackgroundColor(Cyanea.instance.backgroundColor)
                        setTextColor(Cyanea.instance.accent)
                        setTypeface(typeface, Typeface.BOLD)
                    }
                }
            }
        }

        fun setColorPickerButtonStyle(alertDialog: androidx.appcompat.app.AlertDialog, vararg which: Int){
            alertDialog.apply {
                for (b in which){
                    getButton(b).apply {
                        setBackgroundColor(Cyanea.instance.backgroundColor)
                        setTextColor(Cyanea.instance.accent)
                        setTypeface(typeface, Typeface.BOLD)
                        textSize = 14f
                    }
                }
            }
        }
    }
}