package com.android.mybarometer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Date :2021/12/24
 * Time :21:24
 * Author:moyihen
 * Description: 仪表盘
 * * 注意:因为是在固定开发板用,没怎么考虑适配情况.
 * 1.表盘大小是根据控件xml布局文件的width决定
 * 2.控件宽度太小可能刻度文字显示不全,可以看情况调整下文字大小.
 */
public class DashboardView extends View {
    private Paint arcPaint;
    //圆环的角度
    private final int SWEEPANGLE = 280;
    //刻度画笔
    private Paint pointerPaint;

    private int height;

    //圆环半径
    private int mRadius;
    //圆环的宽度
    int arcW = 10;
    //发光的宽度
    int gleamyArcW = arcW * 3;
    //刻度宽度
    int minScalew = 5;
    int maxScalew = 5;
    //刻度的长度
    int maxScaleLength = 50;
    int minScaleLength = 30;
    private Paint gleamyArcPaint;
    //发光圆环的半径
    private int mRadiusG;
    //阴影宽度
    private int shade_w = 40;

    private Path pointerPath;

    private float currentDegree = 0;
    //指针当前的角度.
    private int startAngele = 90 + (360 - SWEEPANGLE) / 2;
    //仪表盘显示的数字
    private String speed = "0";
    private Paint mTextPaint;
    private Paint mPaint;
    private ValueAnimator mAnim;

    public DashboardView(Context context) {
        super(context);
        init(context);
    }


    public DashboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // 关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        //外层动态圆环画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);//画线模式
        mPaint.setAntiAlias(true);

        //圆环画笔
        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.STROKE);//画线模式
        arcPaint.setStrokeWidth(arcW);//线宽度
        arcPaint.setColor(Color.parseColor("#07A6EC"));
        arcPaint.setAntiAlias(true);
        //刻度
        pointerPaint = new Paint();
        pointerPaint.setAntiAlias(true);
        pointerPaint.setColor(Color.parseColor("#0937EF"));
        pointerPaint.setTextSize(40);
        pointerPaint.setTextAlign(Paint.Align.RIGHT);

        pointerPath = new Path();

        //发光圆环
        gleamyArcPaint = new Paint();
        gleamyArcPaint.setAntiAlias(true);
        gleamyArcPaint.setStyle(Paint.Style.STROKE);
        gleamyArcPaint.setStrokeWidth(gleamyArcW);
        //文字
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(60);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        height = (int) (getMeasuredHeight() * 1.2);  //使仪表盘整体下移,确保其它控件合理显示

        mRadius = (int) ((double) getMeasuredWidth() / 2 * 0.8);
        mRadiusG = mRadius - gleamyArcW / 2;
        shade_w = (int) (mRadius * 0.4);
        //表盘背景颜色
//        canvas.drawColor(Color.parseColor("#040613"));
        //最外层白色动态圆环
        drawDynamicArcs(canvas);
        //圆环
        drawArcs(canvas);
        //发光圆环
        drawLuminousArc(canvas);
        //刻度
        drawDegree(canvas);
        //指针阴影
        drawShade(canvas);
        //黑色圆形背景
