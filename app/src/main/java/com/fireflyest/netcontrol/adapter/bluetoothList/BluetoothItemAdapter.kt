package com.fireflyest.netcontrol.adapter.bluetoothList

import android.bluetooth.BluetoothClass.Device.Major
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.ScanActivity
import com.fireflyest.netcontrol.bean.Bluetooth
import com.fireflyest.netcontrol.util.AnimateUtil


class BluetoothItemAdapter(
    private var bluetooths: MutableList<Bluetooth>?,
    private var handler: Handler?
) : RecyclerView.Adapter<BluetoothItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var name: TextView? = itemView.findViewById(R.id.item_bluetooth_name)
        var address: TextView? = itemView.findViewById(R.id.item_bluetooth_address)
        var rssi: ProgressBar? = itemView.findViewById(R.id.item_bluetooth_rssi)
        var connect: TextView? = itemView.findViewById(R.id.item_bluetooth_connect)
        var type: ImageView? = itemView.findViewById(R.id.item_bluetooth_type)
    }

    fun addItem(bluetooth: Bluetooth) {
        bluetooths?.add(bluetooth)
        notifyItemInserted(this.itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_bluetooth, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bluetooths!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bluetooth :Bluetooth = bluetooths!![position]
        holder.name?.text = bluetooth.name
        holder.address?.text = bluetooth.address
        holder.rssi?.progress = 130 + bluetooth.rssi
        when (bluetooth.type) {
            Major.PHONE -> holder.type!!.setImageResource(R.drawable.ic_phone)
            Major.COMPUTER -> holder.type!!.setImageResource(R.drawable.ic_computer)
            Major.NETWORKING -> holder.type!!.setImageResource(R.drawable.ic_networking)
            Major.WEARABLE -> holder.type!!.setImageResource(R.drawable.ic_wearable)
            Major.HEALTH -> holder.type!!.setImageResource(R.drawable.ic_health)
            Major.TOY -> holder.type!!.setImageResource(R.drawable.ic_toy)
            Major.AUDIO_VIDEO -> holder.type!!.setImageResource(R.drawable.ic_audio)
            Major.PERIPHERAL -> holder.type!!.setImageResource(R.drawable.ic_peripheral)
            0x1F00 -> if (bluetooth.name.contains("Band")) {
                holder.type!!.setImageResource(R.drawable.ic_wearable)
            } else {
                holder.type!!.setImageResource(R.drawable.ic_bluetooth)
            }
            else -> holder.type!!.setImageResource(R.drawable.ic_bluetooth)
        }
        holder.connect!!.setOnClickListener { v ->
            val message: Message = handler!!.obtainMessage(ScanActivity.START_CONNECTION)
            message.obj = bluetooth
            message.sendToTarget()
            AnimateUtil.hide(v, 300, 0)
        }
        holder.itemView.setOnClickListener {
            if(holder.connect!!.visibility == View.GONE){
                AnimateUtil.show(holder.connect!!, 300, 0, 1F, true)
                AnimateUtil.hide(holder.rssi!!, 300, 0)
            }else {
                AnimateUtil.hide(holder.connect!!, 300, 0)
                AnimateUtil.show(holder.rssi!!, 300, 0)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}