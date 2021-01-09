package com.fireflyest.netcontrol.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ProgressBar
import com.fireflyest.netcontrol.R
import com.fireflyest.netcontrol.util.DpOrPxUtil
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin


class LightDish(context: Context, attrs: AttributeSet) : ProgressBar(context, attrs) {

    interface ProgressChangeListener{
        fun onKelvinChange(progress: Int)
    }

    var mListener: ProgressChangeListener? = null

    private var showText: Boolean = false
    private var textPosition: Int = 0
    private var barWidth: Float = 0F

    private var defaultSize: Int = DpOrPxUtil.dip2px(context, 200F)

    private var rectF: RectF = RectF()
    private var angle = 0F
    private var pX = 0.0
    private var pY = 0.0

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x232323
    }

//    private val linearLightShader = RadialGradient(defaultSize.toFloat()/2, defaultSize.toFloat(), 600F,
//        Color.parseColor("#77000000"), Color.parseColor("#77FFFFFF"), Shader.TileMode.CLAMP)
//    private val lightPaint = Paint().apply {
//        style = Paint.Style.STROKE
//        strokeCap = Paint.Cap.ROUND
//        isAntiAlias = true
//        shader = linearLightShader
//    }

    private val linearShader = LinearGradient(0F, 0F, 600F, 0F,
        Color.parseColor("#887ed6df"), Color.parseColor("#88f6e58d"), Shader.TileMode.CLAMP)
    private val barPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        shader = linearShader
    }

    private val progressPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        isAntiAlias = true
        color = Color.parseColor("#55FFFFFF")
    }

    private val luminancePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        color = Color.parseColor("#55FFFFFF")
        strokeWidth = DpOrPxUtil.dip2px(context, 50F).toFloat()
    }

//    private val linePaint = Paint().apply {
//        style = Paint.Style.STROKE
//        strokeCap = Paint.Cap.ROUND
//        isAntiAlias = true
//        color = Color.parseColor("#88FFFFFF")
//        strokeWidth = DpOrPxUtil.dip2px(context, 1F).toFloat()
//    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LightDish,
            0, 0).apply {

            try {
                showText = getBoolean(R.styleable.LightDish_showText, false)
                textPosition = getInteger(R.styleable.LightDish_labelPosition, 0)
                barWidth = getDimension(R.styleable.LightDish_strokeWidth,
                    DpOrPxUtil.dip2px(context, 1F).toFloat()
                )
            } finally {
                recycle()
            }
        }

//        lightPaint.strokeWidth = barWidth
        barPaint.strokeWidth = barWidth
        progressPaint.strokeWidth = barWidth

    }

    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)

        canvas?.apply {
            // progress 色温 对应角度
            // secondaryProgress 亮度 对应径距

            // 将100级转化为角度180级的角度 并旋转180到1、2象限
            angle = progress.toFloat()*180/100 + 180
            // 画出背景
            drawArc(rectF, 180F, 180F, false, barPaint)
//            drawArc(rectF, 180F, 180F, false, lightPaint)
            // 角度线
//            drawArc(rectF, angle-0.5F, 1F, false, progressPaint)

            // 先转化为弧度 三角函数算出位置
            pX = width/2 + cos(angle*Math.PI/180) * (secondaryProgress*barWidth/100+barWidth/4)
            pY = height/2 + sin(angle*Math.PI/180) * (secondaryProgress*barWidth/100+barWidth/4)
            // 点
            drawPoint(pX.toFloat(), pY.toFloat(), luminancePaint)

//            drawLine(width/2F, height/2F, width/2F, 0F, linePaint)
//            for(i in 0..10){progress
//                drawLine(width/2F+20-i*2, height/2F*i/10 , width/2F-20+i*2, height/2F*i/10, linePaint)
//            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try for a width based on our minimum
        if(suggestedMinimumWidth != 0)defaultSize = suggestedMinimumWidth
        val minWidth: Int = paddingLeft + paddingRight + defaultSize
        val width: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        val height: Int = resolveSizeAndState(
            MeasureSpec.getSize(width),
            heightMeasureSpec,
            0
        )

        setMeasuredDimension(width, height)

        if (width >= barWidth * 2 && height >= barWidth * 2 ) { //这里简单限制了圆弧的最大宽度
            rectF.set(barWidth / 2, barWidth / 2, width - barWidth / 2, height - barWidth / 2)
        }

    }

    private val listener =  object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 变化后的坐标
            val tX = pX - distanceX
            val tY = pY - distanceY
            // 计算坐标相对圆心的角度
            var angle = atan((height/2 - tY)/(width/2 - tX)) * 180 / Math.PI
            if(abs(angle) < 10){
                angle = if(angle > 0){
                    10.0
                }else{
                    -10.0
                }
            }
            // 角度换算成比例
            val ratio = angle / 180 * 100
            progress = if(ratio > 0){
                ratio.toInt()
            }else{
                (100 + ratio).toInt()
            }

            mListener?.onKelvinChange(progress)

//            Log.e("Ev", "y ${(height/2 - tY)}   x ${(width/2 - tX)}")
//            Log.e("Ev", "计算弧度角 ${atan((height/2 - tY)/(width/2 - tX))}")
//            Log.e("Ev", "角 ${atan((height/2 - tY)/(width/2 - tX)) * 180 / Math.PI}")
//            Log.e("Ev", "比例 ${atan((height/2 - tY)/(width/2 - tX)) * 180 / Math.PI / 180 * 100}")
//            Log.e("Ev", "progress $progress")
//            Log.e("Ev", "###################################################")

            return super.onScroll(e1, e2, distanceX, distanceY)
        }

    }

    private val detector: GestureDetector = GestureDetector(context, listener)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return detector.onTouchEvent(event).let {
            true
//            if (!result) {
//                if (event?.action == MotionEvent.ACTION_UP) {
//                    progress ++
//                    true
//                } else false
//            } else true
        }
    }

    fun isShowText(): Boolean {
        return showText
    }

    fun setShowText(showText: Boolean) {
        this.showText = showText
        invalidate()
        requestLayout()
    }

}