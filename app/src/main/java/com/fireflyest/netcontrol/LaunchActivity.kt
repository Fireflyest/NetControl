package com.fireflyest.netcontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.net.BtManager

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        DataService.instance.initDataService(this)

        BtManager.init(0, this)

        startActivity(Intent(this, MainActivity::class.java))

        finish()
    }
}