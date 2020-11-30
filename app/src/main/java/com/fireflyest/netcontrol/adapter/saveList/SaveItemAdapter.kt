package com.fireflyest.netcontrol.adapter.saveList

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.ScanActivity
import com.fireflyest.netcontrol.bean.Device


class SaveItemAdapter(
    private var devices: MutableList<Device>?,
    private var handler: Handler?
) : RecyclerView.Adapter<SaveItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var name: TextView? = itemView.findViewById(R.id.item_save_name)
        var type: ImageView? = itemView.findViewById(R.id.item_save_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_save, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return devices!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device :Device = devices!![position]
        holder.name?.text = device.name
        holder.itemView.setOnClickListener{
            handler!!.obtainMessage(ScanActivity.EDIT_SAVE, device).sendToTarget()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}