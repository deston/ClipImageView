package com.deston.view;

import android.graphics.*;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.deston.util.DeviceUtil;

public class HighLightView {
    public static final int TOUCH_AREA_ZOOM_CIRCLE = 1;//触摸区域在缩放圆形内
    public static final int TOUCH_AREA_RECTANGLE = 2;//触摸区域在矩形内
    public static final int TOUCH_AREA_BACKGROUND = 3;//触摸区域在背景上（即不在缩放圆形和矩形内）
    private Paint mRectPaint;
    private Paint mShaderPaint;
    private float mEdgeLength;//矩形边长
    private float mRectCenterX;//矩形中点X坐标
    private float mRectCenterY;//矩形中点Y坐标
    private float mRectMaxEdgeLength;
    private float mRectMinEdgeLength;
    private int mImageHeight;
    private int mImageWidth;
    private int mImageTop;
    private int mImageLeft;
    private float mLastMotionX;
    private float mLastMotionY;
    private Rect mRectBound = new Rect();
    private int mTouchArea;//触摸区域
    private float mRectEdgeWidth;
    private float mZoomCircleRadius;
    private ImageView mImageView;
    private PorterDuffXfermode mXfermode;

    public HighLightView(ImageView view) {
        this.mImageView = view;
        init();
    }

