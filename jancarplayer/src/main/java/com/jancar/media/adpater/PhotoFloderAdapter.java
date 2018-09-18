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
import com.jancar.media.view.MarqueeTextView;

import java.io.File;
import java.util.List;

/**
 * ExpandableListView 适配器
 */
public class PhotoFloderAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> groupList;
    private List<List<String>> itemList;
    public PhotoFloderAdapter(Context context, List<String> groupList,
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
        ViewHolderGroup holder = new ViewHolderGroup();
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.explist_music_floder_item_group, null);
            holder.textView1 = (MarqueeTextView) convertView.findViewById(R.id.item_tv01);
            holder.textView2 = (MarqueeTextView) convertView.findViewById(R.id.item_tv02);
            holder.textView3 = (TextView) convertView.findViewById(R.id.item_tv03);
            holder.imageView1 = (ImageView) convertView.findViewById(R.id.item_iv01);
            holder.imageView2 = (ImageView) convertView.findViewById(R.id.item_iv02);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderGroup) convertView.getTag();
        }
        holder.imageView1.setImageResource(isExpanded ? R.drawable.media_file_02 : R.drawable.media_file);
        holder.imageView2.setImageResource(isExpanded ? R.drawable.media_down_02 : R.drawable.media_right);

        String path = groupList.get(groupPosition);
        int last = path.lastIndexOf(File.separator);
        holder.textView1.setText(path.substring(last + 1, path.length()));
        holder.textView2.setText(path.substring(0, last));
        holder.textView1.enableMarquee(isExpanded);
        holder.textView2.enableMarquee(isExpanded);
        if (isExpanded) {
            holder.textView1.setTextColor(0xFF0370E5);
            holder.textView2.setTextColor(0xFF0370E5);
            holder.textView3.setTextColor(0xFF0370E5);
        } else {
            holder.textView1.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
            holder.textView2.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
            holder.textView3.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
        }
        holder.textView3.setText(String.format(mContext.getString(R.string.photosumformat),itemList.get(groupPosition).size()));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        ViewHolderChild holder = new ViewHolderChild();
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.explist_video_item_child, null);
            holder.gridView = (GridView) convertView.findViewById(R.id.item_gv01);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolderChild) convertView.getTag();
        }
        PhotoFloderItemAdapter gridViewAdapter = new PhotoFloderItemAdapter(mContext, itemList.get(groupPosition));
        holder.gridView.setAdapter(gridViewAdapter);
        holder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    private class ViewHolderGroup {
        public ImageView imageView1;
        public ImageView imageView2;
        public MarqueeTextView textView1;
        public MarqueeTextView textView2;
        public TextView textView3;
    }

    private class ViewHolderChild {
        public GridView gridView;
    }

}
