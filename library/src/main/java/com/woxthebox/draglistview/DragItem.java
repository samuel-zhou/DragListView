/**
 * Copyright 2014 Magnus Woxblom
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.woxthebox.draglistview;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class DragItem {
    protected static final int ANIMATION_DURATION = 250;
    public static final String DRAG_VIEW_TAG = "drag_view";
    private View mDragView;

    private float mOffsetX;
    private float mOffsetY;
    //dragView的中心点位置，不包含offset，随着拖动会变化
    private float mPosX;
    private float mPosY;
    private float mPosTouchDx;
    private float mPosTouchDy;
    private float mAnimationDx = 0;  //已在startDrag中已禁止
    private float mAnimationDy = 0;  //已在startDrag中已禁止
    private boolean mCanDragHorizontally = true;  //是否可以横向拖动
    private boolean mSnapToTouch = true;  //开始的时候移动到触摸的位置，已改为不起作用

    public DragItem(Context context) {
        mDragView = new View(context);
        mDragView.setTag(DRAG_VIEW_TAG);
        hide();
    }

    public DragItem(Context context, int layoutId) {
        mDragView = View.inflate(context, layoutId, null);
        mDragView.setTag(DRAG_VIEW_TAG);
        hide();
    }

    //将要拖动的view的图像复制到dragView上
    public void onBindDragView(View clickedView, View dragView) {
        Bitmap bitmap = Bitmap.createBitmap(clickedView.getWidth(), clickedView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        clickedView.draw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dragView.setBackground(new BitmapDrawable(clickedView.getResources(), bitmap));
        } else {
            //noinspection deprecation
            dragView.setBackgroundDrawable(new BitmapDrawable(clickedView.getResources(), bitmap));
        }
    }

    //设置dragView的size
    public void onMeasureDragView(View clickedView, View dragView) {
        dragView.setLayoutParams(new FrameLayout.LayoutParams(clickedView.getMeasuredWidth(), clickedView.getMeasuredHeight()));
        int widthSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(clickedView.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
        dragView.measure(widthSpec, heightSpec);
    }

    public void onStartDragAnimation(View dragView) {
    }

    public void onEndDragAnimation(View dragView) {
    }

    boolean canDragHorizontally() {
        return mCanDragHorizontally;
    }

    void setCanDragHorizontally(boolean canDragHorizontally) {
        mCanDragHorizontally = canDragHorizontally;
    }

    boolean isSnapToTouch() {
        return mSnapToTouch;
    }

    void setSnapToTouch(boolean snapToTouch) {
        mSnapToTouch = snapToTouch;
    }

    View getDragItemView() {
        return mDragView;
    }

    void show() {
        mDragView.setVisibility(View.VISIBLE);
    }

    void hide() {
        mDragView.setVisibility(View.GONE);
    }

    void startDrag(View startFromView, float touchX, float touchY) {
        show();
        onBindDragView(startFromView, mDragView);
        onMeasureDragView(startFromView, mDragView);
        onStartDragAnimation(mDragView);

        //getX() = TranslationX + getLeft(); getY()=TranslationY+getTop();
        float startX = startFromView.getX() - (mDragView.getMeasuredWidth() - startFromView.getMeasuredWidth()) / 2 + mDragView
                .getMeasuredWidth() / 2;
        float startY = startFromView.getY() - (mDragView.getMeasuredHeight() - startFromView.getMeasuredHeight()) / 2 + mDragView
                .getMeasuredHeight() / 2;
//        Log.d("===","startX="+startX+" startY="+startY);
//        if (mSnapToTouch) {
//            mPosTouchDx = 0;
//            mPosTouchDy = 0;
//            setPosition(touchX, touchY);
//            setAnimationDx(startX - touchX);
//            setAnimationDY(startY - touchY);
//
//            PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("AnimationDx", mAnimationDx, 0);
//            PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("AnimationDY", mAnimationDy, 0);
//            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
//            anim.setInterpolator(new DecelerateInterpolator());
//            anim.setDuration(ANIMATION_DURATION);
//            anim.start();
//        } else {
            mPosTouchDx = startX - touchX;
            mPosTouchDy = startY - touchY;
            setPosition(touchX, touchY);
//        }
    }

    void endDrag(View endToView, AnimatorListenerAdapter listener) {
        onEndDragAnimation(mDragView);

        float endX = endToView.getX() - (mDragView.getMeasuredWidth() - endToView.getMeasuredWidth()) / 2 + mDragView
                .getMeasuredWidth() / 2;
        float endY = endToView.getY() - (mDragView.getMeasuredHeight() - endToView.getMeasuredHeight()) / 2 + mDragView
                .getMeasuredHeight() / 2;
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("X", mPosX, endX);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("Y", mPosY, endY);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, pvhX, pvhY);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(ANIMATION_DURATION);
        anim.addListener(listener);
        anim.start();
    }

    void setAnimationDx(float x) {
        mAnimationDx = x;
        updatePosition();
    }

    void setAnimationDY(float y) {
        mAnimationDy = y;
        updatePosition();
    }

    @SuppressWarnings("unused")
    void setX(float x) {
        mPosX = x;
        updatePosition();
    }

    @SuppressWarnings("unused")
    void setY(float y) {
        mPosY = y;
        updatePosition();
    }

    float getX() {
        return mPosX;
    }

    float getY() {
        return mPosY;
    }

    void setPosition(float touchX, float touchY) {
        mPosX = touchX + mPosTouchDx;
        mPosY = touchY + mPosTouchDy;
//        Log.d("===","mPosX,mPosY = "+mPosX+","+mPosY);
        updatePosition();
    }

    void setOffset(float offsetX, float offsetY) {
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        updatePosition();
    }

    void updatePosition() {
        //控制dragView显示的位置的代码
        if (mCanDragHorizontally) {
            mDragView.setX(mPosX + mOffsetX + mAnimationDx - mDragView.getMeasuredWidth() / 2);
        }else {
            mDragView.setX(mOffsetX);
        }

        mDragView.setY(mPosY + mOffsetY + mAnimationDy - mDragView.getMeasuredHeight() / 2);
        mDragView.invalidate();
    }
}
