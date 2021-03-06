package com.gc.buaa.tencentqq.drag;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * 侧滑面板
 * （继承FrameLayout，FrameLayout继承GroupView，通过继承FrameLayout来获得已经实现的测量、绘制、布局等方法）
 */
public class DragLayout extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private ViewGroup mLeftContent;
    private ViewGroup mMainContent;
    private int mHeight;
    private int mWidth;
    //移动的范围
    private int mRange;
    private Status mStatus = Status.Close;
    private OnDragStatusChangeListener mListener;

    /**
     * 状态枚举
     */
    public static enum Status {
        Close, Open, Draging;
    }

    public interface OnDragStatusChangeListener{
        void onClose();
        void onOpen();
        void onDraging(float percent);
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    public void setDragStatusListener(OnDragStatusChangeListener mListener){
        this.mListener = mListener;
    }

    public DragLayout(Context context) {
        this(context,null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        //a,初始化ViewDragHelper
        viewDragHelper = ViewDragHelper.create(this, mCallback);


    }

    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        //c,重写回调事件

        //1，根据返回结果决定当前child是否可以拖拽
        /**
         * 尝试捕获View
         * 根据返回结果决定当前child是否可以拖拽
         * @param child 当前被拖拽的View
         * @param pointerId 区分多点触摸的id
         * @return 是否允许该View被拖拽
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
//            //只有是主面板情况下才可以拖拽
//            return child == mMainContent;

            return true;
        }


        /**
         * 当View被捕获时调用，即tryCaptureView返回true的View触发
         * @param capturedChild
         * @param activePointerId
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }


        /**
         * 水平方向拖拽范围
         * 返回拖拽的范围，不会对拖拽起到真真的限制，仅仅决定了动画的执行速度
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        //2,根据建议值修正将要移动到的位置(水平)
        /**
         * 根据建议值修正将要移动到的位置(水平)
         * 此时没有发生真正的移动
         * @param child 当前拖拽的View
         * @param left 新的位置左侧建议值
         * @param dx 水平x轴变化量
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            //只作用于主面板
            if(child == mMainContent){
                left = fixLeft(left);
            }

            return left;
        }


        /**
         * 根据建议值修正将要移动到的位置(竖直)
         * 此时没有发生真正的移动
         * @param child 当前拖拽的View
         * @param top 新的位置顶部建议值
         * @param dy 竖直y轴变化量
         * @return
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }

        //3,当View位置改变的时候调用（更新状态，伴随动画，重绘界面），此时发生了真正的移动
        /**
         * 当位置改变时绘制
         * 此时发生了真正的移动
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            int newLeft = left;
            //将左界面的拖拽情况传递给主界面
            if(changedView == mLeftContent){
                newLeft = mMainContent.getLeft() + dx;

                newLeft = fixLeft(newLeft);

                //拖拽过程中，可以获得左界面的拖拽数据，但是在强制左界面移动到初始位置
                mLeftContent.layout(0,0,mWidth,mHeight);

                mMainContent.layout(newLeft,0,mWidth + dx ,mHeight);
            }

            //更新状态执行动画
            dispatchDragEvent(newLeft);

            //为了兼容android低版本，每次修改值之后，进行重绘
            invalidate();
        }

        //4,当View拖拽被释放（执行动画）
        /**
         * 当View拖拽被释放（执行动画）
         * @param releasedChild 被释放的子View
         * @param xvel 水平方向的速度(向右为正)
         * @param yvel 竖直方向的速度（向下为正）
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            if(xvel == 0 && mMainContent.getLeft() > mRange/2.0f){
                open(true);
            }else if(xvel > 0){
                open(true);
            }else{
                close(true);
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
        }

    };

    /**
     * 执行动画，更新状态
     * @param newLeft
     */
    private void dispatchDragEvent(int newLeft) {
        float percent = newLeft * 1.0f / mRange;

        // 更新状态, 执行接口回调
        if(mListener != null){
            mListener.onDraging(percent);
        }
        Status preStatus = mStatus;
        mStatus = updateStatus(percent);
        if(mStatus != preStatus){
            // 状态发生变化
            if(mStatus == Status.Close){
                // 当前变为关闭状态
                if(mListener != null){
                    mListener.onClose();
                }
            }else if (mStatus == Status.Open) {
                if(mListener != null){
                    mListener.onOpen();
                }
            }
        }


        //伴随动画
        animViews(percent);
    }

    /**
     * 更新状态
     * @param percent 拖拽进度（百分比）
     * @return 新状态
     */
    private Status updateStatus(float percent) {
        if(percent == 0f){
            return Status.Close;
        }else if (percent == 1.0f) {
            return Status.Open;
        }
        return Status.Draging;
    }

    /**
     * 伴随动画
     * @param parcent 动画进度（百分比）
     */
    private void animViews(float parcent) {
        //左界面动画：缩放动画，平移动画，透明度动画

        //缩放动画（0.5f~1.0f）
        //mLeftContent.setScaleX(0.5f + 0.5f * parcent);
        //mLeftContent.setScaleY(0.5f + 0.5f * parcent);
        //使用nineoldandroids-2.4.0.jar兼容低版本
        ViewHelper.setScaleX(mLeftContent, evaluate(parcent, 0.5f, 1.0f));
        ViewHelper.setScaleY(mLeftContent, evaluate(parcent, 0.5f, 1.0f));

        //平移动画(-mWidth/2.0f~0)
        ViewHelper.setTranslationX(mLeftContent, evaluate(parcent, -mWidth / 2.0f ,0));

        //透明度动画(0.5f~1.0f)
        ViewHelper.setAlpha(mLeftContent, evaluate(parcent, 0.5f, 1.0f));

        //主界面动画：缩放动画

        //缩放动画
        ViewHelper.setScaleX(mMainContent,evaluate(parcent, 1.0f, 0.8f));
        ViewHelper.setScaleY(mMainContent, evaluate(parcent, 1.0f, 0.8f));

        //背景动画：颜色亮度渐变

        //颜色亮度渐变
        getBackground().setColorFilter((Integer) evaluateColor(parcent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);

    }

    /**
     * 颜色变化过度
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }

    /**
     * 计算阶段值(估值器)
     * @param fraction 阶段百分比
     * @param startValue 开始值
     * @param endValue 结尾值
     * @return
     */
    public Float evaluate(float fraction, Number startValue, Number endValue){
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        //II,持续执行平滑动画（高频率调用）
        if(viewDragHelper.continueSettling(true)){
            //如果返回true，则动画还要继续执行
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 开启侧边栏
     * @param isSmooth 是否平滑开启
     */
    public void open(boolean isSmooth) {
        int finalLeft = mRange;
        if(isSmooth){
            //I,触发一个平滑动画
            if(viewDragHelper.smoothSlideViewTo(mMainContent,finalLeft,0)){
                //返回true表示还没有移动到指定位置，需要刷新界面

                //参数传this（child所在的ViewGroup）
                ViewCompat.postInvalidateOnAnimation(this);
            }

        }else{
            mMainContent.layout(finalLeft,0,finalLeft + mWidth,mHeight);
        }
    }

    /**
     * 关闭侧边栏
     * @param isSmooth 是否平滑关闭
     */
    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if(isSmooth){
            //I,触发一个平滑动画
            if(viewDragHelper.smoothSlideViewTo(mMainContent,finalLeft,0)){
                //返回true表示还没有移动到指定位置，需要刷新界面

                //参数传this（child所在的ViewGroup）
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }else {
            mMainContent.layout(finalLeft,0,finalLeft + mWidth,mHeight);
        }
    }


    /**
     * 修正View的左端距离
     * @param left
     * @return
     */
    private int fixLeft(int left){
        if(left < 0){
            left = 0;
        }else if(left > mRange){
            left = mRange;
        }
        return left;
    }

    //b,传递触摸事件

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //把事件传递给viewDragHelper
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {
            viewDragHelper.processTouchEvent(event);
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 当XML布局加载完成时调用，此时所有的孩子View都已加载完毕
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //容错性检查
        if(getChildCount() < 2){
            //非法状态异常
            throw new IllegalStateException("布局至少有两个孩子。Your ViewGroup must have two children at least.");
        }
        if(!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)){
            //非法参数异常
            throw new IllegalArgumentException("子View必须是ViewGroup子类。Your children must be an instance of ViewGroup");
        }

        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);
    }

    /**
     * 当尺寸有变化的时候调用
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mHeight = getHeight();
        mWidth = getWidth();

        mRange = (int) (mWidth * 0.6f);
    }
}
