package com.jancar.player.photo.adpater;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jancar.media.R;
import com.jancar.media.data.FloderImage;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.MarqueeTextView;
import com.jancar.player.photo.PhotoActivity;

import java.io.File;
import java.util.List;


/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class PhotoFloderAdapater extends RecyclerView.Adapter<ViewHolder> {
    private List<FloderImage> mList;
    private Context mContext;
    private int mColumnNum;
    private OnItemClickListener mOnItemClick;

    public interface OnItemClickListener {
        void onItemClick(View v, FloderImage floderImage);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClick) {
        mOnItemClick = onItemClick;
    }

    public PhotoFloderAdapater(Context context, List<FloderImage> list, int columnNum) {
        mContext = context;
        mList = list;
        mColumnNum = columnNum;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = null;
        switch (viewType) {
            case 1:
                View v0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.explist_music_floder_item_group, parent, false);
                viewHolder = new MenuHolder(v0);
                break;
            case 2:
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
                viewHolder = new PhotoHolder(v1);
                break;
            case 3:
                View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_none, parent, false);
                viewHolder = new PhotoHolder(v2);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        String crtUrl = ((PhotoActivity)mContext).CURRENT_IMAGE ==null?"":((PhotoActivity)mContext).CURRENT_IMAGE.url;
        if (holder instanceof PhotoHolder) {
            PhotoHolder photoHolder = (PhotoHolder) holder;
            Glide.with(mContext).load((mList.get(position)).url).into(photoHolder.imageView1);
            boolean flag = mList.get(position).url.equals(crtUrl);
            photoHolder.textView.setText(StringTools.getNameByPath(mList.get(position).url));
            photoHolder.textView.setTextColor(flag ? 0xFF0370E5 : 0xFFFFFFFF);
            photoHolder.textView.enableMarquee(flag);
            photoHolder.imageView2.setImageResource(flag ? R.drawable.media_list_item_select_02 : R.drawable.media_list_item_select_01);
        } else if (holder instanceof MenuHolder) {
            MenuHolder textHolder = (MenuHolder) holder;
            String path = mList.get(position).url;
            int last1 = path.lastIndexOf(File.separator);
            textHolder.textView1.setText(path.substring(last1 + 1, path.length()));
            textHolder.textView2.setText(path.substring(0, last1));
            textHolder.textView3.setText(String.format(mContext.getString(R.string.photosumformat),mList.get(position).sum));
            int last2 = crtUrl.lastIndexOf(File.separator);
            String selectPath = crtUrl.substring(0,last2);
            boolean flag = path.equals(selectPath);
            if (flag) {
                textHolder.textView1.setTextColor(0xFF0370E5);
                textHolder.textView2.setTextColor(0xFF0370E5);
                textHolder.textView3.setTextColor(0xFF0370E5);
            } else {
                textHolder.textView1.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
                textHolder.textView2.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
                textHolder.textView3.setTextColor(mContext.getResources().getColorStateList(R.color.textcolor_blue_white));
            }
            textHolder.imageView1.setImageResource(flag ? R.drawable.media_file_02 : R.drawable.media_file);
            textHolder.imageView2.setImageResource(mList.get(position).isSelect ?
                    flag?R.drawable.media_down_02:R.drawable.media_down_01
                    : flag?R.drawable.media_right_02:R.drawable.media_right_01);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClick != null) {
                    mOnItemClick.onItemClick(v, mList.get((Integer) v.getTag()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).type;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mList.get(position).type) {
                    case 1:
                        return mColumnNum;
                    case 2:
                        return 1;
                    case 3:
                        return mColumnNum;
                }
                return mColumnNum;
            }
        });
    }

    private static class PhotoHolder extends ViewHolder {
        public MarqueeTextView textView;
        public ImageView imageView1;
        public ImageView imageView2;

        public PhotoHolder(View itemView) {
            super(itemView);
            textView = (MarqueeTextView) itemView.findViewById(R.id.item_tv01);
            imageView1 = (ImageView) itemView.findViewById(R.id.item_iv01);
            imageView2 = (ImageView) itemView.findViewById(R.id.item_iv01_back);
        }
    }

    private static class NonePhotoHolder extends ViewHolder {
        public MarqueeTextView textView;
        public ImageView imageView1;
        public ImageView imageView2;

        public NonePhotoHolder(View itemView) {
            super(itemView);
            textView = (MarqueeTextView) itemView.findViewById(R.id.item_tv01);
            imageView1 = (ImageView) itemView.findViewById(R.id.item_iv01);
            imageView2 = (ImageView) itemView.findViewById(R.id.item_iv01_back);
        }
    }

    private static class MenuHolder extends ViewHolder {
        public ImageView imageView1;
        public ImageView imageView2;
        public MarqueeTextView textView1;
        public MarqueeTextView textView2;
        public TextView textView3;

        public MenuHolder(View itemView) {
            super(itemView);
            textView1 = (MarqueeTextView) itemView.findViewById(R.id.item_tv01);
            textView2 = (MarqueeTextView) itemView.findViewById(R.id.item_tv02);
            textView3 = (TextView) itemView.findViewById(R.id.item_tv03);
            imageView1 = (ImageView) itemView.findViewById(R.id.item_iv01);
            imageView2 = (ImageView) itemView.findViewById(R.id.item_iv02);
        }

    }
}
