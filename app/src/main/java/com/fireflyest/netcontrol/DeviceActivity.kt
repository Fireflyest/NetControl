package com.fireflyest.netcontrol

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.adapter.charsList.CharsItemAdapter
import com.fireflyest.netcontrol.anim.FloatItemAnimator
import com.fireflyest.netcontrol.bean.Chars
import com.fireflyest.netcontrol.bean.Device
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.net.BtManager
import com.fireflyest.netcontrol.util.AnimateUtil
import com.fireflyest.netcontrol.util.CalendarUtil
import com.fireflyest.netcontrol.util.StatusBarUtil
import com.fireflyest.netcontrol.util.ToastUtil


class DeviceActivity : AppCompatActivity() {

    private var dataService: DataService? = null

    private var deviceName: TextView? = null
    private var deviceAddress: TextView? = null
    private var deviceTime: TextView? = null
    private var charsList: RecyclerView? = null
    private var deviceMotion: MotionLayout? = null
    private var deviceRename: ImageButton? = null
    private var deviceDelete: ImageButton? = null
    private var deviceDone: ImageButton? = null
    private var deviceCancel: ImageButton? = null
    private var renameEdit: EditText? = null
    private var deviceDisplay: CheckBox? = null


    private var charss: MutableList<Chars> = ArrayList()

    private var charsItemAdapter: CharsItemAdapter? = null

    private var selectAddress: String? = null
    private var selectName: String? = null

    private var device: Device? =null

    companion object{
        const val UPDATE_DATA = 1
        const val SELECT_SERVICE = 2
    }

    private val handler = Handler(Handler.Callback { msg ->
        when(msg.what){
            UPDATE_DATA ->{
                if(device!!.display!!) deviceDisplay!!.isChecked = true

                deviceTime!!.text = "创建于: %s".format(CalendarUtil.convertTime(device!!.time))

                refreshService()

                charsList!!.scheduleLayoutAnimation()

            }
            SELECT_SERVICE -> {
                val service = (msg.obj as Array<*>)[0] as String
                val characteristic = (msg.obj as Array<*>)[1] as String
                device?.service = service
                device?.characteristic = characteristic
                BtManager.getBtController().setCharacteristic(selectAddress, service, characteristic)
                charss.clear()
                charsItemAdapter!!.notifyDataSetChanged()
                updateDevice()
                refreshService()
                ToastUtil.showShort(this@DeviceActivity, "服务特征更新")
            }
            else->{
            }
        }
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device)

        this.initData()

        this.initView()
    }

    private fun initView(){

        StatusBarUtil.StatusBarLightMode(this)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_sub).apply { title="" }
        this.setSupportActionBar(toolbar)

        selectAddress = this.intent.extras?.getString("address")
        selectName = this.intent.extras?.getString("name")

        deviceName = findViewById<TextView>(R.id.select_name).apply {
            text = selectName
        }
        deviceAddress = findViewById<TextView>(R.id.select_address).apply {
            text = selectAddress
        }

