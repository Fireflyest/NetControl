package com.fireflyest.netcontrol

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.adapter.connectedList.ConnectedItemAdapter
import com.fireflyest.netcontrol.bean.Connected
import com.fireflyest.netcontrol.net.BtController
import com.fireflyest.netcontrol.net.BtManager
import com.fireflyest.netcontrol.net.callback.ConnectStateCallback
import com.fireflyest.netcontrol.util.StatusBarUtil
import com.fireflyest.netcontrol.util.ToastUtil
import java.lang.Thread.sleep


class MainActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null

    private val connecteds: MutableList<Connected> = ArrayList()

    private var connectedItemAdapter: ConnectedItemAdapter? = null
    private var btController: BtController? = null

    private var selectName: TextView? = null
    private var selectAddress: TextView? = null
    private var connectedMotion: MotionLayout? = null

    companion object{
        const val REQUEST_BLUETOOTH = 1

        const val SELECT_BLUETOOTH = 1
    }

    private val handler: Handler = Handler(Handler.Callback() { msg ->
            when (msg.what) {
                SELECT_BLUETOOTH -> {
                    val connected = msg.obj as Connected
//                    selectName!!.text = connected.name
//                    selectAddress!!.text = connected.address
                    connectedMotion!!.transitionToState(R.id.connected_scene_top)
                    btController!!.connect(this, connected.address)
                    ToastUtil.showShort(this, "正在连接: " + connected.name)
                }
//                REFRESH_CARDS -> deviceAdapter.notifyDataSetChanged()
//                REFRESH_PAGER -> pagerAdapter.notifyDataSetChanged()
//                REFRESH_INDEX -> {
//                    val index: Index = msg.obj as Index
//                    indexItemAdapter.addItem(index)
//                }
//                SELECT_CARDS -> {
//                    deviceCards.setCurrentItem(msg.obj)
//                }
//                CONNECT_CALLBACK -> {
//                    showShort(this@MainActivity, msg.obj)
//                }
                else -> {
                }
            }
            true
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //初始化蓝牙控制器
        this.initBleController()
        this.initView()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        if (data == null) return
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_BLUETOOTH -> {
                val name = data.getStringExtra("name")
                val address = data.getStringExtra("address")
                address?.let {a ->
                    name?.let { n ->
                        connectedItemAdapter?.addItem(Connected(a, n))
                    }
                }
            }
//            REQUEST_MODE, REQUEST_COMMAND -> {
//                val mode = data.getStringExtra("mode")
//                Thread(Runnable {
//                    val device: Device = deviceMap.get(SettingManager.SELECT_ADDRESS)
//                    if (device != null) {
//                        val record: Record<*> =
//                            Record<Any?>()
//                        record.setAddress(SettingManager.SELECT_ADDRESS)
//                        record.setTime(CalendarUtil.getDate())
//                        record.setFrom(String.valueOf(device.getMode()))
//                        record.setTo(mode)
//                        if (mode != null && mode != SettingManager.CLOSE_CODE) {
//                            device.setMode(mode)
//                            device.setOpen(true)
//                            device.setStart(CalendarUtil.getDate())
//                            device.setEnd(0)
//                            record.setType("Change")
//                            settingManager.setStringPreference("select_address", "none")
//                            settingManager.setStringPreference(
//                                "select_address",
//                                device.getAddress()
//                            )
//                        } else {
//                            device.setOpen(false)
//                            device.setEnd(CalendarUtil.getDate())
//                            record.setType("Close")
//                            actionButton.setImageResource(R.drawable.animate_action)
//                        }
//                        dataManager.getRecordDao().insertAll(record)
//                        dataManager.getDeviceDao().updateAll(device)
//                    }
//                }).start()
//            }
            else -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 初始化蓝牙控制器
     */
    private fun initBleController() {
        btController = BtManager.getBtController()
        //初始化蓝牙适配器
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) bluetoothAdapter.enable()
        btController?.setStateCallback(object: ConnectStateCallback {
            override fun connectSucceed(deviceAddress: String){

            }

            override fun connectLost(deviceAddress: String) {

            }
        })
    }

    private fun initView(){
        StatusBarUtil.StatusBarLightMode(this)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_main).apply { title="" }
        this.setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.main_drawer)

        connectedItemAdapter = ConnectedItemAdapter(connecteds, handler)
        findViewById<RecyclerView>(R.id.main_connected_list).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2, RecyclerView.VERTICAL, false)
            adapter = connectedItemAdapter
        }
        connectedItemAdapter?.addItem(Connected("请扫描并添加设备", "暂无连接"))

        selectName = findViewById(R.id.main_select_name)
        selectAddress = findViewById(R.id.main_select_address)
        connectedMotion = findViewById<MotionLayout>(R.id.main_connected_box)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            android.R.id.home -> drawerLayout?.openDrawer(GravityCompat.START)
            R.id.nav_device -> {
                intent = Intent(this, ScanActivity::class.java)
                this.startActivityForResult(intent, REQUEST_BLUETOOTH)
            }
            R.id.nav_setting ->{
                drawerLayout?.closeDrawer(0)
            }
            else -> {
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}