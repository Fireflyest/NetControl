package com.fireflyest.netcontrol

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fireflyest.netcontrol.util.StatusBarUtil
import com.fireflyest.netcontrol.util.ToastUtil.showLong


class FirstActivity : AppCompatActivity() {

    private var permission = false

    companion object{
        const val REQUEST_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        StatusBarUtil.StatusBarLightMode(this)

        findViewById<TextView>(R.id.first_skip).setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //申请权限
        this.requestPermission()

    }

    /**
     * 动态申请权限
     */
    private fun requestPermission() {
        val state =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (state == PackageManager.PERMISSION_GRANTED) {
            permission = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) { //蓝牙权限开启失败
                showLong(this, "未开启权限")
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}