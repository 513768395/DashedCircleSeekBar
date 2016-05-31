package com.example.wufang.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.example.wufang.R;

/**
 * Created by wufang on 16-5-6.
 */
public class DashedCircleSeekBar extends View {

    private static final String TAG = DashedCircleSeekBar.class.getName();

    // 外层圆圈线条的宽度
    private float circleWidth = 2f;
    // 默认的颜色
    private int colorDefault = Color.parseColor("#30FFFFFF");
    // 进度条的颜色
    private int colorProgress = Color.parseColor("#FFFFFF");
    // 表示进度的小线条的宽度
    private float lineWidth = 2f;
    // 默认小线条的长度
    private float lineLength = 30f;
    // 进度小线条的长度
    private float lineLengthProgress = 40f;
    // 中间字体的颜色
    private int centerTextColor = Color.parseColor("#FFFFFF");
    // 最中间字体的大小
    private float centerTextSize = 156f;
    // 从哪个角度开始
    private float startAngle = 180f;
    // 外层表示进度的图片
    private Bitmap imgBar;

    private OnProgressChangedListener listener;
    public void setOnProgressChangedListener(OnProgressChangedListener listener){
        this.listener = listener;
    }

    private void sendListener(){
        if(listener != null){
            listener.onProgressChanged(progress, maxProgress);
        }
    }

    public void setProgress(int progress){
        this.progress = progress <= maxProgress ? progress : maxProgress;
        postInvalidate();
    }

    public int getMaxProgress(){
        return maxProgress;
    }

    public void setCircleColorDefault(int color){
        this.colorDefault = color;
    }

    public void setCircleColorProgress(int color){
        this.colorProgress = color;
    }

    public DashedCircleSeekBar(Context context) {
        super(context);
    }

