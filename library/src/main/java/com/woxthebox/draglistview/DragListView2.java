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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class DragListView2 extends FrameLayout {

    public interface DragListListener {
        void onItemDragStarted(int position);

        void onItemDragging(int itemPosition, float x, float y);

        void onItemDragEnded(int fromPosition, int toPosition);

        void onItemDragRemoved(int itemPosition,Object itemData);
    }

    public static abstract class DragListListenerAdapter implements DragListListener {
        @Override
        public void onItemDragStarted(int position) {
        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y) {
        }

        @Override
        public void onItemDragEnded(int fromPosition, int toPosition) {
        }

        @Override
        public void onItemDragRemoved(int itemPosition,Object itemData){}
    }

    public interface DragListCallback {
        boolean canDragItemAtPosition(int dragPosition);

        boolean canDropItemAtPosition(int dropPosition);

        boolean onHandleMoveInLeftView(float x,float y);

        boolean onHandleMoveEndInLeftView(float x,float y);
    }

    public static abstract class DragListCallbackAdapter implements DragListCallback {
        @Override
        public boolean canDragItemAtPosition(int dragPosition) {
            return true;
        }

        @Override
        public boolean canDropItemAtPosition(int dropPosition) {
            return true;
        }
    }

    private DragItemRecyclerView mRecyclerView;
    private DragListListener mDragListListener;
    private DragListCallback mDragListCallback;
    private DragItem mDragItem;
    private float mTouchX;   //DragListView中的触摸点X
    private float mTouchY;   //DragListView中的触摸点Y

    public DragListView2(Context context) {
        super(context);
    }

    public DragListView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragListView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LinearLayout container = new LinearLayout(getContext());
        leftView = new FrameLayout(getContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(leftView,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT));
        addView(container,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

        mDragItem = new DragItem(getContext());
        mRecyclerView = createRecyclerView();
        mRecyclerView.setDragItem(mDragItem);
        container.addView(mRecyclerView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        addView(mDragItem.getDragItemView());
    }

    private FrameLayout leftView;

    public FrameLayout getLeftView(){
        return leftView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean retValue = handleTouchEvent(event);
        return retValue || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = handleTouchEvent(event);
        return retValue || super.onTouchEvent(event);
    }

    private boolean handleTouchEvent(MotionEvent event) {
        mDragItem.setOffset(mRecyclerView.getLeft(),0);
        mTouchX = event.getX();
        mTouchY = event.getY();
//        Log.d("===","DragListView onTouchX,onTouchY = "+mTouchX+","+mTouchY+"  mRecycleView.getLeft()="+mRecyclerView.getLeft());
        if (isDragging()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if(isDragInLeftView()) {
                        float x = Math.max(leftView.getLeft(),getDragItemLeft());
                        float y = Math.max(leftView.getTop(),getDragItemTop());
                        if(mDragListCallback != null){
                            mDragListCallback.onHandleMoveInLeftView(x,y);
                        }
                        mRecyclerView.draggingOutOfRecyclerView(event.getX(), event.getY());
                    }else {
                        mRecyclerView.onDragging(event.getX(), event.getY());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    boolean handleInRecycle = true;
                    if(isDragInLeftView()){
                        if(mDragListCallback != null){
                            float x = Math.max(leftView.getLeft(),getDragItemLeft());
                            float y = Math.max(leftView.getTop(),getDragItemTop());
                            boolean handled = mDragListCallback.onHandleMoveEndInLeftView(x,y);
                            handleInRecycle = !handled;
                        }
                    }
                    if(handleInRecycle) {
                        mRecyclerView.onDragEnded();
                    }else {
                        mRecyclerView.onDragRemovedAndEnded();
                    }

                    break;
            }
            return true;
        }
        return false;
    }

    //dragItem超过recyclerView左边一定的距离
    private boolean isDragInLeftView(){
        float dragItemLeft = getDragItemLeft();
        float dragItemTop = getDragItemTop();
        if(dragItemLeft < mRecyclerView.getLeft()*0.7) {
            return true;
        }
        return false;
    }

    private float getDragItemLeft(){
        float left = mDragItem.getX()-mDragItem.getDragItemView().getMeasuredWidth()/2+mRecyclerView.getLeft();
        return left;
    }

    private float getDragItemTop(){
        float top = mDragItem.getY()-mDragItem.getDragItemView().getMeasuredHeight()/2;
        return top;
    }

    private DragItemRecyclerView createRecyclerView() {
        final DragItemRecyclerView recyclerView = (DragItemRecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.drag_item_recycler_view, this, false);
        recyclerView.setMotionEventSplittingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);
        recyclerView.setDragItemListener(new DragItemRecyclerView.DragItemListener() {
            private int mDragStartPosition;

            @Override
            public void onDragStarted(int itemPosition, float x, float y) {
                getParent().requestDisallowInterceptTouchEvent(true);
                mDragStartPosition = itemPosition;
                if (mDragListListener != null) {
                    mDragListListener.onItemDragStarted(itemPosition);
                }
            }

            @Override
            public void onDragging(int itemPosition, float x, float y) {
                if (mDragListListener != null) {
                    mDragListListener.onItemDragging(itemPosition, x, y);
                }
            }

            @Override
            public void onDragEnded(int newItemPosition) {
                if (mDragListListener != null) {
                    mDragListListener.onItemDragEnded(mDragStartPosition, newItemPosition);
                }
            }

            @Override
            public void onDragRemovedAndEnded(int itemPosition,Object removedItemData) {
                if(mDragListListener != null){
                    mDragListListener.onItemDragRemoved(itemPosition,removedItemData);
                }
            }
        });
        recyclerView.setDragItemCallback(new DragItemRecyclerView.DragItemCallback() {
            @Override
            public boolean canDragItemAtPosition(int dragPosition) {
                if (mDragListCallback != null) {
                    return mDragListCallback.canDragItemAtPosition(dragPosition);
                }
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int dropPosition) {
                if (mDragListCallback != null) {
                    return mDragListCallback.canDropItemAtPosition(dropPosition);
                }
                return true;
            }
        });
        return recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public DragItemAdapter getAdapter() {
        if (mRecyclerView != null) {
            return (DragItemAdapter) mRecyclerView.getAdapter();
        }
        return null;
    }

    public void setAdapter(DragItemAdapter adapter, boolean hasFixedItemSize) {
        mRecyclerView.setHasFixedSize(hasFixedItemSize);
        mRecyclerView.setAdapter(adapter);
        //由DragItemAdapter中ViewHolder的GrabView的onTouch事件触发(RecycleView)，调用DragStartCallback的startDrag方法，
        // 之后Touch事件被DragListView处理
        adapter.setDragStartedListener(new DragItemAdapter.DragStartCallback() {
            @Override
            public boolean startDrag(View itemView, long itemId) {
                return mRecyclerView.startDrag(itemView, itemId, mTouchX, mTouchY);
            }

            @Override
            public boolean isDragging() {
                return mRecyclerView.isDragging();
            }
        });
    }

    public void setLayoutManager(RecyclerView.LayoutManager layout) {
        mRecyclerView.setLayoutManager(layout);
    }

    public void setDragListListener(DragListListener listener) {
        mDragListListener = listener;
    }

    public void setDragListCallback(DragListCallback callback) {
        mDragListCallback = callback;
    }

    public boolean isDragEnabled() {
        return mRecyclerView.isDragEnabled();
    }

    public void setDragEnabled(boolean enabled) {
        mRecyclerView.setDragEnabled(enabled);
    }

    public void setCustomDragItem(DragItem dragItem) {
        int indexOfDragView = -1;
        int childViewCount = getChildCount();
        for(int i=0;i<childViewCount;i++){
            View view = getChildAt(i);
            if(DragItem.DRAG_VIEW_TAG.equals(view.getTag())){
                indexOfDragView = i;
                break;
            }
        }
        if(indexOfDragView != -1) {
            removeViewAt(indexOfDragView);
        }

        DragItem newDragItem;
        if (dragItem != null) {
            newDragItem = dragItem;
        } else {
            newDragItem = new DragItem(getContext());
        }

        newDragItem.setCanDragHorizontally(mDragItem.canDragHorizontally());
        newDragItem.setSnapToTouch(mDragItem.isSnapToTouch());
        mDragItem = newDragItem;
        mRecyclerView.setDragItem(mDragItem);
        addView(mDragItem.getDragItemView());
    }

    public boolean isDragging() {
        return mRecyclerView.isDragging();
    }

    public void setCanDragHorizontally(boolean canDragHorizontally) {
        mDragItem.setCanDragHorizontally(canDragHorizontally);
    }

    public void setSnapDragItemToTouch(boolean snapToTouch) {
        mDragItem.setSnapToTouch(snapToTouch);
    }

    public void setCanNotDragAboveTopItem(boolean canNotDragAboveTop) {
        mRecyclerView.setCanNotDragAboveTopItem(canNotDragAboveTop);
    }

    public void setCanNotDragBelowBottomItem(boolean canNotDragBelowBottom) {
        mRecyclerView.setCanNotDragBelowBottomItem(canNotDragBelowBottom);
    }

    public void setScrollingEnabled(boolean scrollingEnabled) {
        mRecyclerView.setScrollingEnabled(scrollingEnabled);
    }

    /**
     * Set if items should not reorder automatically when dragging. If reorder is disabled, drop target
     * drawables can be set with {@link #setDropTargetDrawables} which will highlight the current item that
     * will be swapped when dropping. By default items will reorder automatically when dragging.
     *
     * @param disableReorder True if reorder of items should be disabled, false otherwise.
     */
    public void setDisableReorderWhenDragging(boolean disableReorder) {
        mRecyclerView.setDisableReorderWhenDragging(disableReorder);
    }

    /**
     * If {@link #setDisableReorderWhenDragging} has been set to True then a background and/or foreground drawable
     * can be provided to highlight the current item which will be swapped when dropping. These drawables
     * will be drawn as decorations in the RecyclerView and will not interfere with the items own background
     * and foreground drawables.
     *
     * @param backgroundDrawable The background drawable for the item that will be swapped.
     * @param foregroundDrawable The foreground drawable for the item that will be swapped.
     */
    public void setDropTargetDrawables(Drawable backgroundDrawable, Drawable foregroundDrawable) {
        mRecyclerView.setDropTargetDrawables(backgroundDrawable, foregroundDrawable);
    }

    public void setAutoScrollWhenReachEdge(boolean autoScrollWhenReachEdge){
        mRecyclerView.setAutoScrollWhenReachEdge(autoScrollWhenReachEdge);
    }
}