        deviceRename = findViewById<ImageButton>(R.id.device_rename).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
                deviceMotion!!.transitionToState(R.id.device_scene_rename)
            }
        }
        deviceDelete = findViewById<ImageButton>(R.id.device_delete).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
                AlertDialog.Builder(this@DeviceActivity).apply {
                    setMessage("您确认要删除此设备吗")
                    setTitle(selectName)
                    setPositiveButton(R.string.app_done
                    ) { dialog, _ ->
                        BtManager.getBtController().closeConnect(selectAddress)
                        dialog.dismiss()
                        back(Activity.RESULT_CANCELED)
                    }
                    setNegativeButton(R.string.app_cancel
                    ) { dialog, _ ->
                        dialog.cancel()
                    }
                    create()
                    show()
                }
            }
        }
        deviceDone = findViewById<ImageButton>(R.id.device_done).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
                deviceMotion!!.transitionToState(R.id.device_scene_start)
                if(selectName == renameEdit!!.text.toString())return@setOnClickListener
                selectName = renameEdit!!.text.toString()
                deviceName!!.text  = selectName
                device!!.name = selectName
                updateDevice()
                ToastUtil.showShort(this@DeviceActivity, "设备已更名")
            }
        }
        deviceCancel = findViewById<ImageButton>(R.id.device_cancel).apply {
            setOnClickListener {
                AnimateUtil.click(it, 100)
                renameEdit!!.setText(selectName)
                deviceMotion!!.transitionToState(R.id.device_scene_start)
                ToastUtil.showShort(this@DeviceActivity, "取消更名")
            }
        }
        renameEdit = findViewById<EditText>(R.id.device_rename_edit).apply {
            setText(selectName)
        }

        deviceDisplay = findViewById<CheckBox>(R.id.device_display).apply {
            setOnCheckedChangeListener { _, isChecked ->
                device?.display = isChecked
                updateDevice()
            }
        }

        deviceTime = findViewById(R.id.device_time)

        charsItemAdapter = CharsItemAdapter(charss, handler)
        charsList = findViewById<RecyclerView>(R.id.device_char_list).apply {
            layoutManager = LinearLayoutManager(this@DeviceActivity)
            adapter = charsItemAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@DeviceActivity, R.anim.layout_animation_from_bottom)
            itemAnimator = FloatItemAnimator()
        }

        deviceMotion = findViewById(R.id.device_motion)

        Thread(Runnable {
            device = dataService!!.deviceDao.findByAddress(selectAddress!!)
            device?.let { handler.obtainMessage(UPDATE_DATA).sendToTarget() }
        }).start()

    }

    private fun initData(){
        dataService = DataService.instance
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> back(Activity.RESULT_OK)
            else -> {
            }
        }
        return true
    }

    private fun back(result: Int){
        val intent = Intent()
        intent.putExtra("name", selectName)
        intent.putExtra("address", selectAddress)
        setResult(result, intent)
        finishAfterTransition()
    }

    override fun onBackPressed() {
        back(Activity.RESULT_OK)
        super.onBackPressed()
    }

    private fun refreshService(){
        val gatt = BtManager.getBtController().getGatt(device!!.address)
        gatt?.let {
            for (service in gatt.services) {
                charss.add(
                    Chars(
                        "服务",
                        String.format("%s", service.uuid),
                        0, true, service.uuid.toString())
                )
                for (characteristic in service.characteristics) {
                    charss.add(
                        Chars("特征 0x%s".format(Integer.toHexString(characteristic.properties)),
                            "[%s]".format(getProprty(characteristic.properties)),
                            1, true, service.uuid.toString())
                    )
                    val left: String =
                        if(device?.service == service.uuid.toString() && device?.characteristic == characteristic.uuid.toString())
                            "▣" else "□"
                    charss.add(
                        Chars(left, String.format("%s", characteristic.uuid), 1, false, service.uuid.toString())
                    )
                }
                charss.add(Chars(" ", "", 4, true, ""))
            }
        }
        if(charss.size == 0){
            findViewById<TextView>(R.id.device_connect).apply {
                visibility = View.VISIBLE
                setOnClickListener{
                    back(Activity.RESULT_OK)
                }
            }
            charss.add(
                Chars(
                    "服务",
                    String.format("%s", device!!.service),
                    0, true, device!!.service!!)
            )
            charss.add(
                Chars("特征 0x%s".format(""),
                    "[%s]".format(" "),
                    1, true, device!!.service!!)
            )
            charss.add(
                Chars("▣", String.format("%s", device!!.characteristic), 1, false, device!!.service!!)
            )
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = this.currentFocus
            view?.let {
                if (isShouldHideInput(view, event)) {
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    renameEdit!!.clearFocus()
                    renameEdit!!.isSelected = false
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

    private fun updateDevice(){
        Thread(Runnable {
            device?.let { dataService!!.deviceDao.updateAll(it) }
        }).start()
    }

    private fun getProprty(p: Int): String? {
        val builder = StringBuilder()
        if (p and 0x01 != 0) builder.append("广播,")
        if (p and 0x02 != 0) builder.append("读取,")
        if (p and 0x04 != 0) builder.append("无回应写入,")
        if (p and 0x08 != 0) builder.append("写入,")
        if (p and 0x10 != 0) builder.append("反馈信息,")
        if (p and 0x20 != 0) builder.append("指示,")
        if (p and 0x40 != 0) builder.append("带签名写入,")
        if (p and 0x80 != 0) builder.append("扩展属性,")
        return builder.toString().trim(',')
    }

}