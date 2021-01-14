package com.fireflyest.netcontrol.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.fireflyest.netcontrol.R;
import com.fireflyest.netcontrol.util.DpOrPxUtil;

public class ColorfulBar extends ProgressBar {

    final private Paint barPaint = new Paint();

    final private Paint progressPaint = new Paint();

    final static private float defaultStrokeWidth = 10F;

    private static float joinWidth;
    private static float levelWidth;
    private float x0;
    private float y0;
    private float x1;
    private float y1;
    private static final int maxLevel = 10;

    public interface ProgressChangeListener{
        void onProgressChange(int progress);
    }

    private ProgressChangeListener listener;

    public ColorfulBar(Context context) {
        super(context);
    }

    public ColorfulBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public ColorfulBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.drawRect(
                joinWidth/2+1,
                joinWidth/2+1,
                getWidth() - joinWidth/2-1,
                getHeight() - joinWidth/2-1,
                barPaint
        );

        float level = (float) getProgress()/getMax()*maxLevel;
        levelWidth = (float) getWidth()/maxLevel;

        canvas.drawRect(
                level*levelWidth - joinWidth/3,
                levelWidth/3,
                level*levelWidth + joinWidth/3,
                getHeight() - levelWidth/3,
                progressPaint
                );
    }

    private final GestureDetector detector = new GestureDetector(this.getContext(), new TouchListener());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    private void init(Context context, @Nullable AttributeSet attrs){
        joinWidth = DpOrPxUtil.dip2px(context, defaultStrokeWidth);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorfulBar, 0, 0);

        joinWidth = typedArray.getDimension(R.styleable.ColorfulBar_joinWidth, joinWidth);
        x0 = typedArray.getDimension(R.styleable.ColorfulBar_x0, 0);
        y0 = typedArray.getDimension(R.styleable.ColorfulBar_y0, 0);
        x1 = typedArray.getDimension(R.styleable.ColorfulBar_x1, 0);
        y1 = typedArray.getDimension(R.styleable.ColorfulBar_y1, 0);

        typedArray.recycle();

        LinearGradient linearShader = new LinearGradient(x0, y0, x1, y1,
                Color.parseColor("#887ed6df"),
                Color.parseColor("#88f6e58d"),
                Shader.TileMode.CLAMP);

        barPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        barPaint.setStrokeJoin(Paint.Join.ROUND);
        barPaint.setAntiAlias(true);
        barPaint.setShader(linearShader);
        barPaint.setStrokeWidth(joinWidth);

        progressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        progressPaint.setStrokeJoin(Paint.Join.ROUND);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(joinWidth);
        progressPaint.setColor(Color.parseColor("#88FFFFFF"));
    }

    class TouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int changeLevel = (int)(distanceX/levelWidth/maxLevel*getMax());
            setProgress(getProgress() - changeLevel);
            if(listener != null)listener.onProgressChange(getProgress());
            return true;
        }
    }

    public void setListener(ProgressChangeListener listener) {
        this.listener = listener;
    }
}
