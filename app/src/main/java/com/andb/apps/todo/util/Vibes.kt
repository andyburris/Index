package com.andb.apps.todo.util

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Vibes {
    lateinit var vibrator: Vibrator

    @JvmStatic
    @JvmOverloads
    fun vibrate(millis: Long = 4, amplitude: Int = 75){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, amplitude))
        }else{
            vibrator.vibrate(millis)
        }
    }

    @JvmStatic
    fun init(context: Context){
        vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }
}
