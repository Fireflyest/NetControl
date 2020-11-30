package com.fireflyest.netcontrol

import android.R.attr.key
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.net.BtManager


class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        DataService.instance.initDataService(this)

        BtManager.init(0, this)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstLunch = sharedPreferences.getBoolean("first_lunch", true)
        if(firstLunch){
            val editor = sharedPreferences.edit()
            editor.putBoolean("first_lunch", false)
            editor.apply()
            startActivity(Intent(this, FirstActivity::class.java))
        }else{
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}