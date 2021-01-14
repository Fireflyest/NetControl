package com.fireflyest.netcontrol

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.GridLayout
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.data.SettingData
import com.fireflyest.netcontrol.layout.FlexButton
import com.fireflyest.netcontrol.util.StatusBarUtil

class QuickActivity : AppCompatActivity() , OnSeekBarChangeListener {

    private var gridLayout:GridLayout? = null
    private var typePicker:NumberPicker? = null
    private var commandEdit:EditText? = null
    private var displayEdit:EditText? = null
    private var colorRBar:SeekBar? = null
    private var colorGBar:SeekBar? = null
    private var colorBBar:SeekBar? = null

    private var dataService: DataService? = null

    private var selectNum: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick)
        if(SettingData.instance.themeNight){
            setTheme(R.style.AppDarkTheme)
        }else{
            setTheme(R.style.AppLightTheme)
        }

        dataService = DataService.instance

        this.initView()
    }

    private fun initView(){
        StatusBarUtil.StatusBarLightMode(this)

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_sub).apply { title="" }
        this.setSupportActionBar(toolbar)

        commandEdit = findViewById(R.id.quick_edit_command)
        displayEdit = findViewById(R.id.quick_edit_display)

        colorRBar = findViewById<SeekBar>(R.id.quick_edit_color_R).apply {
            setOnSeekBarChangeListener(this@QuickActivity)
        }
        colorGBar = findViewById<SeekBar>(R.id.quick_edit_color_G).apply {
            setOnSeekBarChangeListener(this@QuickActivity)
        }
        colorBBar = findViewById<SeekBar>(R.id.quick_edit_color_B).apply {
            setOnSeekBarChangeListener(this@QuickActivity)
        }

        gridLayout = findViewById(R.id.quick_button_grid)

        for(num :Int in 0 until gridLayout!!.childCount){
            getChild(num).apply {
                setOnClickListener {
                    cancelSelectChild(selectNum)
                    selectChild(num)
                }
                Thread(Runnable {
                    val quick = dataService!!.quickDao.loadByNum(num)
                    setType(quick.type)
                    setColor(Color.parseColor("#" +
                            intToHex(quick.colorR) +
                            intToHex(quick.colorG) +
                            intToHex(quick.colorB)
                    ))
                    text = quick.display
                    command = quick.command
                }).start()
            }
        }

        typePicker = findViewById<NumberPicker>(R.id.quick_edit_type).apply {
            maxValue = 5
            minValue = 0
            setOnValueChangedListener { _, _, newVal ->
                getChild(selectNum).setType(newVal)
                Thread(Runnable {
                    val quick = dataService!!.quickDao.loadByNum(selectNum)
                    quick.type = newVal
                    dataService!!.quickDao.update(quick)
                }).start()
            }
        }

        selectChild(selectNum)

    }

    override fun onBackPressed() {
        cancelSelectChild(selectNum)
        super.onBackPressed()
    }

    private fun selectChild(num: Int){
        selectNum = num
        getChild(num).setSelect(true)
        Thread(Runnable {
            val quick = dataService!!.quickDao.loadByNum(num)
            quick.apply {
                commandEdit!!.setText(quick.command)
                displayEdit!!.setText(quick.display)
                typePicker!!.value = quick.type
                colorRBar!!.progress = quick.colorR
                colorGBar!!.progress = quick.colorG
                colorBBar!!.progress = quick.colorB
            }
        }).start()
    }

    private fun cancelSelectChild(num: Int){
        Thread(Runnable {
            val quick = dataService!!.quickDao.loadByNum(num)
            quick.apply {
                command = commandEdit!!.text.toString()
                display = displayEdit!!.text.toString()
            }
            dataService!!.quickDao.update(quick)
        }).start()
        getChild(selectNum).setSelect(false)
    }

    private fun getChild(num: Int): FlexButton{
        return (gridLayout!!.getChildAt(num) as FlexButton)
    }

    private fun intToHex(color: Int): String{
        var temp = Integer.toHexString(color)
        if(temp.length == 1){
            temp = "0$temp"
        }
        return temp
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                cancelSelectChild(selectNum)
                finish()
            }
            else -> {}
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        Thread(Runnable {
            val quick = dataService!!.quickDao.loadByNum(selectNum)
            when(seekBar!!.id){
                R.id.quick_edit_color_R -> {
                    quick.colorR = progress
                }
                R.id.quick_edit_color_G -> {
                    quick.colorG = progress
                }
                R.id.quick_edit_color_B -> {
                    quick.colorB = progress
                }
            }
            dataService!!.quickDao.update(quick)
            getChild(selectNum).setColor(Color.parseColor("#" +
                    intToHex(quick.colorR) +
                    intToHex(quick.colorG) +
                    intToHex(quick.colorB)
            ))
        }).start()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
}