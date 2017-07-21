package com.minminaya.fish;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Created by Niwa on 2017/7/20.
 */
public class FishDrawable extends Drawable {

    /**
     * 头部半径
     */
    private static final float HEAD_RADIUS = 30f;
    /**
     * 第一节身体长度
     */
    private static final float BODY_LENGH = HEAD_RADIUS * 3.2f;
    /**
     * 身体的透明度
     */
    private static final int BODY_ALPHA = 220;
    /**
     * 鱼鳍的透明度
     */
    private static final int FINS_ALPHA = 100;

    /**
     * 其他部分的透明度
     */
    private static final int OTHER_ALPHA = 160;

    /**
     * 左鱼鳍
     */
    private static final int FINS_LEFT = 1;
    /**
     * 右鱼鳍
     */
    private static final int FINS_RIGHT = -1;

    /**
     * 鱼鳍的俩个控制点的长度，即鱼鳍宽度
     */
    private static final float FINS_LENGTH = HEAD_RADIUS * 1.3f;
    /**
     * 鱼的总长度
     */
    private static final float TOTAL_LENGTH = HEAD_RADIUS * 6.79f;


    //控制的
    /**
     * 动画控制引擎变量值
     */
    private float currentValue = 0;
    private float mainAngle = 90;
    private ObjectAnimator finsAnimator;
    /**
     * 全局频率，三角函数里面的
     */
    private float waveFrequence = 1;

    /**
     * 鱼头点
     */
    private PointF headPoint;


    //转弯更自然的中心点
    /**
     * 重心
     */
    private PointF middlePoint;

    private float finsAngle = 0;

    /**
     * 身体的画笔
     */
    private Paint bodyPaint;

    private Path mPath;

    /**
     * 主画笔
     */
    private Paint mPaint;
    private Context mContext;

    public FishDrawable(Context context) {
        init();
        this.mContext = context;
    }

    public PointF getHeadPoint() {
        return headPoint;
    }

    public PointF getMiddlePoint() {
        return middlePoint;
    }

