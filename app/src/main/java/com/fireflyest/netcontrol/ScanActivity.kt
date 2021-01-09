package com.fireflyest.netcontrol

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fireflyest.netcontrol.adapter.bluetoothList.BluetoothItemAdapter
import com.fireflyest.netcontrol.adapter.saveList.SaveItemAdapter
import com.fireflyest.netcontrol.anim.FallItemAnimator
import com.fireflyest.netcontrol.bean.Bluetooth
import com.fireflyest.netcontrol.bean.Device
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.data.SettingData
import com.fireflyest.netcontrol.util.StatusBarUtil
import com.fireflyest.netcontrol.util.ToastUtil


class ScanActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var saveItemAdapter: SaveItemAdapter? = null

    private var dataService: DataService? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var backBluetooth: Bluetooth? = null

    private var saves: MutableList<Device>? = ArrayList()

    companion object{
        const val START_CONNECTION = 1
        const val EDIT_SAVE = 2
    }

    private val handler =
        Handler(Handler.Callback { msg ->
            when (msg.what) {
                START_CONNECTION -> {
                    backBluetooth = msg.obj as Bluetooth
                    back()
                }
                EDIT_SAVE -> {
                    val device = msg.obj as Device
                    val intent = Intent(this@ScanActivity, DeviceActivity::class.java)
                    intent.putExtra("name", device.name)
                    intent.putExtra("address", device.address)
                    startActivityForResult(intent, EDIT_SAVE)
                }
                else -> {
                }
            }
            true
        })

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return
        when (requestCode) {
            EDIT_SAVE -> {
                val name = data.getStringExtra("name")
                val address = data.getStringExtra("address")
                address?.let {
                    when (resultCode) {
                        Activity.RESULT_CANCELED -> {
                            saves!!.find { it.address == address }.apply {
                                Thread(Runnable {
                                    dataService!!.deviceDao.delete(Device().apply { this.address = address})
                                }).start()
                                val index = saves!!.indexOf(this)
                                saves!!.remove(this)
                                saveItemAdapter!!.notifyItemRemoved(index)
                            }
                        }
                        Activity.RESULT_FIRST_USER -> {
                            backBluetooth = Bluetooth(name!!, 0, address, 0, 0)
                            back()
                        }
                        Activity.RESULT_OK -> {
                            saves!!.find { it.address == address }.apply {
                                val index = saves!!.indexOf(this)
                                this!!.name = name
                                saveItemAdapter!!.notifyItemChanged(index)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 接收蓝牙广播
     */
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if(loading?.visibility == View.GONE) loading?.visibility = View.VISIBLE

            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                if (addressList.contains(device.address)) return
                if (TextUtils.isEmpty(device.name)) return
                var rssi: Short = -150
                val bundle = intent.extras
                if (bundle != null) rssi = bundle.getShort(BluetoothDevice.EXTRA_RSSI)
                addressList.add(device.address)
                bluetoothItemAdapter?.addItem(
                    Bluetooth(
                        device.name,
                        rssi,
                        device.address,
                        device.bondState,
                        device.bluetoothClass.majorDeviceClass
                    )
                )
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) { //找完
                loading!!.visibility = View.GONE
            }
        }
    }

    private val bluetooths: MutableList<Bluetooth> = ArrayList()
    private val addressList: MutableList<String> = ArrayList()
    private var bluetoothItemAdapter: BluetoothItemAdapter? = null

    private var loading: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(SettingData.instance.themeNight){
            setTheme(R.style.AppDarkTheme)
        }else{
            setTheme(R.style.AppLightTheme)
        }
        setContentView(R.layout.activity_scan)

        this.initBluetooth()

        this.initView()

        this.initData()
    }

    private fun initView(){
        StatusBarUtil.StatusBarLightMode(this)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_scan).apply { title = "" }
        this.setSupportActionBar(toolbar)

        loading = findViewById(R.id.toolbar_scan_box)

        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.scan_refresh)?.apply {
            setOnRefreshListener { startScanBluetooth() }
        }

        bluetoothItemAdapter = BluetoothItemAdapter(bluetooths, handler)
        findViewById<RecyclerView>(R.id.scan_result_list).apply {
            layoutManager = LinearLayoutManager(this@ScanActivity)
            adapter = bluetoothItemAdapter
            itemAnimator = FallItemAnimator()
        }

        saveItemAdapter = SaveItemAdapter(saves, handler)
        findViewById<RecyclerView>(R.id.scan_save_list).apply {
            layoutManager = GridLayoutManager(this@ScanActivity, 4)
            adapter = saveItemAdapter
        }

        this.startScanBluetooth()
    }

    /**
     * 初始化蓝牙适配器
     */
    private fun initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter!!.isEnabled) {
            ToastUtil.showShort(this, "蓝牙未开启")
            bluetoothAdapter?.enable()
        }
        //注册广播接收
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    }

    private fun initData(){
        dataService = DataService.instance
        Thread(Runnable {
            dataService!!.deviceDao.queryAll().forEach {
                saves!!.add(it)
            }
        }).start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) back()
        return true
    }

    override fun onBackPressed() {
        back()
    }

    private fun back(){
        val intent = Intent()
        if (null != backBluetooth) {
            intent.putExtra("name", backBluetooth!!.name)
            intent.putExtra("address", backBluetooth!!.address)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED, intent)
        }
        finishAfterTransition()
    }

    private fun startScanBluetooth(){
        swipeRefreshLayout?.isRefreshing = false

        bluetooths.clear()
        addressList.clear()
        bluetoothItemAdapter?.notifyDataSetChanged()

        if (bluetoothAdapter!!.isDiscovering) bluetoothAdapter!!.cancelDiscovery()
        bluetooths.clear()
        bluetoothAdapter!!.startDiscovery()

        loading?.visibility = View.VISIBLE

    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

}