//        drawCircleBlack(canvas);
        //指针
        drawPointer(canvas);
        //中心圆环
        drawCenterArcs(canvas);
        //中心显示文字
        drawCenterText(canvas);
    }

    private void drawDynamicArcs(Canvas canvas) {
        //半径
        int dyRadius = (int) ((double) getMeasuredWidth() / 2 * 0.88);
        int w = 10;
        int[] colorSweep = new int[]{Color.parseColor("#00FFFFFF"), Color.parseColor("#FFFFFFFF")};
        float[] position = new float[]{0f, 0.5f};
        SweepGradient mShader = new SweepGradient((float) getMeasuredWidth() / 2, (float) height / 2, colorSweep, position);

        //旋转渐变
        Matrix matrix = new Matrix();
        matrix.setRotate(startAngele, (float) canvas.getWidth() / 2, (float) height / 2);
        mShader.setLocalMatrix(matrix);
        mPaint.setShader(mShader);
        mPaint.setStrokeWidth(w);
        RectF rectF = new RectF();
        rectF.left = (float) (getMeasuredWidth() / 2 - (dyRadius - w / 2));
        rectF.top = (float) (height / 2 - (dyRadius - w / 2));
        rectF.right = (float) (getMeasuredWidth() / 2 + (dyRadius - w / 2));
        rectF.bottom = (float) (height / 2 + (dyRadius - w / 2));
        canvas.drawArc(rectF, 90 + (float) (360 - SWEEPANGLE) / 2, currentDegree, false, mPaint);
    }

    private void drawCenterText(Canvas canvas) {
        mTextPaint.reset();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(80);
        mTextPaint.setAntiAlias(true);
        double mm = mRadius * 0.4;
        RectF rect = new RectF();
        rect.left = (float) ((double) getMeasuredWidth() / 2 - mm);
        rect.top = (float) ((double) height / 2 - mm);
        rect.right = (float) ((double) getMeasuredWidth() / 2 + mm);
        rect.bottom = (float) ((double) height / 2 + mm);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = rect.centerY() + distance;
        //速度
        canvas.drawText(speed, rect.centerX(), baseline, mTextPaint);
        mTextPaint.setTextSize(100);

        //绘制底部文字(向下20px)
        float text_h = Math.abs(fontMetrics.top - fontMetrics.bottom) - 80;
        String info = "hPa";
        canvas.drawText(info, (float) getMeasuredWidth() / 2, (float) (height / 2) + (mRadius - text_h), mTextPaint);
        //速度文字下划线
        float text_w = mTextPaint.measureText(info);
        int[] color = {Color.parseColor("#80041B25"), Color.parseColor("#0496C6"), Color.parseColor("#80041B25")};
        float[] position = {0f, 0.5f, 1f};
        mTextPaint.setStrokeWidth(3);
        mTextPaint.setShader(new LinearGradient((float) (getMeasuredWidth() / 2) - text_w / 2, (float) (height / 2) + (mRadius - text_h + 10),
                (float) (getMeasuredWidth() / 2) + text_w / 2, (float) (height / 2) + (mRadius - text_h + 10), color, position
                , Shader.TileMode.MIRROR));
        canvas.drawLine((float) (getMeasuredWidth() / 2) - text_w / 2, (float) (height / 2) + (mRadius - text_h + 10)
                , (float) (getMeasuredWidth() / 2) + text_w / 2, (float) (height / 2) + (mRadius - text_h + 10), mTextPaint);
    }

    private void drawCenterArcs(Canvas canvas) {
        //中心发光圆环
        pointerPaint.setColor(Color.parseColor("#0947C3"));
        pointerPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((float) getMeasuredWidth() / 2, (float) height / 2, (float) (mRadius * 0.4), pointerPaint);
        //内部深色实心圆
        pointerPaint.setColor(Color.parseColor("#040613"));
        canvas.drawCircle((float) getMeasuredWidth() / 2, (float) height / 2,
                (float) (mRadius * 0.4) - pointerPaint.getStrokeWidth(), pointerPaint);
    }

    private void drawPointer(Canvas canvas) {
        canvas.save();
        canvas.translate((float) getMeasuredWidth() / 2, (float) height / 2);
        canvas.rotate(startAngele + currentDegree);
        pointerPaint.setColor(Color.WHITE);
        pointerPath.moveTo(mRadius, 0);
        pointerPath.lineTo(0, -10);
        pointerPath.lineTo(0, 10);
        pointerPath.close();
        canvas.drawPath(pointerPath, pointerPaint);
        canvas.restore();
    }

    private void drawCircleBlack(Canvas canvas) {
        pointerPaint.setStyle(Paint.Style.FILL);
        pointerPaint.setColor(Color.parseColor("#040613"));
        canvas.drawCircle((float) getMeasuredWidth() / 2, (float) height / 2, (float) (mRadius * 0.6), pointerPaint);
    }

    //指针阴影
    private void drawShade(Canvas canvas) {
        int[] colorSweep = new int[]{0x66FFE9EC, 0x0328E9EC, 0x1a28E9EC, 0x66FFE9EC};
        float[] position = new float[]{0f, 0.36f, 0.5f, 0.7f};
        SweepGradient mShader = new SweepGradient((float) getMeasuredWidth() / 2, (float) height / 2, colorSweep, position);
        gleamyArcPaint.setShader(mShader);
        gleamyArcPaint.setStrokeWidth(shade_w);
        RectF rectF = new RectF();
        rectF.left = (float) (getMeasuredWidth() / 2 - (mRadiusG - shade_w / 2));
        rectF.top = (float) (height / 2 - (mRadiusG - shade_w / 2));
        rectF.right = (float) (getMeasuredWidth() / 2 + (mRadiusG - shade_w / 2));
        rectF.bottom = (float) (height / 2 + (mRadiusG - shade_w / 2));
        canvas.drawArc(rectF, 90 + (float) (360 - SWEEPANGLE) / 2, currentDegree, false, gleamyArcPaint);
    }

    //画发光圆
    private void drawLuminousArc(Canvas canvas) {

        gleamyArcPaint.setStrokeWidth(gleamyArcW);
        int[] a = {Color.parseColor("#000947C3"), Color.parseColor("#ff0947C3")};
        float[] b = {0.9f, 1f};
        RadialGradient radialGradient = new RadialGradient((float) getMeasuredWidth() / 2, (float) height / 2, mRadius - arcW, a, b, Shader.TileMode.CLAMP);
        gleamyArcPaint.setShader(radialGradient);

        RectF rectF = new RectF((float) getMeasuredWidth() / 2 - mRadiusG, (float) height / 2 - mRadiusG,
                (float) getMeasuredWidth() / 2 + mRadiusG, (float) height / 2 + mRadiusG);
        canvas.drawArc(rectF, 90 + (float) (360 - SWEEPANGLE) / 2, SWEEPANGLE, false, gleamyArcPaint);
    }

    int clockPointNum = 40;

    private void drawDegree(Canvas canvas) {
        pointerPaint.setColor(Color.parseColor("#26396F"));
        pointerPaint.setTextSize(40);
        pointerPaint.clearShadowLayer();
        pointerPaint.setTextAlign(Paint.Align.RIGHT);

        canvas.save();
        //原点移到空间中心点
        canvas.translate((float) getMeasuredWidth() / 2, (float) height / 2);

        canvas.rotate((float) (360 - SWEEPANGLE) / 2 + 90);//(360-240)/2+90;
        //设置刻度文字颜色大小.
        mTextPaint.reset();
        mTextPaint.setTextSize(40);
        mTextPaint.clearShadowLayer();
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTextPaint.setAntiAlias(true);
        for (int i = 0; i < clockPointNum; i++) {
            if (i % 5 == 0) {     //长表针
                pointerPaint.setStrokeWidth(maxScalew);
                canvas.drawLine(mRadiusG - arcW, (float) maxScalew / 2, mRadiusG - maxScaleLength, (float) maxScalew / 2, pointerPaint);
                drawPointerText(canvas, mRadiusG - arcW, maxScalew / 2, i);
            } else {    //短表针
                pointerPaint.setStrokeWidth(minScalew);
                canvas.drawLine(mRadiusG - arcW, (float) maxScalew / 2, mRadiusG - minScaleLength, (float) maxScalew / 2, pointerPaint);
            }
            canvas.rotate((float) SWEEPANGLE / (float) clockPointNum);
        }
        //最后一根
        canvas.drawLine(mRadiusG - arcW, (float) -maxScalew / 2, mRadiusG - maxScaleLength, (float) -maxScalew / 2, pointerPaint);

        drawPointerText(canvas, mRadiusG - arcW, maxScalew / 2, 40);
        canvas.restore();
        count = 300;
    }

    int count = 300;

    private void drawPointerText(Canvas canvas, int x, int y, int i) {
        //动态设置刻度文字颜色。
        float a = (float) SWEEPANGLE / (float) clockPointNum;

        mTextPaint.setColor(Color.WHITE);

        if (i != 0) count += 100;
        //保存状态
        canvas.save();

        canvas.translate(mRadiusG - maxScaleLength, y);
        // 90 + (360 - SWEEPANGLE) / 2 = 130
        canvas.rotate(-(130f + a * i));

        //文字baseline在y轴方向的位置
        float baseLineY = Math.abs(mTextPaint.ascent() + mTextPaint.descent()) / 2;
        if (i >= 0 && i <= 10) {
            mTextPaint.setTextAlign(Paint.Align.LEFT);
        } else if (i > 10 && i <= 25) {
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            baseLineY = baseLineY + 15;
        } else if (i > 25) {
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
        }
        canvas.drawText(String.valueOf(count), 0, baseLineY, mTextPaint);
        //恢复对canvas操作
        canvas.restore();
    }

    private void drawArcs(Canvas canvas) {
        RectF rectF = new RectF((float) getMeasuredWidth() / 2 - mRadius, (float) height / 2 - mRadius,
                (float) getMeasuredWidth() / 2 + mRadius, (float) height / 2 + mRadius);
        canvas.drawArc(rectF, 90 + (float) (360 - SWEEPANGLE) / 2, SWEEPANGLE, false, arcPaint);
    }


    /**
     * 外部更新刻度.
     *
     * @param value .
     */
    public void udDataSpeed(float value) {
        speed = String.valueOf(value);
        if (value < 300f) return;
        if (value > 1100) value = 1100;
        float constant = SWEEPANGLE / 8f / 100;
        startAnimation(currentDegree, (value - 300) * constant);
    }

    //指针+阴影偏移动画.
    private void startAnimation(float start, float end) {
        if (mAnim != null) {
            if (mAnim.isRunning() || mAnim.isStarted()) {
                mAnim.cancel();
                mAnim.removeAllUpdateListeners();
            }
        }
        mAnim = ValueAnimator.ofFloat(start, end);
        mAnim.setDuration(700);
        mAnim.addUpdateListener(valueAnimator -> {
            currentDegree = (float) mAnim.getAnimatedValue();
            invalidate();
        });
        mAnim.start();
    }

    /**
     * 退出动画.
     */
    public void closeAnimation() {
        if (mAnim != null) {
            mAnim.cancel();
            mAnim.removeAllUpdateListeners();
        }
    }
}




