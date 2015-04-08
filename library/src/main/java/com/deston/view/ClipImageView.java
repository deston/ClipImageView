package com.deston.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ClipImageView extends ImageView {
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mDisplayTop;
    private int mDisplayLeft;
    private HighLightView mHighLightView;
    public ClipImageView(Context context) {
        super(context);
        init();
    }

    public ClipImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClipImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        mHighLightView = new HighLightView(this);
    }


    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        onFrameSet();
        return changed;
    }

    protected void onFrameSet() {
        float matrix[] = new float[9];

        getImageMatrix().getValues(matrix);
        float scaleX = matrix[Matrix.MSCALE_X];
        float scaleY = matrix[Matrix.MSCALE_Y];

        float originW = getDrawable().getIntrinsicWidth();
        float originH = getDrawable().getIntrinsicHeight();

        mDisplayWidth = Math.round(scaleX * originW);
        mDisplayHeight = Math.round(scaleY * originH);
        mDisplayLeft = (int) matrix[Matrix.MTRANS_X];
        mDisplayTop = (int) matrix[Matrix.MTRANS_Y];

        mHighLightView.initBounds(mDisplayLeft, mDisplayTop, mDisplayWidth, mDisplayHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mHighLightView.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mHighLightView.onTouchEvent(event);
    }

    public Bitmap getClipBitmap() {
        return getClipBitmap(mHighLightView.getRectLeft(), mHighLightView.getRectTop(), mHighLightView.getRectLength(), mHighLightView.getRectLength());
    }

    private Bitmap getClipBitmap(float left, float top, float width, float height) {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        Bitmap displayBitmap = Bitmap.createBitmap(drawable.getBitmap(), 0, 0, drawable.getBitmap().getWidth(), drawable.getBitmap().getHeight(), getImageMatrix(), true);
        Bitmap result = Bitmap.createBitmap(displayBitmap, (int) left - mDisplayLeft, (int) top - mDisplayTop, (int) width, (int) height);
        displayBitmap.recycle();
        return result;
    }

}