    public DashedCircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(context, attrs);
    }

    public DashedCircleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttrs(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        x0 = getMeasuredWidth() / 2;
        y0 = getMeasuredHeight() / 2;
        diameter = Math.min(getMeasuredWidth(), getMeasuredHeight());
        radius = diameter / 2f - (imgBar == null ? 0 :imgBar.getWidth() / 2);
        girth = (float) (2f * Math.PI * radius);
        imgSweep = (int) Math.ceil(imgCenterx / (girth / maxProgress));
        rectCircle = new RectF(x0 - radius, y0-radius, x0 + radius, y0 + radius);
    }

    private int progressBarImg = R.mipmap.ic_progress_bar;
    private float progressBarX = 0;
    private float progressBarY = 0;
    private  float diameter;
    private RectF rectCircle = null;

    private void setAttrs(Context context, AttributeSet attrs){
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DashedSeekBar);
        if(ta.hasValue(R.styleable.DashedSeekBar_progressIndicatorImage)){
            Drawable d = ta.getDrawable(R.styleable.DashedSeekBar_progressIndicatorImage);
            imgBar = ((BitmapDrawable)d).getBitmap();
        }else{
            imgBar = BitmapFactory.decodeResource(getContext().getResources(), progressBarImg);
        }
        imgCenterx = imgBar == null ? 0 : imgBar.getWidth()/2;
        imgCentery = imgBar == null ? 0 : imgBar.getHeight()/2;

        circleWidth = ta.getFloat(R.styleable.DashedSeekBar_circleWidth, circleWidth);
        colorDefault = ta.getColor(R.styleable.DashedSeekBar_colorDefault, colorDefault);
        colorProgress = ta.getColor(R.styleable.DashedSeekBar_colorProgress, colorProgress);
        lineWidth = ta.getFloat(R.styleable.DashedSeekBar_lineWidth, lineWidth);
        lineLength = ta.getFloat(R.styleable.DashedSeekBar_lineLength, lineLength);
        lineLengthProgress = ta.getFloat(R.styleable.DashedSeekBar_lineLengthProgress, lineLengthProgress);
        centerTextColor = ta.getColor(R.styleable.DashedSeekBar_centerTextColor, centerTextColor);
        centerTextSize = ta.getDimension(R.styleable.DashedSeekBar_centerTextSize, centerTextSize);
        maxProgress = ta.getInt(R.styleable.DashedSeekBar_maxProgress, maxProgress);
        progress = ta.getInt(R.styleable.DashedSeekBar_progress, progress);
        startAngle = ta.getFloat(R.styleable.DashedSeekBar_startAngle, startAngle);
        if(startAngle >= 360) startAngle = 0;
        ta.recycle();
    }

    private int maxProgress = 200;
    private int progress = 0;

    private Paint pc = new Paint();
    private float radius; // 大圆半径
    private float girth;
    private float imgCenterx, imgCentery; // 图片中心坐标
    private int imgSweep;   // 图片占的刻度
    private float x0 = 0, y0  = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x1, y1, x2, y2;

        // 画中间的数字
        String pt = progress+"";
        pc.setStyle(Paint.Style.FILL);
        pc.setTextSize(centerTextSize);
        pc.setColor(centerTextColor);
        float tw = pc.measureText(pt);
        canvas.drawText(pt, x0 - tw/2f, y0 + centerTextSize/2f, pc);
        pc.reset();

        // 画外层圆上的图片
        float progressAngle = (progress > 0 ? progress - 1 : 0) * (360f / maxProgress) - (360 - startAngle);
        double radian = angleToRadian(progressAngle);
        progressBarX = x1 = (float) (x0 + Math.cos(radian) * radius);
        progressBarY = y1 = (float) (y0 + Math.sin(radian) * radius);
        Matrix matrix = new Matrix();
        matrix.postScale(0.8f, 0.8f, imgCenterx, imgCentery);
        float imgAngle = progress * (360f / maxProgress) - (360f - startAngle);
        matrix.postRotate(imgAngle + 90, imgCenterx, imgCentery);
        matrix.postTranslate(x1 - imgCenterx, y1 - imgCentery);
        canvas.drawBitmap(imgBar, matrix, pc);

        // 画最外层的圆（无进度）
        pc.setColor(colorDefault);
        pc.setStrokeWidth(circleWidth);
        pc.setStrokeCap(Paint.Cap.ROUND);
        pc.setStyle(Paint.Style.STROKE);
        pc.setAntiAlias(true);
        float ae = (maxProgress - (progress > 0 ? progress - 1 : 0) - imgSweep) * (360f / maxProgress);
        imgAngle = Math.abs(imgAngle);
        // 只滑动一点点避免外圆与拖动图片重复
        if(ae > 3) {
            float imgStartAngle = Math.abs(imgAngle - startAngle) < imgSweep ? imgAngle - (imgSweep * (360f / maxProgress)) : startAngle;
            float imgSweepAngle = progress < imgSweep ? -ae + (imgSweep * (360f / maxProgress)) : -ae;
            canvas.drawArc(rectCircle, imgStartAngle, imgSweepAngle, false, pc);
        }
        // 进度的圆
        pc.setColor(colorProgress);
        ae = progress > 0 ? ((progress > 0 ? progress - 1 : 0) - imgSweep) * (360f / maxProgress) : 0;
        // 角度小于3就不会画了 (只滑动一点点避免外圆与拖动图片重复 )
        if(ae > 3) {
            if(ae > 360f - imgSweep * 2 * (360f / maxProgress)){
                ae -= (imgSweep/2 * (360f / maxProgress));
            }
            float imgStartAngle = Math.abs(startAngle - imgAngle) < imgSweep ? imgAngle + imgSweep/2 * (360f / maxProgress) : startAngle;
            canvas.drawArc(rectCircle, imgStartAngle, ae, false, pc);
            pc.reset();
        }

        // 画虚线
        float pRadius = radius - imgCenterx;
        float bgRadius = pRadius - ((lineLengthProgress - lineLength) / 2);
        pc.setColor(colorProgress);
        pc.setStrokeWidth(lineWidth);
        for (int i=0; i<=maxProgress-1; i++) {
            float angle = i * (360f / maxProgress) - (360f - startAngle);
            if(i < progress) {
                radian = angleToRadian(angle);
                x1 = (float) (x0 + Math.cos(radian) * pRadius);
                y1 = (float) (y0 + Math.sin(radian) * pRadius);
                x2 = (float) (x0 + Math.cos(radian) * (pRadius - lineLengthProgress));
                y2 = (float) (y0 + Math.sin(radian) * (pRadius - lineLengthProgress));
            }else{
                pc.setColor(colorDefault);
                radian = angleToRadian(angle);
                x1 = (float) (x0 + Math.cos(radian) * bgRadius);
                y1 = (float) (y0 + Math.sin(radian) * bgRadius);
                x2 = (float) (x0 + Math.cos(radian) * (bgRadius - lineLength));
                y2 = (float) (y0 + Math.sin(radian) * (bgRadius - lineLength));
            }
            canvas.drawLine(x1, y1, x2, y2, pc);
        }
        pc.reset();
    }

    private double angleToRadian(float angle){
        return (Math.PI / 180.0f) * angle;
    }

    private float feelWidth = 20;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(touchInCircle(x, y)){
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(touchInCircle(x, y)) {
                    seekTo(x, y, false);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(touchInCircle(x, y)) {
                    seekTo(x, y, true);
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void seekTo(float eventX, float eventY, boolean isUp) {
        if (!isUp) {
            double radian = Math.atan2(eventY - y0, eventX - x0);
            /*
             * 由于atan2返回的值为[-pi,pi]
             * 因此需要将弧度值转换一下，使得区间为[0,2*pi]
             */
            if (radian < 0){
                radian = radian + 2 * Math.PI;
            }

            //mSeekBarDegree = (float) Math.round(Math.toDegrees(radian));
            double degree = Math.toDegrees(radian);
            degree = degree > startAngle ? degree - startAngle : degree + (360 - startAngle);
            int cp = (int) Math.round(degree / (360f / maxProgress));

            //progress = (int) (maxProgress * (mSeekBarDegree + startAngle) / 360);
            if(Math.abs(progress - cp) > maxProgress / 4){
                return;
            }
            progress = cp;
            postInvalidate();

            sendListener();
        }else{
            invalidate();
        }
    }

    private boolean touchInCircle(float x, float y){
        double r = Math.sqrt(Math.pow(x - x0, 2) + Math.pow(y-y0, 2));
        if(Math.abs(r) <= radius + imgBar.getWidth() && Math.abs(r) >= radius - lineLengthProgress - imgBar.getWidth()) {
            return true;
        }
        return false;
    }

    private class MyAnimation extends Animation{
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
        }
    }

    public interface OnProgressChangedListener{
        void onProgressChanged(int progress, int maxProgress);
    }
}
