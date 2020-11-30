package com.fireflyest.netcontrol.adapter.quickList

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fireflyest.netcontrol.MainActivity
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.bean.Quick
import com.fireflyest.netcontrol.data.DataService


class QuickItemAdapter(private var quicks: MutableList<Quick>, private var handler: Handler) :
    RecyclerView.Adapter<QuickItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var text: TextView = view.findViewById(R.id.item_quick_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_quick, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val quick: Quick = quicks[position]
        holder.text.text = quick.command
        holder.itemView.setOnClickListener{
            handler.obtainMessage(MainActivity.SEND_QUICK, quick).sendToTarget()
        }
        holder.itemView.setOnLongClickListener{
            handler.obtainMessage(MainActivity.DELETE_QUICK, quick).sendToTarget()
            Thread(Runnable {
                DataService.instance.quickDao.delete(quick)
            }).start()
            true
        }
    }

    override fun getItemCount(): Int {
        return quicks.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}