package com.fireflyest.netcontrol

import android.app.Activity
import android.app.ActivityOptions
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.adapter.commandList.CommandItemAdapter
import com.fireflyest.netcontrol.adapter.connectedList.ConnectedItemAdapter
import com.fireflyest.netcontrol.anim.FloatItemAnimator
import com.fireflyest.netcontrol.bean.Command
import com.fireflyest.netcontrol.bean.Connected
import com.fireflyest.netcontrol.bean.Device
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.net.BtController
import com.fireflyest.netcontrol.net.BtManager
import com.fireflyest.netcontrol.net.callback.ConnectStateCallback
import com.fireflyest.netcontrol.net.callback.OnWriteCallback
import com.fireflyest.netcontrol.util.AnimateUtil
import com.fireflyest.netcontrol.util.CalendarUtil
import com.fireflyest.netcontrol.util.StatusBarUtil
import com.fireflyest.netcontrol.util.ToastUtil
import android.util.Pair as UtilPair

class MainActivity : AppCompatActivity() {

    private var drawerLayout: DrawerLayout? = null

    private val connecteds: MutableList<Connected> = ArrayList()
    private val commands: MutableList<Command> = ArrayList()
    private val devices: MutableList<String> = ArrayList()

    private var connectedItemAdapter: ConnectedItemAdapter? = null
    private var commandItemAdapter: CommandItemAdapter? = null
    private var btController: BtController? = null
    private var dataService: DataService? = null
    private var sharedPreferences: SharedPreferences? =null
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private var selectName: TextView? = null
    private var selectAddress: TextView? = null
    private var connectedMotion: MotionLayout? = null
    private var controlMotion: MotionLayout? = null
    private var commandMotion: MotionLayout? = null
    private var selectClose: ImageButton? = null
    private var selectEdit: ImageButton? = null
    private var selectClear: ImageButton? = null
    private var commandMore: ImageButton? = null
    private var commandEdit: EditText? = null
    private var commandSend: TextView? = null
    private var commandList: RecyclerView? = null

    private var connectedAddress: String? = null
    private var lastTime: Long = 0

    companion object{
        const val REQUEST_BLUETOOTH = 1

        const val SELECT_BLUETOOTH = 6
        const val SEND_TOAST = 7
        const val SUCCEED_CONNECT = 8
        const val CLOSE_CONNECT = 9
        const val UPDATE_CONNECTED = 10
        const val EDIT_DEVICE = 11
        const val ADD_COMMAND = 12
        const val REFRESH_COMMAND = 13
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
                UPDATE_CONNECTED ->{
                    connectedItemAdapter!!.notifyItemChanged(msg.obj as Int)
                }
                ADD_COMMAND ->{
                    commandItemAdapter!!.addItem(msg.obj as Command)
                    commandList!!.smoothScrollToPosition(commands.size)
                }
                REFRESH_COMMAND ->{
                    commandItemAdapter!!.notifyDataSetChanged()
                }
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
        this.initData()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        if (data == null) return
        when (requestCode) {
            REQUEST_BLUETOOTH -> {
                if(resultCode != Activity.RESULT_OK) return
                val name = data.getStringExtra("name")
                val address = data.getStringExtra("address")
                connecteds.find { it.address == address }?.apply {
                    handler.obtainMessage(SEND_TOAST, "设备已存在: ${this.name}").sendToTarget()
                    return
                }
                connectedItemAdapter?.addItem(Connected(address!!, name!!, false))
                btController!!.connect(this, address)
                connectedMotion!!.transitionToState(R.id.connected_scene_top)
            }
            EDIT_DEVICE ->{
                val name = data.getStringExtra("name")
                val address = data.getStringExtra("address")
                address?.let {
                    selectName!!.text = name
                    connecteds.find { it.address == address }?.apply {
                        val num = connecteds.indexOf(this)
                        if(resultCode == Activity.RESULT_CANCELED){
                            Thread(Runnable {
                                dataService!!.deviceDao.delete(Device().apply { this.address = address})
                            }).start()
                            this.save = false
                            devices.remove(address)
                            handler.obtainMessage(UPDATE_CONNECTED, num).sendToTarget()
                        }else{
                            this.name = name!!
                            handler.obtainMessage(UPDATE_CONNECTED, num).sendToTarget()
                        }
                    }
                }
            }
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
                connecteds.find{it.address == deviceAddress}?.apply {
                    this.save = true
                    handler.obtainMessage(SEND_TOAST, "成功连接: ${this.name}").sendToTarget()
                    handler.obtainMessage(SUCCEED_CONNECT, this).sendToTarget()
                    handler.obtainMessage(UPDATE_CONNECTED, connecteds.indexOf(this)).sendToTarget()
                    Thread(Runnable {
                        val device: Device?
                        if(!devices.contains(deviceAddress)){
                            val uuid: Array<String> = getUUID(this.name)
                            device = Device(
                                deviceAddress,
                                this.name,
                                this.name,
                                "",
                                uuid[0],
                                uuid[1],
                                false,
                                CalendarUtil.getDate()
                            )
                            dataService!!.deviceDao.insert(device)
                            devices.add(deviceAddress)
                        }else{
                            device = dataService!!.deviceDao.findByAddress(deviceAddress)
                        }
                        if(device.service != ""){
                            btController!!.setCharacteristic(deviceAddress, device.service, device.characteristic)
                        }
                        commands.clear()
                        dataService!!.commandDao.queryAll().filter { it.address == deviceAddress }.forEach{
                            commands.add(it)
                        }
                    }).start()
                }
            }

            override fun connectLost(deviceAddress: String) {
                connecteds.find { it.address == deviceAddress }?.let {
                    handler.obtainMessage(SEND_TOAST, "连接丢失: ${it.name}").sendToTarget()
                    handler.obtainMessage(CLOSE_CONNECT, it).sendToTarget()
                    if(deviceAddress == connectedAddress) commands.clear()
                }
            }
        })
        btController?.registerReceiveListener("mainActivity"
        ) { value ->
            var string = String(value!!)
            if(string.endsWith("\r\n"))string = string.trimEnd('\r', '\n')
            addData(string, "Receive", true)
        }
    }


    private fun initData(){
        dataService = DataService.instance

        Thread(Runnable {
            dataService!!.deviceDao.queryAll().forEach {device ->
                devices.add(device.address)
                if(device.display!!){
                    connectedItemAdapter?.addItem(Connected(device.address, device.name!!, true))
                }
            }
        }).start()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        btController!!.setEnableHex(sharedPreferences!!.getBoolean("hex_convert", false))
        btController!!.setEnableNotify(!sharedPreferences!!.getBoolean("cancel_notify", false))

        listener = SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences, key ->
            val enable = sharedPreferences.getBoolean(key, false)
            when(key){
                "auto_connect" ->{

                }
                "hex_convert" ->{
                    btController?.setEnableHex(enable)
                }
                "cancel_notify" ->{
                    btController?.setEnableNotify(!enable)
                }
                else ->{
                }
            }
        }

        sharedPreferences!!.registerOnSharedPreferenceChangeListener(listener)

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

        commandItemAdapter = CommandItemAdapter(commands)
        commandList = findViewById<RecyclerView>(R.id.command_list).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = commandItemAdapter
            itemAnimator = FloatItemAnimator()
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@MainActivity, R.anim.layout_animation_from_bottom)
        }

        commandSend = findViewById<TextView>(R.id.command_send).apply {
            setOnClickListener {
                val sendText: String = commandEdit!!.text.toString().trim()
                sendCommand(sendText)
            }
        }

        selectName = findViewById(R.id.select_name)
        selectAddress = findViewById(R.id.select_address)
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
                connectedAddress?.let {a->
                    val intent = Intent(this@MainActivity, DeviceActivity::class.java)
                    intent.putExtra("address", a)
                    for(connected in connecteds) {
                        if (connected.address != a) continue
                        intent.putExtra("name", connected.name)
                        break
                    }
                    val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity,
                        UtilPair.create(selectName as View, "device_name"),
                        UtilPair.create(selectAddress as View, "device_address"))
                    startActivityForResult(intent, EDIT_DEVICE, options.toBundle())
                }
            }
        }
