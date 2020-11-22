package com.fireflyest.netcontrol.util

import android.animation.Animator

import android.animation.AnimatorListenerAdapter
import android.view.View


object AnimateUtil {
    fun show(
        view: View,
        duration: Long,
        delay: Long,
        alpha: Float,
        scale: Boolean
    ) {
        val s = if (scale) 1.1f else 1f
        view.visibility = View.VISIBLE
        view.animate()
            .setStartDelay(delay)
            .alpha(alpha)
            .scaleX(s)
            .scaleY(s)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(duration / 2)
                        .setListener(null)
                }
            })
    }

    fun show(
        view: View,
        duration: Long,
        delay: Long,
        alpha: Float
    ) {
        show(view, duration, delay, alpha, false)
    }

    fun show(view: View, duration: Long, delay: Long) {
        show(view, duration, delay, 1f)
    }

    fun hide(view: View, duration: Long, delay: Long) {
        view.animate()
            .setStartDelay(delay)
            .alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
    }

    fun click(view: View, duration: Long) {
        view.animate()
            .setDuration(duration)
            .scaleX(0.6f)
            .scaleY(0.6f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.animate()
                        .setDuration(duration)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setListener(null)
                }
            })
    }

    fun alpha(view: View, duration: Long, alpha: Float) {
        view.animate()
            .alpha(alpha)
            .setDuration(duration)
            .setListener(null)
    }

    fun expand(view: View, duration: Long) {
        view.animate()
            .setDuration(duration)
            .scaleX(2f)
            .scaleY(2f)
            .setListener(null)
    }

    fun lessen(view: View, duration: Long) {
        view.animate()
            .setDuration(duration)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setListener(null)
    }
}