package com.fireflyest.netcontrol.adapter.bluetoothList

import android.bluetooth.BluetoothClass
import android.content.res.Resources
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.bean.Bluetooth


class BluetoothItemAdapter(
    private var bluetooths: MutableList<Bluetooth>?,
    private var handler: Handler?
) : RecyclerView.Adapter<BluetoothItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var resource: Resources? = itemView.resources
        var name: TextView? = itemView.findViewById(R.id.item_bluetooth_name)
        var address: TextView? = itemView.findViewById(R.id.item_bluetooth_address)
        var rssi: ProgressBar? = itemView.findViewById(R.id.item_bluetooth_rssi)
        var connect: TextView? = itemView.findViewById(R.id.item_bluetooth_connect)
        var type: ImageView? = itemView.findViewById(R.id.item_bluetooth_type)
    }

    fun addItem(bluetooth: Bluetooth?) {
        if (bluetooth != null) {
            bluetooths?.add(bluetooth)
        }
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
            BluetoothClass.Device.Major.PHONE -> holder.type!!.setImageResource(R.drawable.ic_phone)
            BluetoothClass.Device.Major.COMPUTER -> holder.type!!.setImageResource(R.drawable.ic_computer)
            BluetoothClass.Device.Major.NETWORKING -> holder.type!!.setImageResource(R.drawable.ic_networking)
            BluetoothClass.Device.Major.WEARABLE -> holder.type!!.setImageResource(R.drawable.ic_wearable)
            BluetoothClass.Device.Major.HEALTH -> holder.type!!.setImageResource(R.drawable.ic_health)
            BluetoothClass.Device.Major.TOY -> holder.type!!.setImageResource(R.drawable.ic_toy)
            BluetoothClass.Device.Major.AUDIO_VIDEO -> holder.type!!.setImageResource(R.drawable.ic_audio)
            BluetoothClass.Device.Major.PERIPHERAL -> holder.type!!.setImageResource(R.drawable.ic_peripheral)
            0x1F00 -> if (bluetooth.name.contains("Mi Smart Band")) {
                holder.type!!.setImageResource(R.drawable.ic_wearable)
            } else {
                holder.type!!.setImageResource(R.drawable.ic_bluetooth)
            }
            else -> holder.type!!.setImageResource(R.drawable.ic_bluetooth)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}