package com.jancar.media.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.model.storage.Storage;

import java.util.List;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class StorageAdapater extends RecyclerView.Adapter<StorageAdapater.ViewHolder> {
    private List<StorageInfo> mList;
    private Context mContext;
    private OnItemClickListener onItemClickListener;
    private String path = "";

    public void setCurrentPath(String path) {
        this.path = path == null ? "" : path;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public StorageAdapater(Context context, List<StorageInfo> list) {
        mContext = context;
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        StorageInfo storage = mList.get(position);
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, (Integer) v.getTag());
                }
            }
        });

        if(storage.mPath.equals(Storage.ALL_STORAGE)){
            holder.imageView.setImageResource(R.drawable.media_storge);
        }else{
            holder.imageView.setImageResource(storage.isRemoveable ? R.drawable.media_usb : R.drawable.media_disk);
        }
        holder.textView.setText(storage.mDescription);
        boolean flag = path.equals(storage.mPath);
        holder.itemView.setSelected(flag);
        holder.itemView.setEnabled(!flag);
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
