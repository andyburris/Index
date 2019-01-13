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
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).apply {
                    setBackgroundColor(Cyanea.instance.backgroundColor)
                    setTextColor(Cyanea.instance.accent)
                    setTypeface(typeface, Typeface.BOLD)
                }
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).apply {
                    setBackgroundColor(Cyanea.instance.backgroundColor)
                    setTextColor(Cyanea.instance.accent)
                    setTypeface(typeface, Typeface.BOLD)
                }
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).apply {
                    setBackgroundColor(Cyanea.instance.backgroundColor)
                    setTextColor(Cyanea.instance.accent)
                    setTypeface(typeface, Typeface.BOLD)
                }
            }
            return dialog
        }


    }
}