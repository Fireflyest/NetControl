package com.fireflyest.netcontrol.layout

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ProgressBar


class VerticalBar : ProgressBar {
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(context: Context?) : super(context) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        canvas.rotate(-90f)
        canvas.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }
}