    public void draw(Canvas canvas) {
        onDraw(canvas);
    }


    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                mTouchArea = getTouchArea(event);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                handleMoveEvent(mLastMotionX, mLastMotionY, event.getX(), event.getY(), mTouchArea);
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                break;
        }

        return true;
    }

    public void initBounds(int imageLeft, int imageTop, int imageWidth, int imageHeight) {
        this.mImageHeight = imageHeight;
        this.mImageWidth = imageWidth;
        this.mImageLeft = imageLeft;
        this.mImageTop = imageTop;

        mRectCenterY = mImageTop + mImageHeight / 2;
        mRectCenterX = mImageLeft + mImageWidth / 2;

        mRectMaxEdgeLength = Math.min(mImageHeight, mImageWidth);
        mEdgeLength = mRectMaxEdgeLength * 2 / 3;//矩形边长
        mRectMinEdgeLength = mRectMaxEdgeLength / 2;
        mRectEdgeWidth = DeviceUtil.getPixelsFromDip(mImageView.getContext(), 1.5f);
        mZoomCircleRadius = DeviceUtil.getPixelsFromDip(mImageView.getContext(), 15f);

        invalidateRectBounds();
    }

    private void invalidateRectBounds() {
        mRectBound.left = (int) (mRectCenterX - mEdgeLength / 2);
        mRectBound.right = (int) (mRectCenterX + mEdgeLength / 2);
        mRectBound.top = (int) (mRectCenterY - mEdgeLength / 2);
        mRectBound.bottom = (int) (mRectCenterY + mEdgeLength / 2);
    }

    private void handleMoveEvent(float fromX, float fromY, float toX, float toY, int touchArea) {
        float xDiff = toX - fromX;
        float yDiff = toY - fromY;
        if (touchArea == TOUCH_AREA_RECTANGLE) {//触摸矩形内，移动
            mRectCenterX += xDiff;
            mRectCenterY += yDiff;
            invalidateRectBounds();
            boolean needRefreshOffset = false;
            if (mRectBound.right >= mImageLeft + mImageWidth && xDiff > 0) {  //移动到右边缘，继续往右移动的话，停止
                mRectCenterX = mImageLeft + mImageWidth - mEdgeLength / 2;
                needRefreshOffset = true;
            }
            if (mRectBound.left <= mImageLeft && xDiff < 0) { //移动到左边缘停止
                mRectCenterX = mImageLeft + mEdgeLength / 2;
                needRefreshOffset = true;
            }
            if (mRectBound.top <= mImageTop && yDiff < 0) { //移动到顶部停止
                mRectCenterY = mImageTop + mEdgeLength / 2;
                needRefreshOffset = true;
            }
            if (mRectBound.bottom >= mImageTop + mImageHeight && yDiff > 0) { //移动到底部
                mRectCenterY = mImageTop + mImageHeight - mEdgeLength / 2;
                needRefreshOffset = true;
            }
            if (needRefreshOffset) {
                invalidateRectBounds();
            }
            mImageView.invalidate();
        } else if (touchArea == TOUCH_AREA_ZOOM_CIRCLE) {
            boolean isExpand;
            float moveDistance = getDistance(fromX, fromY, toX, toY);
            //到矩形中点的距离变大表示扩大
            if (getDistance(toX, toY, mRectCenterX, mRectCenterY) - getDistance(fromX, fromY, mRectCenterX, mRectCenterY) > 0) {
                isExpand = true;
            } else {
                isExpand = false;
            }
            if (!isExpand) {
                moveDistance = -moveDistance;
            }
            mEdgeLength += moveDistance;
            invalidateRectBounds();
            boolean needRefreshOffset = false;
            //扩张到边缘时
            if (mRectBound.left <= mImageLeft) {
                mRectCenterX += Math.abs(xDiff);
                mEdgeLength = 2 * (mRectCenterX - mImageLeft);
                needRefreshOffset = true;
            }
            if (mRectBound.right >= mImageLeft + mImageWidth) {
                mRectCenterX -= Math.abs(xDiff);
                mEdgeLength = 2 * (mImageLeft + mImageWidth - mRectCenterX);
                needRefreshOffset = true;
            }
            if (mRectBound.bottom >= mImageTop + mImageHeight) {
                mRectCenterY -= Math.abs(xDiff);
                mEdgeLength = 2 * (mImageTop + mImageHeight - mRectCenterY);
                needRefreshOffset = true;
            }
            if (mRectBound.top <= mImageTop) {
                mRectCenterY += Math.abs(xDiff);
                mEdgeLength = 2 * (mRectCenterY - mImageTop);
                needRefreshOffset = true;
            }
            if (Math.abs(mEdgeLength) >= mRectMaxEdgeLength) { //扩大到最大
                mEdgeLength = mRectMaxEdgeLength;
                needRefreshOffset = true;
            } else if (Math.abs(mEdgeLength) < mRectMinEdgeLength) {//收缩到最小
                mEdgeLength = mRectMinEdgeLength;
                needRefreshOffset = true;
            }
            if (needRefreshOffset) {
                invalidateRectBounds();
            }
            mImageView.invalidate();
        }
    }


    private int getTouchArea(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        //判断是否在四个圆心内
        if (getDistance(x, y, mRectBound.left, mRectBound.top) <= mZoomCircleRadius
                || getDistance(x, y, mRectBound.left, mRectBound.bottom) <= mZoomCircleRadius
                || getDistance(x, y, mRectBound.right, mRectBound.top) <= mZoomCircleRadius
                || getDistance(x, y, mRectBound.right, mRectBound.bottom) <= mZoomCircleRadius) {
            return TOUCH_AREA_ZOOM_CIRCLE;
        }
        //判断是否在矩形内
        if (x >= mRectBound.left && x <= mRectBound.right && y >= mRectBound.top && y <= mRectBound.bottom) {
            return TOUCH_AREA_RECTANGLE;
        }

        return TOUCH_AREA_BACKGROUND;
    }


    private float getDistance(float fromX, float fromY, float toX, float torY) {
        return (float) (Math.sqrt((fromX - toX) * (fromX - toX) + (fromY - torY) * (fromY - torY)));
    }


    private void init() {
        mRectPaint = new Paint();
        setRectPaint(mRectPaint);
        mShaderPaint = new Paint();
        setBackgroundPaint(mShaderPaint);
    }

    private void setRectPaint(Paint paint) {
        paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(mRectEdgeWidth);
    }

    private void setBackgroundPaint(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(100);
    }


    public float getRectTop() {
        return mRectBound.top;
    }

    public float getRectBottom() {
        return mRectBound.bottom;
    }

    public float getRectLeft() {
        return mRectBound.left;
    }

    public float getRectRight() {
        return mRectBound.right;
    }

    public float getRectLength() {
        return mEdgeLength;
    }

    private void drawShaderBackground(Canvas canvas) {
        mShaderPaint.reset();
        int sc = canvas.saveLayer(mImageLeft, mImageTop, mImageLeft + mImageWidth, mImageTop + mImageHeight, mShaderPaint, Canvas.ALL_SAVE_FLAG);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);

        canvas.drawRect(mRectBound.left, mRectBound.top, mRectBound.right, mRectBound.bottom, mShaderPaint);
        mShaderPaint.setXfermode(mXfermode);
        setBackgroundPaint(mShaderPaint);

        canvas.drawRect(mImageLeft, mImageTop, mImageLeft + mImageWidth, mImageTop + mImageHeight, mShaderPaint);
        mShaderPaint.setXfermode(null);
        canvas.restoreToCount(sc);

    }

    protected void onDraw(Canvas canvas) {
        drawShaderBackground(canvas);
        drawEditRect(canvas);
    }


    private void drawEditRect(Canvas canvas) {
        canvas.drawRect(mRectBound.left, mRectBound.top, mRectBound.right, mRectBound.bottom, mRectPaint);
        canvas.drawCircle(mRectBound.left, mRectBound.top, mZoomCircleRadius, mRectPaint);
        canvas.drawCircle(mRectBound.left, mRectBound.bottom, mZoomCircleRadius, mRectPaint);
        canvas.drawCircle(mRectBound.right, mRectBound.top, mZoomCircleRadius, mRectPaint);
        canvas.drawCircle(mRectBound.right, mRectBound.bottom, mZoomCircleRadius, mRectPaint);
    }


}
