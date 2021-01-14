package com.fireflyest.netcontrol.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ProgressBar;

import com.fireflyest.netcontrol.util.DpOrPxUtil;

public class WhiteBar extends ProgressBar {

    final private Paint barPaint = new Paint();

    final private Paint progressPaint = new Paint();

    final static private float defaultStrokeWidth = 10F;

    private static float joinWidth;
    private static float levelWidth;
    private static final int maxLevel = 255;

    public interface ProgressChangeListener{
        void onBarProgressChange(int progress);
    }

    private ProgressChangeListener listener;

    public WhiteBar(Context context) {
        super(context);
    }

    public WhiteBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public WhiteBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.drawRect(
                getWidth() - joinWidth/2-1,
                joinWidth/2+1,
                joinWidth/2+1,
                getHeight() - joinWidth/2-1,
                barPaint
        );

        float level = ((float) getProgress())/getMax()*maxLevel;
        levelWidth = (float) getWidth()/maxLevel;

        canvas.drawRect(
                level*levelWidth - joinWidth/2-12,
                joinWidth/2+12,
                joinWidth/2+12,
                getHeight() - joinWidth/2-12,
                progressPaint
        );
    }

    private final GestureDetector detector = new GestureDetector(this.getContext(), new WhiteBar.TouchListener());

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    private void init(Context context, AttributeSet attrs){
        joinWidth = DpOrPxUtil.dip2px(context, defaultStrokeWidth);

        barPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        barPaint.setStrokeJoin(Paint.Join.ROUND);
        barPaint.setAntiAlias(true);
        barPaint.setStrokeWidth(joinWidth);
        barPaint.setColor(Color.parseColor("#88E0E0E0"));

        progressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        progressPaint.setStrokeJoin(Paint.Join.ROUND);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(joinWidth);
        progressPaint.setColor(Color.parseColor("#AAFFFFFF"));

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
            if(listener != null)listener.onBarProgressChange(getProgress());
            return true;
        }
    }

    public void setListener(ProgressChangeListener listener) {
        this.listener = listener;
    }
}
