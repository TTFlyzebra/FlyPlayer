package com.jancar.media.adpater;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.R;

import java.io.File;
import java.util.List;

/**
 * ExpandableListView 适配器
 */
public class VideoFloderAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> groupList;
    private List<List<String>> itemList;
    private GridView gridView;
    public VideoFloderAdapter(Context context, List<String> groupList,
                              List<List<String>> itemList) {
        mContext = context;
        this.groupList = groupList;
        this.itemList = itemList;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup
            parent) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.expandablelist_group, null);
        }
        ImageView ivGroup = (ImageView) convertView.findViewById(R.id.iv_group);
        if (isExpanded) {
            ivGroup.setImageResource(R.drawable.media_down);
        } else {
            ivGroup.setImageResource(R.drawable.media_right);
        }

        String path = groupList.get(groupPosition);
        int last = path.lastIndexOf(File.separator);
        TextView tvGroup1 = (TextView) convertView.findViewById(R.id.tv_group1);
        TextView tvGroup2 = (TextView) convertView.findViewById(R.id.tv_group2);
        tvGroup1.setText(path.substring(last+1,path.length()));
        tvGroup2.setText(path.substring(0,last));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.expandablelist_grid_item, null);
        }
        gridView = (GridView) convertView;
        VideoFloderItemAdapter gridViewAdapter = new VideoFloderItemAdapter(mContext, itemList.get(groupPosition),gridView);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(view,itemList.get(groupPosition).get((int) id));
                }
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, String url);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
