package com.fireflyest.netcontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fireflyest.netcontrol.util.StatusBarUtil

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.initView()
        startActivity(Intent(this, ScanActivity::class.java))
    }

    private fun initView(){
        StatusBarUtil.StatusBarLightMode(this)
    }

}