//        selectClear = findViewById<ImageButton>(R.id.main_select_clear).apply {
//            setOnClickListener {
//                AnimateUtil.click(it, 100)
//                commands.clear()
//                handler.obtainMessage(REFRESH_COMMAND).sendToTarget()
//                Thread(Runnable {
//                    dataService!!.commandDao.queryAll().filter { command ->  command.address == connectedAddress }.forEach {c ->
//                        dataService!!.commandDao.delete(c)
//                    }
//                }).start()
//            }
//        }

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
                        commandMotion!!.transitionToState(R.id.scene_command_edit)
                    } else {
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
                this.startActivityForResult(intent, REQUEST_BLUETOOTH, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }
            else -> {
            }
        }
        return true
    }

    override fun onDestroy() {
        sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }

    /**
     * 发送蓝牙指令
     * @param command 指令
     */
    private fun sendCommand(command: String) {
        val bytes = command.toByteArray()
        commandEdit!!.setText("")
        btController!!.writeBuffer(connectedAddress, bytes, object : OnWriteCallback {
            override fun onSuccess() {
                addData(command, "Send", true)
            }

            override fun onFailed(state: Int) {
                ToastUtil.showShort(baseContext, "发送失败")
                addData(command, "Send", false)
            }
        })
    }

    /**
     * 添加指令数据
     * @param command 指令
     * @param type 类型
     * @param success 是否成功
     */
    private fun addData(
        command: String,
        type: String,
        success: Boolean
    ) {
        Thread(Runnable {
            if (CalendarUtil.getDate() - lastTime > 180000L) {
                val time = Command(text = "#", type = "System", time = CalendarUtil.getDate(), isSuccess = true, address = connectedAddress)
                dataService!!.commandDao.insertAll(time)
                handler.obtainMessage(ADD_COMMAND, time).sendToTarget()
            }
            lastTime = CalendarUtil.getDate()
            val data = Command(text = command, type = type, time = CalendarUtil.getDate(), isSuccess = success, address = connectedAddress)
            dataService!!.commandDao.insertAll(data)
            handler.obtainMessage(ADD_COMMAND, data).sendToTarget()
        }).start()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        if(connectedMotion!!.currentState != R.id.connected_scene_start){
            connectedMotion!!.transitionToState(R.id.connected_scene_start)
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

    private fun getUUID(name: String): Array<String>{
        val uuid: Array<String> = Array(2) {""}
        when (name) {
            "Ai-Thinker" -> {
                uuid[0] = "00010203-0405-0607-0809-0a0b0c0d1910"
                uuid[1] = "00010203-0405-0607-0809-0a0b0c0d2b10"
            }
            "MLT-BT05", "HC-42", "JDY-24M" -> {
                uuid[0] = "0000ffe0-0000-1000-8000-00805f9b34fb"
                uuid[1] = "0000ffe1-0000-1000-8000-00805f9b34fb"
            }
            else -> {
                uuid[0] = ""
                uuid[1] = ""
            }
        }
        return uuid
    }

}