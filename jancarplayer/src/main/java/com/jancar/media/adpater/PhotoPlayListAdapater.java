package com.jancar.media.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jancar.media.R;
import com.jancar.media.activity.PhotoActivity;
import com.jancar.media.utils.StringTools;

import java.util.List;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class PhotoPlayListAdapater extends RecyclerView.Adapter<PhotoPlayListAdapater.ViewHolder> {
    private List<String> mList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public PhotoPlayListAdapater(Context context, List<String> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String url = mList.get(position);
        holder.itemView.setTag(position);
        Glide.with(mContext).load(url).placeholder(R.drawable.media_default_image).into(holder.imageView);
        holder.textView.setText(StringTools.getNameByPath(url));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(v, (Integer) v.getTag());
                }
            }
        });

        if(url.equals(((PhotoActivity)mContext).CRET_URL)){
            holder.itemView.setSelected(true);
        }else{
            holder.itemView.setSelected(false);
        }
    }


    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_iv01);
            textView = (TextView) itemView.findViewById(R.id.item_tv01);
        }
    }


}
