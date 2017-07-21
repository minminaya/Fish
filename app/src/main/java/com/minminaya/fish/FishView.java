package com.minminaya.fish;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

/**
 * 注意点：
 * 1.波纹点击效果（并且透明度随时间变化）
 * 2.鱼加速游动，先慢后块（用插值器）
 * 3.鱼的摆动速度（动画显示时长，其实就是扭尾巴的快慢），随机变化
 * 4.鱼游动的路径，贝塞尔曲线控制点的取值，起始点取imageView的左顶点
 * Created by Niwa on 2017/7/21.
 */

public class FishView extends RelativeLayout {

    /**
     * 波纹的线宽
     */
    private static final int STROKE_WIDTH = 8;

    /**
     * 默认的波纹半径
     */
    private static final float DEFAULT_RADIUS = 150;
    /**
     * 屏幕宽
     */
    private int mScreenWidth;
    /**
     * 屏幕高
     */
    private int mScreenHeight;

    private ImageView ivFish;
    private FishDrawable fishDrawable;

    private Paint mPaint;
    private int alpha = 100;
    private Canvas canvas;

    //当前按下的坐标点x和y
    private float x = 0;
    private float y = 0;

    /**
     * 波纹的半径
     */
    private float radius = 0;

    public FishView(Context context) {
        this(context, null);
    }

    public FishView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FishView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStuff(context);
    }

    /**
     * 初始化各数据
     */
    private void initStuff(Context context) {

        setWillNotDraw(false);//将不绘画打开，设为可以绘画，即调用onDraw方法
        //获取
        getScreenParams();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);

        ivFish = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ivFish.setLayoutParams(layoutParams);

        fishDrawable = new FishDrawable(context);
        ivFish.setImageDrawable(fishDrawable);

        addView(ivFish);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置当前的高宽
        setMeasuredDimension(mScreenWidth, mScreenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.canvas == null) {
            this.canvas = canvas;
        }
        //点击后的波纹的颜色
        mPaint.setARGB(alpha, 0, 125, 251);
        //画波纹的圈，这里的半径随着动画引擎变化
        canvas.drawCircle(x, y, radius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "radius", 0f, 1f);
        objectAnimator.start();

        //画鱼的游泳路径
        makeTrail(new PointF(x, y));

        return super.onTouchEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void makeTrail(PointF touch) {
        Path path = new Path();
        PointF fishMiddlePointF = new PointF(ivFish.getX() + fishDrawable.getMiddlePoint().x, ivFish.getY() + fishDrawable.getMiddlePoint().y);
        PointF fishHeadPointF = new PointF(ivFish.getX() + fishDrawable.getHeadPoint().x, ivFish.getY() + fishDrawable.getHeadPoint().y);

        //将imageView的左顶点作为鱼的middlePoint， 就是点O
        path.moveTo(ivFish.getX(), ivFish.getY());
        //要的转动的角度，示意图中的角AOB
        float angle = caculateScrollAngle(fishMiddlePointF, fishHeadPointF, touch);
        //控制点2  AOB夹角中线
        PointF controlPointF = calculatPoint(fishMiddlePointF, (float) (1.6 * fishDrawable.getHeadRadius()), angle / 2);
        //A,C作为控制点，终点是触摸点偏左fishDrawable.getHeadPoint()距离（不明白这里的具体意思，感觉和示意图有点差别）
        path.cubicTo(fishHeadPointF.x, fishHeadPointF.y, controlPointF.x, controlPointF.y, touch.x - fishDrawable.getHeadPoint().x, touch.y - fishDrawable.getHeadPoint().y);

        final float[] pos = new float[2];
        final float[] tan = new float[2];
        final PathMeasure pathMeasure = new PathMeasure(path, false);

        ObjectAnimator animator = ObjectAnimator.ofFloat(ivFish, "x", "y", path);
        animator.setDuration(2 * 1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                //恢复扭动频率
                fishDrawable.setWaveFrequence(1f);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                //设定扭动频率
                fishDrawable.setWaveFrequence(2f);
                //动态设置扭动动画
                //获取FishDrawable里头的扭尾巴动画，然后动态设置，为了更骚
                ObjectAnimator finsAnimator = fishDrawable.getFinsAnimator();
                //动画次数，体现出鱼的生物性
                finsAnimator.setRepeatCount(new Random().nextInt(3));
                //动画显示时长，其实就是扭尾巴的快慢
                finsAnimator.setDuration((long) ((new Random().nextInt(1) + 1) * 500));
                finsAnimator.start();
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //属性改变百分比
                float persent = valueAnimator.getAnimatedFraction();
                //第一个参数一脸懵逼，个人理解应该是当前切线的轮廓乘以属性改变百分比
                pathMeasure.getPosTan(pathMeasure.getLength() * persent, pos, tan);
                //根据切线值得到当前的切线角度，并按Android 的y轴在下面将y轴反转
                float actualAngle = (float) (Math.atan2(-tan[1], tan[0]) * 180.0 / Math.PI);
                //动态设置角度到fishDrawable
                fishDrawable.setMainAngle(actualAngle);
            }
        });
        animator.start();
    }

    /**
     * objectAnimators自动执行
     */
    public void setRadius(float currentValue) {
        alpha = (int) (100 * (1 - currentValue) / 2);
        radius = DEFAULT_RADIUS * currentValue;
        postInvalidate();
    }

    /**
     * 要的转动的角度，示意图中的角AOB
     *
     * @param fishMiddlePoint O点
     * @param fishHeadPointF  A点
     * @param touch           触摸点
     * @return 要的转动的角度
     */
    private float caculateScrollAngle(PointF fishMiddlePoint, PointF fishHeadPointF, PointF touch) {
        //计算OA向量乘OB向量
        float abc = (fishHeadPointF.x - fishMiddlePoint.x) * (touch.x - fishMiddlePoint.x) + (fishHeadPointF.y - fishMiddlePoint.y) * (touch.y - fishMiddlePoint.y);
        //向量积除以向量模
        float angleCos = (float) (abc /
                ((Math.sqrt((fishHeadPointF.x - fishMiddlePoint.x) * (fishHeadPointF.x - fishMiddlePoint.x) + (fishHeadPointF.y - fishMiddlePoint.y) * (fishHeadPointF.y - fishMiddlePoint.y)))
                        * (Math.sqrt((touch.x - fishMiddlePoint.x) * (touch.x - fishMiddlePoint.x) + (touch.y - fishMiddlePoint.y) * (touch.y - fishMiddlePoint.y)))));

        System.out.println(angleCos + "angleCos");

        //要的转动的角度，示意图中的角AOB
        float temAngle = (float) Math.toDegrees(Math.acos(angleCos));
        //判断方向  正左侧  负右侧 0线上,但是Android的坐标系Y是朝下的，所以左右颠倒一下
        //向量共线定理，x1y2-x2y1
        float direction = (fishMiddlePoint.x - touch.x) * (fishHeadPointF.y - touch.y) - (fishMiddlePoint.y - touch.y) * (fishHeadPointF.x - touch.x);

        if (direction == 0) {
            //共线
            if (abc >= 0) {
                //abc大于0说明0到90度内，这里就是正方向的意思
                return 0;
            } else
                return 180;
        } else {
            if (direction > 0) {//右侧顺时针为负
                //x1y2>x2y1说明点在线的右边，考虑到Android的角度正方向是逆时针，示意图中是顺时针的角度这里直接取反减少干扰
                return -temAngle;
            } else {
                return temAngle;
            }
        }
    }

    /**
     * 获取屏幕宽高
     */
    private void getScreenParams() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
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

}
