package com.fireflyest.netcontrol.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.preference.PreferenceManager
import com.fireflyest.netcontrol.MainActivity
import com.fireflyest.netcontrol.QuickActivity
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.adapter.quickList.QuickItemAdapter
import com.fireflyest.netcontrol.bean.Quick
import com.fireflyest.netcontrol.data.DataService
import com.fireflyest.netcontrol.layout.ColorfulBar
import com.fireflyest.netcontrol.layout.FlexButton
import com.fireflyest.netcontrol.layout.WhiteBar
import com.fireflyest.netcontrol.net.BleController
import com.fireflyest.netcontrol.net.BtController
import com.fireflyest.netcontrol.net.BtManager
import com.fireflyest.netcontrol.net.callback.OnWriteCallback
import com.fireflyest.netcontrol.util.AnimateUtil
import com.fireflyest.netcontrol.util.ScaleUtil
import com.fireflyest.netcontrol.util.ToastUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.lang.Thread.sleep


class SheetFragment : BottomSheetDialogFragment()
    ,QuickItemAdapter.ItemClickedCallBack ,ColorfulBar.ProgressChangeListener, WhiteBar.ProgressChangeListener{

//    private val quicks: MutableList<Quick> = ArrayList()
//    private var quickItemAdapter: QuickItemAdapter? = null
    private var dataService: DataService? = null

    private var gridLayout: GridLayout? = null
    private var firstText: TextView? = null
    private var secondText: TextView? = null

    private var lastFirst: Int = 0
    private var lastSecond: Int = 0

    private var delay: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_more, container, false)
    }

    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null
    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            //禁止拖拽，
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                mBottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }


    override fun onStart() {

        val dialog: Dialog? = dialog

        if (dialog != null) {
            val bottomSheet: View = dialog.findViewById(R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val mView = view
        mView!!.post {
            val parent = mView.parent as View
            val params =
                parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            mBottomSheetBehavior = behavior as BottomSheetBehavior<View>?
            mBottomSheetBehavior!!.addBottomSheetCallback(mBottomSheetBehaviorCallback)
            parent.setBackgroundColor(Color.TRANSPARENT)
        }

        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataService = DataService.instance

        view.findViewById<ColorfulBar>(R.id.quick_first_bar).apply {
            setListener(this@SheetFragment)
        }

        view.findViewById<WhiteBar>(R.id.quick_second_bar).apply {
            max = 255
            setListener(this@SheetFragment)
        }

        firstText = view.findViewById(R.id.quick_first_text)
        secondText = view.findViewById(R.id.quick_second_text)

        view.findViewById<ImageButton>(R.id.quick_add).apply {
            setOnClickListener {
                startActivity(Intent(this@SheetFragment.context, QuickActivity::class.java))
            }
        }

        view.findViewById<ImageButton>(R.id.quick_down).apply {
            setOnClickListener{
                AnimateUtil.click(it, 100)
                dismiss()
            }
        }

        gridLayout = view.findViewById(R.id.quick_button_grid)
        for(num :Int in 0 until gridLayout!!.childCount){
            Thread(Runnable {
                 val quick = dataService!!.quickDao.loadByNum(num)

                getChild(num).apply {
                    setOnClickListener {child ->
                        AnimateUtil.click(child, 100)
                        Thread(Runnable {
                            quick.command?.let { it1 -> sendCommand(it1) }
                        }).start()
                    }

                    setType(quick.type)
                    setColor(Color.parseColor("#" +
                            intToHex(quick.colorR) +
                            intToHex(quick.colorG) +
                            intToHex(quick.colorB)
                    ))
                    text = quick.display
                    command = quick.command

                }
            }).start()

        }

        Thread(Runnable {
            while (true){
                if(delay > 0)delay --
                sleep(500)
            }
        }).start()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun getChild(num: Int): FlexButton {
        return (gridLayout!!.getChildAt(num) as FlexButton)
    }

    private fun intToHex(color: Int): String{
        var temp = Integer.toHexString(color)
        if(temp.length == 1){
            temp = "0$temp"
        }
        return temp
    }

    override fun onLongClicked(quick: Quick) {
        dismiss()
//        quick.command?.let { this.sendCommand(it) }
    }

    override fun onClicked(quick: Quick) {
//        quicks.remove(quick)
//        quickItemAdapter!!.notifyDataSetChanged()
    }

    private fun sendCommand(command: String) {
        if(delay != 0)return
        delay = 1
        var data = command.toByteArray()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val enableHex = sharedPreferences!!.getBoolean("hex_convert", false)
        if(enableHex){
            val regex = Regex("^[a-f0-9A-F]+$")
            if(command.matches(regex)){
                data = ScaleUtil.getHexBytes(command)
                Log.e(MainActivity.TAG, "指令转换进制")
            }else{
                Log.e(MainActivity.TAG, "指令转换进制失败")
            }
        }
        Log.e(MainActivity.TAG, "数据字节 -> ${data.contentToString()}")

        val btController = BtManager.getBtController()

        btController?.writeBuffer(BleController.getInstance().lastConnect, data, null)
    }

    override fun onProgressChange(progress: Int) {
        val level = (progress/10)
        if(lastFirst != level){
            firstText!!.text = "$level"
            sendCommand("${300+level}")
            lastFirst = level
        }
    }

    override fun onBarProgressChange(progress: Int) {
        secondText!!.text = "$progress"
        if(lastSecond != progress){
            sendCommand("$progress")
            lastSecond = progress
        }
    }

}