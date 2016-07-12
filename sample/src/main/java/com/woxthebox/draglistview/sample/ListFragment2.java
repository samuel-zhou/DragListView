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

package com.woxthebox.draglistview.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView2;

import java.util.ArrayList;

public class ListFragment2 extends Fragment {

    private ArrayList<Pair<Long, String>> mItemArray;
    private DragListView2 mDragListView;

    public static ListFragment2 newInstance() {
        return new ListFragment2();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout2, container, false);
        mDragListView = (DragListView2) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setSnapDragItemToTouch(true);
        mDragListView.setDragListListener(new DragListView2.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
//                Toast.makeText(mDragListView.getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
                Log.e("===","Start - position: " + position);
            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
//                if (fromPosition != toPosition) {
//                    Toast.makeText(mDragListView.getContext(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
//                }
                Log.e("===","End - position: " + toPosition);
                leftListAdapter.setFocusPosition(ListView.INVALID_POSITION);
            }

            @Override
            public void onItemDragRemoved(int itemPosition, Object itemData) {
//                Toast.makeText(mDragListView.getContext(), "removed - position: " + itemPosition, Toast.LENGTH_SHORT).show();
                Log.e("===","removed - position: " + itemPosition);
                leftListAdapter.setFocusPosition(ListView.INVALID_POSITION);
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y) {
//                super.onItemDragging(itemPosition, x, y);
                Log.e("===","dragging - position: "+itemPosition);
                leftListAdapter.setFocusPosition(ListView.INVALID_POSITION);

            }
        });

        mItemArray = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            mItemArray.add(new Pair<>(Long.valueOf(i), "Item " + i));
        }

        setupListRecyclerView();

        setupLeftListView(view);
        return view;
    }

    private LeftListAdapter leftListAdapter;
    private ListView leftListView;

    private void setupLeftListView(View view){
        final FrameLayout leftView = mDragListView.getLeftView();
        final ListView listView = (ListView) getActivity().getLayoutInflater().inflate(R.layout.listview,null);
        leftListView = listView;
        leftView.addView(listView);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)leftView.getLayoutParams();
        layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels*0.25);
        leftView.setLayoutParams(layoutParams);
        leftListAdapter = new LeftListAdapter();
        listView.setAdapter(leftListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("===","click "+position+" firstVisible pos="+parent.getFirstVisiblePosition());
            }
        });
        mDragListView.setDragListCallback(new DragListView2.DragListCallback() {
            @Override
            public boolean canDragItemAtPosition(int dragPosition) {
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int dropPosition) {
                return true;
            }

            @Override
            public boolean onHandleMoveInLeftView(float x, float y) {
                int position = listView.pointToPosition((int)x,(int)y);
                Log.e("===","positon = "+position);
                if(leftListAdapter.getCheckedPosition() == position){
                    return false;
                }
                leftListAdapter.setFocusPosition(position);
                return true;
            }

            @Override
            public boolean onHandleMoveEndInLeftView(float x, float y) {
                int position = listView.pointToPosition((int)x,(int)y);
                if(position != ListView.INVALID_POSITION){
                    if(leftListAdapter.getCheckedPosition() == position){
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("List and Grid");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_disable_drag).setVisible(mDragListView.isDragEnabled());
        menu.findItem(R.id.action_enable_drag).setVisible(!mDragListView.isDragEnabled());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_disable_drag:
                mDragListView.setDragEnabled(false);
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.action_enable_drag:
                mDragListView.setDragEnabled(true);
                getActivity().supportInvalidateOptionsMenu();
                return true;
            case R.id.action_list:
                setupListRecyclerView();
                return true;
            case R.id.action_grid_vertical:
                setupGridVerticalRecyclerView();
                return true;
            case R.id.action_grid_horizontal:
                setupGridHorizontalRecyclerView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListRecyclerView() {
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.list_item, R.id.item_layout, true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setAutoScrollWhenReachEdge(false);
        mDragListView.setCustomDragItem(new MyDragItem(getContext(), R.layout.list_item));
    }

    private void setupGridVerticalRecyclerView() {
        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setCustomDragItem(null);

    }

    private void setupGridHorizontalRecyclerView() {
        mDragListView.setLayoutManager(new GridLayoutManager(getContext(), 4, LinearLayoutManager.HORIZONTAL, false));
        ItemAdapter listAdapter = new ItemAdapter(mItemArray, R.layout.grid_item, R.id.item_layout, true);
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(true);
        mDragListView.setCustomDragItem(null);
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.text)).getText();
            ((TextView) dragView.findViewById(R.id.text)).setText(text);
            dragView.setBackgroundColor(dragView.getResources().getColor(R.color.app_color_transprent));
        }
    }

    private class LeftListAdapter extends BaseAdapter {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        private int leftCheckedPosition = 0;
        private int leftFocusPosition = ListView.INVALID_POSITION;

        @Override
        public int getCount() {
            return 16;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(R.layout.list_item_text,null);
            }
            TextView textView = (TextView)convertView.findViewById(R.id.text);
            textView.setText("D"+(position+1));
            if(leftCheckedPosition == position){
                textView.setTextColor(getResources().getColor(R.color.app_color));
//                convertView.setBackgroundColor(getResources().getColor(R.color.app_color));
            }else {
//                convertView.setBackgroundColor(getResources().getColor(R.color.black));
                textView.setTextColor(getResources().getColor(R.color.white));
            }
            if(leftFocusPosition == position){
                convertView.setBackgroundColor(getResources().getColor(R.color.red));
            }else {
                convertView.setBackgroundColor(getResources().getColor(R.color.black));
            }
            return convertView;
        }

        public void setFocusPosition(int position){
            if(leftFocusPosition != ListView.INVALID_POSITION){
                View view = leftListView.getChildAt(leftFocusPosition-leftListView.getFirstVisiblePosition());
                view.setBackgroundColor(getResources().getColor(R.color.black));
            }
            leftFocusPosition = position;
            if(position != ListView.INVALID_POSITION){
                View view = leftListView.getChildAt(position-leftListView.getFirstVisiblePosition());
                view.setBackgroundColor(getResources().getColor(R.color.red));
            }
        }

        public int getCheckedPosition(){
            return leftCheckedPosition;
        }

        public int getFocusPosition(){
            return leftFocusPosition;
        }
    }

}
