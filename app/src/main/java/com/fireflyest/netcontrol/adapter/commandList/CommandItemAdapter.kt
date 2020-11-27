package com.fireflyest.netcontrol.adapter.commandList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.bean.Command
import com.fireflyest.netcontrol.util.CalendarUtil


class CommandItemAdapter (private val commands: MutableList<Command>) :
    RecyclerView.Adapter<CommandItemAdapter.ViewHolder>() {

    private val transition: Transition = AutoTransition().setDuration(200)

    class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        var receive: TextView = view.findViewById(R.id.item_command_receive)
        var send: TextView = view.findViewById(R.id.item_command_send)
        var receiveBox: ConstraintLayout = view.findViewById(R.id.item_command_receive_box)
        var sendBox: ConstraintLayout = view.findViewById(R.id.item_command_send_box)
        var right: ConstraintLayout = view.findViewById(R.id.item_command_right)
        var sendTime: TextView = view.findViewById(R.id.item_command_send_time)
        var receiveTime: TextView = view.findViewById(R.id.item_command_receive_time)
        var system: TextView = view.findViewById(R.id.item_command_system)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_command, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val command = commands[position]
        when (command.type) {
            "Receive" -> {
                holder.receive.text = String.format("%s", command.text)
                holder.receiveTime.text = String.format("接收时间:\n%s", CalendarUtil.convertTime(command.time))
                holder.receiveBox.visibility = View.VISIBLE
            }
            "Send" -> {
                holder.send.text = String.format("%s", command.text)
                holder.sendTime.text = String.format("发送时间:\n%s", CalendarUtil.convertTime(command.time))
                holder.sendBox.visibility = View.VISIBLE
                if (!command.isSuccess) holder.right.setBackgroundResource(R.drawable.round_red)
            }
            "System" -> {
                var system = command.text
                if ("#" == system) {
                    val time = CalendarUtil.getDate() - command.time
                    system = when {
                        time < 86400000 -> {
                            CalendarUtil.convertTime(command.time).substring(6)
                        }
                        time < 259200000 -> {
                            String.format("%s小时前", CalendarUtil.convertHour(command.time))
                        }
                        time < 604800000 -> {
                            String.format("%s天前", CalendarUtil.convertDay(command.time))
                        }
                        else -> {
                            "大于一周前"
                        }
                    }
                }
                holder.system.text = system
                holder.system.visibility = View.VISIBLE
            }
            else -> {
            }
        }
        val receiveConstraintSet = ConstraintSet()
        val sendConstraintSet = ConstraintSet()
        receiveConstraintSet.clone(holder.receiveBox)
        sendConstraintSet.clone(holder.sendBox)

        holder.receiveBox.setOnClickListener {
            TransitionManager.beginDelayedTransition(holder.receiveBox, transition)
            if (holder.receiveTime.visibility == View.GONE) {
                holder.receiveTime.visibility = View.VISIBLE
            } else {
                holder.receiveTime.visibility = View.GONE
            }
            receiveConstraintSet.applyTo(holder.receiveBox)
        }

        holder.sendBox.setOnClickListener {
            TransitionManager.beginDelayedTransition(holder.sendBox, transition)
            if (holder.sendTime.visibility == View.GONE) {
                holder.sendTime.visibility = View.VISIBLE
            } else {
                holder.sendTime.visibility = View.GONE
            }
            sendConstraintSet.applyTo(holder.sendBox)
        }
    }

    override fun getItemCount(): Int {
        return commands.size
    }

    fun addItem(command: Command) {
        commands.add(command)
        notifyItemInserted(commands.size)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}