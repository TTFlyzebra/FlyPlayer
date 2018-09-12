package com.jancar.media.adpater;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.model.MusicPlayer;

import java.io.File;
import java.util.List;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class MusicPlayListAdapter extends RecyclerView.Adapter<MusicPlayListAdapter.ViewHolder> {
    private List<String> mList;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MusicPlayListAdapter(Context context, List<String> list, RecyclerView recyclerView) {
        mContext = context;
        mList = list;
        mRecyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_single, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String url = mList.get(position);
        int start = url.lastIndexOf(File.separator)+1;
        int end = url.lastIndexOf('.');
        start = Math.max(0,start);
        end = Math.max(0,end);
        start = Math.min(start,url.length()-1);
        end = Math.min(end,url.length()-1);
        String name = url.substring(start,end);
        holder.textView01.setText(name);

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(v, (Integer) v.getTag());
                }
            }
        });
        boolean flag = url.equals(MusicPlayer.getInstance().getPlayUrl());
        holder.imageView.setVisibility(flag?View.VISIBLE:View.INVISIBLE);
        holder.itemView.setSelected(flag);
    }


    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView01;
        TextView textView02;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_iv01);
            textView01 = (TextView) itemView.findViewById(R.id.item_tv01);
            textView02 = (TextView) itemView.findViewById(R.id.item_tv02);

        }
    }


}
