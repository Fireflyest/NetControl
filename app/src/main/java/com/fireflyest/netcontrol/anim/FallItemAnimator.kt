package com.fireflyest.netcontrol.anim

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fireflyest.netcontrol.R


class FallItemAnimator : SimpleItemAnimator(){
    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        val animation = AnimationUtils.loadAnimation(
            holder!!.itemView.context,
            R.anim.item_animation_fall_down
        )
        holder.itemView.startAnimation(animation)
        return true
    }

    override fun runPendingAnimations() {
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        return false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromLeft: Int,
        fromTop: Int,
        toLeft: Int,
        toTop: Int
    ): Boolean {
        return false
    }

    override fun isRunning(): Boolean {
        return false
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun endAnimations() {
    }

}