    /**
     * 初始化
     */
    private void init() {
        mPath = new Path();

        //主画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setDither(true);
        mPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));

        bodyPaint = new Paint();
        bodyPaint.setAntiAlias(true);
        bodyPaint.setStyle(Paint.Style.FILL);
        bodyPaint.setDither(true);
        bodyPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));

        //将重心坐标设置为重点
        middlePoint = new PointF(4.18f * HEAD_RADIUS, 4.18f * HEAD_RADIUS);

        //鱼鳍灵动的动画等
        finsAnimator = ObjectAnimator.ofFloat(this, "finsAngle", 0f, 1f, 0f);
        finsAnimator.setRepeatMode(ValueAnimator.REVERSE);
        finsAnimator.setRepeatCount(new Random().nextInt(3));

        //动画引擎
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 540 * 100);
        valueAnimator.setDuration(180 * 1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //引擎
                currentValue = (float) valueAnimator.getAnimatedValue();
                invalidateSelf();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                //监听重复动画，动作为开始播放动画
                finsAnimator.start();
            }
        });
        valueAnimator.start();
    }

    @Override
    public void draw(Canvas canvas) {

        //设置一个半透明图层，避免与白色背景形成干扰，并且尺寸必须与view的大小一致否则鱼显示不完全
        canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 240, Canvas.ALL_SAVE_FLAG);
        makeBody(canvas, HEAD_RADIUS);
        canvas.restore();
        mPath.reset();
        //恢复颜色
        mPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));
    }


    /**
     * 画鱼的身子
     * <p>
     * 主方向是头到尾的方向跟x轴正方向的夹角
     * 前进方向跟主方向相差180度
     *
     * @param canvas
     * @param headRadius
     */
    private void makeBody(Canvas canvas, float headRadius) {
        //现有的角度 = 原始角度 + sin(域值)*可摆动的角度    sin是用来控制周期运动的
        float angle = (float) (mainAngle + Math.sin(Math.toRadians(currentValue * 1.2 * waveFrequence)) * 2);//中心轴线加偏移量和x轴顺时针方向夹角，其实就是偏移角-2到2

        //假设现在BODY_LENGH / 2线段在x轴上，旋转90度得到头部终点坐标
        headPoint = calculatPoint(middlePoint, BODY_LENGH / 2, mainAngle);
        //画头
        canvas.drawCircle(headPoint.x, headPoint.y, HEAD_RADIUS, mPaint);

        //右鱼鳍  右鱼鳍轴的起点  起点相对鱼头方向顺时针转了110度
        PointF pointFFinsRight = calculatPoint(headPoint, headRadius * 0.9f, angle - 110);
        makeFins(canvas, pointFFinsRight, FINS_RIGHT, angle);
        //左鱼鳍
        PointF pointFFinsLeft = calculatPoint(headPoint, headRadius * 0.9f, angle + 110);
        makeFins(canvas, pointFFinsLeft, FINS_LEFT, angle);

        //躯干底部的圆位置   相对鱼头方向旋转180度
        PointF endPointF = calculatPoint(headPoint, BODY_LENGH, angle - 180);

        //大躯干的下面的躯干2,
        PointF mainPointF = new PointF(endPointF.x, endPointF.y);
        makeSegments(canvas, mainPointF, headRadius * 0.7f, 0.6f, angle);

        //鱼的最大身子
        PointF pointF1, pointF2, pointF3, pointF4, controlLeftPointF, controlRightPointF;


        pointF1 = calculatPoint(headPoint, headRadius, angle - 80);//80度得到的点偏上一点，看起来会更好，右上
        pointF4 = calculatPoint(headPoint, headRadius, angle + 80);//左上

        pointF2 = calculatPoint(endPointF, headRadius * 0.7f, angle - 90);//右下
        pointF3 = calculatPoint(endPointF, headRadius * 0.7f, angle + 90);//左下

        //贝塞尔曲线控制点
        controlRightPointF = calculatPoint(headPoint, BODY_LENGH * 0.56f, angle - 130);//右控制点
        controlLeftPointF = calculatPoint(headPoint, BODY_LENGH * 0.56f, angle + 130);

        mPath.reset();
        mPath.moveTo(pointF1.x, pointF1.y);
        mPath.quadTo(controlRightPointF.x, controlRightPointF.y, pointF2.x, pointF2.y);
        mPath.lineTo(pointF3.x, pointF3.y);

        mPath.quadTo(controlLeftPointF.x, controlLeftPointF.y, pointF4.x, pointF4.y);
        mPath.lineTo(pointF1.x, pointF1.y);
        //画出鱼最大身子
        mPaint.setColor(Color.argb(BODY_ALPHA, 244, 92, 71));
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 画第二节节肢
     *
     * @param canvas
     * @param mainPointF       该段的顶部圆心的坐标
     * @param segmentTopRadius 该段的顶部半径
     * @param ratio            梯形上下比例
     * @param fatherAngle      鱼头方向（父控件），默认为90
     */
    private void makeSegments(Canvas canvas, PointF mainPointF, float segmentTopRadius, float ratio, float fatherAngle) {

        float angle = (float) (fatherAngle + Math.cos(Math.toRadians(currentValue * 1.5 * waveFrequence)) * 15);//中心轴线和X轴顺时针方向夹角，15度左右摇摆

        //身长
        float segmentLength = segmentTopRadius * (ratio + 1);
        //底部圆心坐标
        PointF bottomPointF = calculatPoint(mainPointF, segmentLength, angle - 180);

        PointF pointF1, pointF2, pointF3, pointF4;
        //梯形4个角的坐标
        pointF1 = calculatPoint(mainPointF, segmentTopRadius, angle - 90);//左上
        pointF4 = calculatPoint(mainPointF, segmentTopRadius, angle + 90);//右上

        pointF2 = calculatPoint(bottomPointF, segmentTopRadius * ratio, angle - 90);//左下
        pointF3 = calculatPoint(bottomPointF, segmentTopRadius * ratio, angle + 90);//右下

        //画上下俩个圆
        canvas.drawCircle(mainPointF.x, mainPointF.y, segmentTopRadius, mPaint);
        canvas.drawCircle(bottomPointF.x, bottomPointF.y, segmentTopRadius * ratio, mPaint);

        mPath.reset();
        mPath.moveTo(pointF1.x, pointF1.y);
        mPath.lineTo(pointF4.x, pointF4.y);
        mPath.lineTo(pointF3.x, pointF3.y);
        mPath.lineTo(pointF2.x, pointF2.y);
        canvas.drawPath(mPath, mPaint);

        //躯干2的下面的躯干，和躯干2差不多
        PointF topPointF2 = new PointF(bottomPointF.x, bottomPointF.y);
        makeSegmentLong(canvas, topPointF2, segmentTopRadius * 0.6f, 0.4f, angle);
    }

    /**
     * 画第三节节肢
     *
     * @param canvas
     * @param topPointF2       该段的顶部圆心的坐标
     * @param segmentTopRadius 该段的顶部半径
     * @param ratio            梯形上下比例
     * @param fatherAngle      上面控件方向，默认为90
     */
    private void makeSegmentLong(Canvas canvas, PointF topPointF2, float segmentTopRadius, float ratio, float fatherAngle) {

        float angle = (float) (fatherAngle + Math.sin(Math.toRadians(currentValue * 1.5 * waveFrequence)) * 35);//中心轴线和X轴顺时针方向夹角,这里为35度比上面部分控件越来越大，扭动时候可以体现出鱼灵动的摇尾巴的动作

        //身长
        float segmentLength = segmentTopRadius * (ratio + 2.7f);
        //同上面
        PointF bottomPointF = calculatPoint(topPointF2, segmentLength, angle - 180);

        PointF pointF1, pointF2, pointF3, pointF4;
        //梯形4个角的坐标
        pointF1 = calculatPoint(topPointF2, segmentTopRadius, angle - 90);//左上
        pointF4 = calculatPoint(topPointF2, segmentTopRadius, angle + 90);//右上

        pointF2 = calculatPoint(bottomPointF, segmentTopRadius * ratio, angle - 90);//左下
        pointF3 = calculatPoint(bottomPointF, segmentTopRadius * ratio, angle + 90);//右下


//        makeTail(canvas, topPointF2, segmentLength, segmentTopRadius, angle);
        //为了扭得更骚，加了50宽度
        makeTail(canvas, topPointF2, segmentLength, segmentTopRadius + 50, angle);
        //画上下俩个圆
        canvas.drawCircle(bottomPointF.x, bottomPointF.y, segmentTopRadius * ratio, mPaint);

        mPath.reset();
        mPath.moveTo(pointF1.x, pointF1.y);
        mPath.lineTo(pointF4.x, pointF4.y);
        mPath.lineTo(pointF3.x, pointF3.y);
        mPath.lineTo(pointF2.x, pointF2.y);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 画尾巴
     *
     * @param topPointF2 上个控件的底部圆心，
     * @param length     上个控件的底部圆半径
     * @param maxWidth   最大的宽度，用来确定三角形的左右俩宽度不越界
     * @param angle      上级控件的当前角度
     */
    private void makeTail(Canvas canvas, PointF topPointF2, float length, float maxWidth, float angle) {
        //三角形的最大2分之宽度，随三角函数变化
        float newWidth = (float) (Math.abs(Math.sin(Math.toRadians(currentValue * 1.7 * waveFrequence))) * maxWidth + (HEAD_RADIUS * 1) / 5);

        //大小三角形的底边中点，1为大
        PointF bottomPointF1 = calculatPoint(topPointF2, length, angle - 180);
        PointF bottomPointF2 = calculatPoint(topPointF2, length - 10, angle - 180);//小的

        //2个三角形的左右顶点，共4个
        PointF pointF1, pointF2, pointF3, pointF4;

        pointF1 = calculatPoint(bottomPointF1, newWidth, angle - 90);//右定点 大三角形
        pointF4 = calculatPoint(bottomPointF1, newWidth, angle + 90);//左

        pointF2 = calculatPoint(bottomPointF2, newWidth - 20, angle - 90);//右定点， 小三角形   这里用10和20都可以
        pointF3 = calculatPoint(bottomPointF2, newWidth - 20, angle + 90);//左

        //小三角形
        mPath.reset();
        mPath.moveTo(topPointF2.x, topPointF2.y);
        mPath.lineTo(pointF2.x, pointF2.y);
        mPath.lineTo(pointF3.x, pointF3.y);
        mPath.lineTo(topPointF2.x, topPointF2.y);
        canvas.drawPath(mPath, mPaint);

        //大三角形
        mPath.reset();
        mPath.moveTo(topPointF2.x, topPointF2.y);
        mPath.lineTo(pointF1.x, pointF1.y);
        mPath.lineTo(pointF4.x, pointF4.y);
        mPath.lineTo(topPointF2.x, topPointF2.y);
        canvas.drawPath(mPath, mPaint);


    }

    /**
     * @param canvas
     * @param pointFFinsRight 鱼鳍主轴的起点
     * @param type
     * @param angle           鱼头的方向
     */
    private void makeFins(Canvas canvas, PointF pointFFinsRight, int type, float angle) {
        float controlAngle = 110;//鱼鳍控制点相对于鱼主轴方向的角度，模型示意图中是110
        mPath.reset();

        mPath.moveTo(pointFFinsRight.x, pointFFinsRight.y);
        //计算鱼鳍主轴的终点，按照模型图（博客里）,如果是右鱼鳍，那么按照模型图，鱼的角度减去鱼鳍扭的角度还有减去180度就是鱼主轴的另外一个方向
        PointF endPointF = calculatPoint(pointFFinsRight, FINS_LENGTH, type == FINS_RIGHT ? angle - finsAngle - 180 : angle + finsAngle + 180);
        //贝塞尔曲线辅助控制点
        PointF controlPointF = calculatPoint(pointFFinsRight, FINS_LENGTH * 1.8f, type == FINS_RIGHT ? angle - finsAngle - controlAngle : angle + finsAngle + controlAngle);

        //画贝塞尔曲线
        mPath.quadTo(controlPointF.x, controlPointF.y, endPointF.x, endPointF.y);
        mPath.lineTo(pointFFinsRight.x, pointFFinsRight.y);


        mPaint.setColor(Color.argb(FINS_ALPHA, 244, 92, 71));
        canvas.drawPath(mPath, mPaint);
        mPaint.setColor(Color.argb(OTHER_ALPHA, 244, 92, 71));

    }

    /**
     * 输入起点、长度、旋转角度计算终点
     * <p>
     * 知道一个线段，一个定点，线段旋转角度求终点坐标
     * 根据极坐标系原理 x = pcog(a), y = psin(a)
     *
     * @param startPoint 起点
     * @param length     长度
     * @param angle      旋转角度
     * @return 计算结果点
     */
    private static PointF calculatPoint(PointF startPoint, float length, float angle) {
        float deltaX = (float) Math.cos(Math.toRadians(angle)) * length;
        //符合Android坐标的y轴朝下的标准，和y轴有关的统一减180度
        float deltaY = (float) Math.sin(Math.toRadians(angle - 180)) * length;
        return new PointF(startPoint.x + deltaX, startPoint.y + deltaY);
    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        //半透明,只有绘制的地方才盖住下边
        return PixelFormat.TRANSLUCENT;
    }

    public void setMainAngle(float mainAngle) {
        this.mainAngle = mainAngle;
    }

    public float getHeadRadius() {
        return HEAD_RADIUS;
    }

    public void setWaveFrequence(float waveFrequence) {
        this.waveFrequence = waveFrequence;
    }

    public ObjectAnimator getFinsAnimator() {
        return finsAnimator;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (8.38f * HEAD_RADIUS);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (8.38f * HEAD_RADIUS);
    }
}
