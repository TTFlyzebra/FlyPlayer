package com.jancar.media.adpater;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jancar.media.R;
import com.jancar.media.activity.PhotoActivity;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.MarqueeTextView;

import java.util.List;

/**
 * GridView 适配器
 */
public class PhotoFloderItemAdapter extends BaseAdapter {
    private Context mContext;

    /**
     * 每个分组下的每个子项的 GridView 数据集合
     */
    private List<String> itemGridList;

    public PhotoFloderItemAdapter(Context mContext, List<String> itemGridList) {
        this.mContext = mContext;
        this.itemGridList = itemGridList;
    }

    @Override
    public int getCount() {
        return itemGridList == null ? 0 : itemGridList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemGridList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.item_video2, null);
            viewHolder.textView = (MarqueeTextView) convertView.findViewById(R.id.tv_gridview);
            viewHolder.imageView1 = (ImageView) convertView.findViewById(R.id.iv_gridview);
            viewHolder.imageView2 = (ImageView) convertView.findViewById(R.id.item_iv01_back);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String url = itemGridList.get(position);
        boolean flag = url.equals(((PhotoActivity)mContext).CRET_URL);
        viewHolder.textView.setText(StringTools.getNameByPath(url));
        viewHolder.textView.setTextColor(flag ? 0xFF0370E5 : 0xFFFFFFFF);
        viewHolder.textView.enableMarquee(flag);

        Glide.with(mContext).load(url).placeholder(R.drawable.media_default_image).into(viewHolder.imageView1);
        viewHolder.imageView2.setImageResource(flag ? R.drawable.media_list_item_select_02 : R.drawable.media_list_item_select_01);
        return convertView;
    }

    private class ViewHolder {
        public MarqueeTextView textView;
        public ImageView imageView1;
        public ImageView imageView2;
    }

}
