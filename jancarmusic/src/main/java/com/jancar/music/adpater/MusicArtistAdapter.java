package com.jancar.music.adpater;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.music.R;
import com.jancar.music.model.musicplayer.MusicPlayer;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.AnimationImageView;
import com.jancar.media.view.MarqueeTextView;

import java.util.List;

/**
 * ExpandableListView 适配器
 */
public class MusicArtistAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> groupList;
    private List<List<String>> itemList;

    public MusicArtistAdapter(Context context, List<String> groupList, List<List<String>> itemList) {
        mContext = context;
        this.groupList = groupList;
        this.itemList = itemList;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, String string);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup
            parent) {

        ViewHolderGroup holder = new ViewHolderGroup();
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.explist_music_artist_item_group, null);
            holder.imageView1 = (ImageView) convertView.findViewById(R.id.item_iv01);
            holder.textView1 = (MarqueeTextView) convertView.findViewById(R.id.item_tv01);
            holder.textView2 = (TextView) convertView.findViewById(R.id.item_tv02);
            holder.imageView2 = (ImageView) convertView.findViewById(R.id.item_iv02);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolderGroup) convertView.getTag();
        }
        holder.imageView1.setImageResource(isExpanded ? R.drawable.media_music_artist_02 : R.drawable.media_music_artist);
        holder.imageView2.setImageResource(isExpanded ? R.drawable.media_down_02 : R.drawable.media_right);
        if(isExpanded){
            holder.textView1.setTextColor(0xFF0370E5);
            holder.textView2.setTextColor(0xFF0370E5);
        }else{
            holder.textView1.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
            holder.textView2.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
        }
        String str = groupList.get(groupPosition);
        holder.textView1.setText(str);
        holder.textView1.enableMarquee(isExpanded);
        try {
            int size = itemList == null ? 0 : itemList.get(groupPosition) == null ? 0 : itemList.get(groupPosition).size();
            holder.textView2.setText(String.format(mContext.getString(R.string.musicsumformat), size));
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
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
        } else {
            holder = (ViewHolderChild) convertView.getTag();
        }

        convertView.setTag(R.id.tag1,itemList.get(groupPosition).get(childPosition));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, (String) v.getTag(R.id.tag1));
                }
            }
        });

        String url = itemList.get(groupPosition).get(childPosition);
        boolean flag = url.equals(MusicPlayer.getInstance().getPlayUrl());
        holder.textView1.setText(StringTools.getNameByPath(url));
        holder.textView1.setTextColor(flag ? 0xFF0370E5 : 0xFFFFFFFF);
        holder.textView1.enableMarquee(flag);
        holder.imageView.setVisibility(flag?View.VISIBLE:View.INVISIBLE);
        return convertView;
    }

    private class ViewHolderChild {
        public AnimationImageView imageView;
        public MarqueeTextView textView1;
        public TextView textView2;
    }

    private class ViewHolderGroup {
        public ImageView imageView1;
        public MarqueeTextView textView1;
        public TextView textView2;
        public ImageView imageView2;
    }

}
