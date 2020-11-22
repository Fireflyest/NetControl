package com.fireflyest.netcontrol.util

import android.content.Context
import android.widget.Toast


object ToastUtil {
    fun showShort(context: Context?, message: CharSequence?) {
        val toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
        toast.setText(message)
        toast.show()
    }

    fun showLong(context: Context?, message: CharSequence?) {
        val toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
        toast.setText(message)
        toast.show()
    }

    fun showShort(context: Context?, message: Int) {
        val toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
        toast.setText(message)
        toast.show()
    }

    fun showLong(context: Context?, message: Int) {
        val toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
        toast.setText(message)
        toast.show()
    }
}