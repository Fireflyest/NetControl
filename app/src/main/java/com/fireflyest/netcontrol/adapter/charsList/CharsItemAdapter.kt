package com.fireflyest.netcontrol.adapter.charsList

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.DeviceActivity
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.bean.Chars
import com.fireflyest.netcontrol.net.BtManager


class CharsItemAdapter(private var charss: MutableList<Chars>, private var handler: Handler) :
    RecyclerView.Adapter<CharsItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var left: TextView = view.findViewById(R.id.item_chars_left)
        var right: TextView = view.findViewById(R.id.item_chars_right)
        var line: View = view.findViewById(R.id.item_chars_line)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chars, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val chars: Chars = charss[position]
        when(chars.level){
            0 -> {
                holder.itemView.setBackgroundResource(R.color.colorAccent)
            }
            1 -> {
                holder.left.setTextColor(Color.parseColor("#2F3C55"))
                holder.right.setTextColor(Color.parseColor("#2F3C55"))
                if(!chars.title){
                    holder.line.visibility = View.VISIBLE
                    holder.itemView.setOnClickListener {
                        handler.obtainMessage(DeviceActivity.SELECT_SERVICE, arrayOf(chars.belong, chars.right)).sendToTarget()
                    }
                }
            }
        }
        holder.left.text = chars.left
        holder.right.text = chars.right
    }

    override fun getItemCount(): Int {
        return charss.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}