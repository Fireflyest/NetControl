package com.fireflyest.netcontrol

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.data.SettingData
import com.fireflyest.netcontrol.net.BtManager


class LaunchActivity : AppCompatActivity() {

    private var settingData: SettingData? = null
    private var sharedPreferences: SharedPreferences? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initSetting()

        if(SettingData.instance.themeNight){
            setTheme(R.style.AppDarkTheme)
        }else{
            setTheme(R.style.AppLightTheme)
        }
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

    private fun initSetting(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        settingData = SettingData.instance
        settingData!!.notifyHex = sharedPreferences!!.getBoolean("hex_notify", false)
        settingData!!.themeNight = sharedPreferences!!.getBoolean("theme_night", false)
    }
}