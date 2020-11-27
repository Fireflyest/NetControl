package com.fireflyest.netcontrol.adapter.connectedList

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.MainActivity
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.bean.Connected


class ConnectedItemAdapter(
    private var connecteds: MutableList<Connected>?,
    private var handler: Handler?
) : RecyclerView.Adapter<ConnectedItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var name: TextView? = itemView.findViewById(R.id.item_connected_name)
        var address: TextView? = itemView.findViewById(R.id.item_connected_address)
        var angle: ImageView? = itemView.findViewById(R.id.item_connected_angle)
    }

    fun addItem(connected: Connected) {
        if(connecteds?.size == 1 && connecteds!![0].name == "暂无连接"){
            connecteds?.removeAt(0)
            notifyItemRemoved(0)
        }
        connecteds?.add(0, connected)
        notifyItemInserted(0)
    }

    fun removeItem(position: Int){
        connecteds?.removeAt(position)
        notifyItemRemoved(position)
        if(connecteds?.size == 0){
            addItem(Connected("", "暂无连接", false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_connected, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return connecteds!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val connected :Connected = connecteds!![position]
        if("暂无连接" == connected.name){
            holder.itemView.setBackgroundResource(R.drawable.round_background)
        }else{
            holder.itemView.setBackgroundResource(R.drawable.round_white)
            holder.itemView.setOnClickListener {
                handler!!.obtainMessage(MainActivity.SELECT_BLUETOOTH, connected).sendToTarget()
            }
        }
        holder.angle?.visibility = if(connected.save) View.VISIBLE else View.GONE
        holder.name?.text = connected.name
        holder.address?.text = connected.address
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}