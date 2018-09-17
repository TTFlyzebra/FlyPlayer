package com.jancar.media.adpater;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.data.Music;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.AnimationImageView;
import com.jancar.media.view.MarqueeTextView;

import java.util.List;

/**
 * ExpandableListView 适配器
 */
public class MusicSingerAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> groupList;
    private List<List<Music>> itemList;
    public MusicSingerAdapter(Context context, List<String> groupList, List<List<Music>> itemList) {
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
        return itemList.get(groupPosition).size();
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
            convertView = View.inflate(mContext, R.layout.explist_music_item_group, null);
            holder.imageView1 = (ImageView) convertView.findViewById(R.id.item_iv01);
            holder.textView1 = (MarqueeTextView) convertView.findViewById(R.id.item_tv01);
            holder.imageView2 = (ImageView) convertView.findViewById(R.id.item_iv02);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolderGroup) convertView.getTag();
        }
        if (isExpanded) {
            holder.imageView2.setImageResource(R.drawable.media_down);
        } else {
            holder.imageView2.setImageResource(R.drawable.media_right);
        }

        String str = groupList.get(groupPosition);
        holder.textView1.setText(str+"("+itemList.get(groupPosition).size()+")");
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        ViewHolderChild holder = new ViewHolderChild();
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.explist_music_item_child, null);
            holder.imageView = (AnimationImageView) convertView.findViewById(R.id.item_iv01);
            holder.textView1 = (MarqueeTextView) convertView.findViewById(R.id.item_tv01);
            holder.textView2 = (TextView) convertView.findViewById(R.id.item_tv02);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolderChild) convertView.getTag();
        }

        holder.textView1.setText(StringTools.getNameByPath(itemList.get(groupPosition).get(childPosition).url));

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

    private class ViewHolderChild {
        public AnimationImageView imageView;
        public MarqueeTextView textView1;
        public TextView textView2;
    }

    private class ViewHolderGroup {
        public ImageView imageView1;
        public MarqueeTextView textView1;
        public ImageView imageView2;
    }

}
