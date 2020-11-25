package com.fireflyest.netcontrol

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
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
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.net.BtController
import com.fireflyest.netcontrol.net.BtManager
import com.fireflyest.netcontrol.net.callback.ConnectStateCallback
import com.fireflyest.netcontrol.util.AnimateUtil
import com.fireflyest.netcontrol.util.StatusBarUtil
import com.fireflyest.netcontrol.util.ToastUtil

class MainActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null

    private val connecteds: MutableList<Connected> = ArrayList()

    private var connectedItemAdapter: ConnectedItemAdapter? = null
    private var btController: BtController? = null
    private var dataService: DataService? = null

    private var selectName: TextView? = null
    private var selectAddress: TextView? = null
    private var connectedMotion: MotionLayout? = null
    private var controlMotion: MotionLayout? = null
    private var commandMotion: MotionLayout? = null
    private var selectClose: ImageButton? = null
    private var selectEdit: ImageButton? = null
    private var commandMore: ImageButton? = null
    private var commandEdit: EditText? = null

    private var connectedAddress: String? = null

    companion object{
        const val REQUEST_BLUETOOTH = 1

        const val SELECT_BLUETOOTH = 6
        const val SEND_TOAST = 7
        const val SUCCEED_CONNECT = 8
        const val CLOSE_CONNECT = 9
    }

    private val handler: Handler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                SELECT_BLUETOOTH -> {
                    val connected = msg.obj as Connected
                    connectedMotion!!.transitionToState(R.id.connected_scene_top)
                    btController!!.connect(this, connected.address)
                    ToastUtil.showShort(this, "正在连接: ${connected.name}")
                }
                SEND_TOAST -> ToastUtil.showShort(this, msg.obj as String)
                SUCCEED_CONNECT -> {
                    val connected = msg.obj as Connected
                    selectName!!.text = connected.name
                    selectAddress!!.text = connected.address
                    connectedAddress = connected.address
                    controlMotion!!.transitionToState(R.id.control_scene_success)
                }
                CLOSE_CONNECT -> {
                    selectName!!.text = ""
                    selectAddress!!.text = ""
                    connectedAddress = null
                    controlMotion!!.transitionToState(R.id.control_scene_un)
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

        this.initData()
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
                for(connected in connecteds){
                    if(connected.address == address){
                        handler.obtainMessage(SEND_TOAST, "设备已存在: ${connected.name}").sendToTarget()
                        return
                    }
                }
                connectedItemAdapter?.addItem(Connected(address!!, name!!, false))
                btController!!.connect(this, address)
                connectedMotion!!.transitionToState(R.id.connected_scene_top)
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
                for(connected in connecteds){
                    if(connected.address != deviceAddress) continue
                    handler.obtainMessage(SEND_TOAST, "成功连接: ${connected.name}").sendToTarget()
                    handler.obtainMessage(SUCCEED_CONNECT, connected).sendToTarget()
                    Thread(Runnable {

                    }).start()
                    break
                }
            }

            override fun connectLost(deviceAddress: String) {
                for(connected in connecteds){
                    if(connected.address != deviceAddress) continue
                    handler.obtainMessage(SEND_TOAST, "连接丢失: ${connected.name}").sendToTarget()
                    handler.obtainMessage(CLOSE_CONNECT, connected).sendToTarget()
                    break
                }
            }
        })
    }

    private fun initData(){
        dataService = DataService.instance
    }

    private fun initView(){
        StatusBarUtil.StatusBarLightMode(this)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_main).apply { title="" }
        this.setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.main_drawer)

        connectedItemAdapter = ConnectedItemAdapter(connecteds , handler)
        findViewById<RecyclerView>(R.id.main_connected_list).apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2, RecyclerView.VERTICAL, false)
            adapter = connectedItemAdapter
        }
        connectedItemAdapter?.addItem(Connected("请扫描并添加设备", "暂无连接", false))

        selectName = findViewById(R.id.main_select_name)
        selectAddress = findViewById(R.id.main_select_address)
        connectedMotion = findViewById(R.id.main_connected_box)
        controlMotion = findViewById(R.id.main_control_box)
        commandMotion = findViewById(R.id.main_command_box)

        selectClose = findViewById<ImageButton>(R.id.main_select_close).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
                btController!!.closeConnect(connectedAddress)
            }
        }
        selectEdit = findViewById<ImageButton>(R.id.main_select_edit).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
            }
        }

        commandMore = findViewById<ImageButton>(R.id.command_more).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
            }
        }
        commandEdit = findViewById<EditText>(R.id.command_edit).apply {
            addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty()) {
                        println("scene_command_edit")
                        commandMotion!!.transitionToState(R.id.scene_command_edit)
                    } else {
                        println("scene_command_none")
                        commandMotion!!.transitionToState(R.id.scene_command_none)
                    }
                }
            })
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            android.R.id.home -> drawerLayout?.openDrawer(GravityCompat.START)
            R.id.nav_device -> {
                intent = Intent(this, ScanActivity::class.java)
                this.startActivityForResult(intent, REQUEST_BLUETOOTH)
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

    override fun onBackPressed() {
        if(connectedMotion!!.currentState != R.id.connected_scene_top){
            connectedMotion!!.transitionToState(R.id.connected_scene_top)
            return
        }
        super.onBackPressed()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = this.currentFocus
            view?.let {
                if (isShouldHideInput(view, event)) {
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    commandEdit!!.clearFocus()
                    commandEdit!!.isSelected = false
                }
            }
            return super.dispatchTouchEvent(event)
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(event)) {
            true
        } else onTouchEvent(event)
    }

    private fun isShouldHideInput(
        view: View,
        event: MotionEvent
    ): Boolean {
        if (view is EditText) {
            val leftTop = intArrayOf(0, 0)
            //获取输入框当前的location位置
            view.getLocationInWindow(leftTop)
            val left = leftTop[0]
            val top = leftTop[1]
            val bottom = top + view.height
            val right = left + view.width
            // 点击的是输入框区域，保留点击EditText的事件
            return event.x <= left || event.x >= right || event.y <= top || event.y >= bottom
        }
        return false
    }

}