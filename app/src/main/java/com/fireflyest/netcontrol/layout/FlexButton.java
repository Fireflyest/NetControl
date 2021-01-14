package com.fireflyest.netcontrol.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.fireflyest.netcontrol.util.DpOrPxUtil;

public class FlexButton extends View {

    public static final int RECT = 0;
    public static final int LEFT_ANGLE = 1;
    public static final int RIGHT_ANGLE = 2;
    public static final int UP_ANGLE = 3;
    public static final int DOWN_ANGLE = 4;
    public static final int CYCLE = 5;

    private final int defaultColor = Color.parseColor("#FF5F5F5F");
    private final int selectColor = Color.parseColor("#88555555");

    private int number;
    private int type = RECT;
    private int color = defaultColor;
    private String text = "";
    private String command = "";
    private boolean select = false;

    private static float joinWidth;

    final private Paint paint = new Paint();
    final private Paint selectPaint = new Paint();
    final private Paint textPaint = new Paint();

    private final Path path = new Path();

    final static private float defaultStrokeWidth = 10F;

    final float defaultMargin = DpOrPxUtil.dip2px(getContext(), 8);

    public FlexButton(Context context) {
        super(context);
    }

    public FlexButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public FlexButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs){

        joinWidth = DpOrPxUtil.dip2px(context, defaultStrokeWidth);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(joinWidth);
        paint.setColor(color);

        selectPaint.setStyle(Paint.Style.STROKE);
        selectPaint.setStrokeJoin(Paint.Join.ROUND);
        selectPaint.setAntiAlias(true);
        selectPaint.setStrokeWidth(5);
        selectPaint.setColor(selectColor);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(defaultStrokeWidth);
        textPaint.setTextSize(32);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        switch (type){
            case RECT:
                canvas.drawRect(defaultMargin, defaultMargin, width-defaultMargin, height-defaultMargin, paint);
                break;
            case CYCLE:
                canvas.drawOval(defaultMargin, defaultMargin, width-defaultMargin, height-defaultMargin, paint);
                break;
            case UP_ANGLE:
                path.moveTo((int)(width/2), defaultMargin);
                path.lineTo(defaultMargin, height-defaultMargin);
                path.lineTo(width-defaultMargin, height-defaultMargin);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case DOWN_ANGLE:
                path.moveTo((int)(width/2), height-defaultMargin);
                path.lineTo(defaultMargin, defaultMargin);
                path.lineTo(width-defaultMargin, defaultMargin);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case LEFT_ANGLE:
                path.moveTo(defaultMargin, (int)(height/2));
                path.lineTo(width-defaultMargin, defaultMargin);
                path.lineTo(width-defaultMargin, height-defaultMargin);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case RIGHT_ANGLE:
                path.moveTo(width-defaultMargin, (int)(height/2));
                path.lineTo(defaultMargin, defaultMargin);
                path.lineTo(defaultMargin, height-defaultMargin);
                path.close();
                canvas.drawPath(path, paint);
                break;
            default:
        }
        if(select)canvas.drawRect(0, 0, width, height, selectPaint);

        Paint.FontMetrics fontMetrics=textPaint.getFontMetrics();
        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        canvas.drawText(text, (int)(width/2), (int)(height/2)+distance, textPaint);
    }

    public void setType(int type){
        this.type = type;
        path.reset();
        invalidate();
    }

    public void setColor(int color) {
        this.color = color;
        this.paint.setColor(color);
        invalidate();
    }

    public void setSelect(boolean select) {
        this.select = select;
        invalidate();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
