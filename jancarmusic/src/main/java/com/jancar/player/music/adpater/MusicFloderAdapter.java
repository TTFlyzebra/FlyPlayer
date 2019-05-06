package com.jancar.player.music.adpater;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.AnimationImageView;
import com.jancar.media.view.MarqueeTextView;
import com.jancar.player.music.R;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * ExpandableListView 适配器
 */
public class MusicFloderAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<String> groupList;
    private List<List<String>> itemList;
    private int focusColor, nofocusColor;

    public MusicFloderAdapter(Context context, List<String> groupList,
                              List<List<String>> itemList) {
        mContext = context;
        this.groupList = groupList;
        this.itemList = itemList;
        focusColor = mContext.getResources().getColor(R.color.text_focus);
        nofocusColor = mContext.getResources().getColor(R.color.text_no_focus);
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

    @SuppressLint("StringFormatInvalid")
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
        holder.textView1.setText(last>0?path.substring(last + 1, path.length()):path);
        holder.textView2.setText(last>0?path.substring(0, last):path);
        holder.textView1.enableMarquee(isExpanded);
        holder.textView2.enableMarquee(isExpanded);
        if (isExpanded) {
            holder.textView1.setTextColor(focusColor);
            holder.textView2.setTextColor(focusColor);
            holder.textView3.setTextColor(focusColor);
        } else {
            holder.textView1.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor));
            holder.textView2.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor));
            holder.textView3.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor));
        }
        try{
            holder.textView3.setText(String.format(mContext.getString(R.string.musicsumformat), itemList.get(groupPosition).size()));
        }catch (Exception e){
            holder.textView3.setText(String.format(Locale.ENGLISH,"(%d)", itemList.get(groupPosition).size()));
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


        convertView.setTag(R.id.tag1, itemList.get(groupPosition).get(childPosition));
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

        if (flag) {
            holder.textView1.setTextColor(focusColor);
        } else {
            holder.textView1.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor));
        }
        holder.textView1.enableMarquee(flag);
        holder.imageView.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
        return convertView;
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, String string);
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
        public AnimationImageView imageView;
        public MarqueeTextView textView1;
        public TextView textView2;
    }

}
