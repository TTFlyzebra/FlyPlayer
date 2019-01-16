package com.jancar.player.video.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.data.Video;
import com.jancar.media.module.DoubleBitmapCache;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.jancar.player.video.R;
import com.jancar.player.video.VideoActivity_AP1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class VideoPlayListAdapater extends RecyclerView.Adapter<VideoPlayListAdapater.ViewHolder> {
    private static final int smallImageWidth = 192;
    private static final int smallImageHeight = 108;
    private List<Video> mList;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private Set<GetVideoBitmatTask> tasks = new HashSet<>();
    private DoubleBitmapCache doubleBitmapCache;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public VideoPlayListAdapater(Context context, List<Video> list, RecyclerView recyclerView) {
        mContext = context;
        mList = list;
        mRecyclerView = recyclerView;
        doubleBitmapCache = DoubleBitmapCache.getInstance(context.getApplicationContext());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case SCROLL_STATE_IDLE:
                        update();
                        break;
                    default:
                        cancleAllTask();
                        break;
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final String url = mList.get(position).url;
        holder.itemView.setTag(position);
        holder.imageView.setTag(url);
        holder.textView.setText(StringTools.getNameByPath(url));
        Bitmap bitmap = doubleBitmapCache.get(url);
        if (null != bitmap) {
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(R.drawable.media_default_video);
            GetVideoBitmatTask task = new GetVideoBitmatTask(mList.get(position).url);
            task.execute(mList.get(position).url);
            tasks.add(task);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, (Integer) v.getTag());
                }
            }
        });

        if (url.equals(((VideoActivity_AP1) mContext).player.getPlayUrl())) {
            holder.itemView.setSelected(true);
        } else {
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

    public void cancleAllTask() {
        for (GetVideoBitmatTask task : tasks) {
            task.cancel(true);
        }
        tasks.clear();
    }

    public void loadImageView(int first, int last) {
        FlyLog.d("loadImageView %d-%d", first, last);
        try {
            if (mList == null || first < 0 || first >= mList.size() || last < 0 || last >= mList.size()) {
                return;
            }
            for (int i = first; i <= last; i++) {
                Bitmap bitmap = doubleBitmapCache.get(mList.get(i).url);
                if (null != bitmap) {
                    ImageView imageView = (ImageView) mRecyclerView.findViewWithTag(mList.get(i));
                    if (null != imageView) {
                        imageView.setImageBitmap(bitmap);
                    }
                } else {
                    GetVideoBitmatTask task = new GetVideoBitmatTask(mList.get(i).url);
                    task.execute(mList.get(i).url);
                    tasks.add(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void update() {
        cancleAllTask();
        this.notifyDataSetChanged();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int first = ((GridLayoutManager) (mRecyclerView.getLayoutManager())).findFirstVisibleItemPosition();
                int last = ((GridLayoutManager) (mRecyclerView.getLayoutManager())).findLastVisibleItemPosition();
                if (first >= 0 && last >= first) {
                    loadImageView(first, last);
                }
            }
        }, 0);

    }


    public class GetVideoBitmatTask extends AsyncTask<String, Bitmap, Bitmap> {
        private String url;

        GetVideoBitmatTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                final String path = strings[0];
                bitmap = ThumbnailUtils.createVideoThumbnail(path, MINI_KIND);
                if (bitmap == null) {
                    FlyLog.d("get bitmap from FFmpegMedia. url="+path);
                    FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                    mmr.setDataSource(path);
                    bitmap = mmr.getFrameAtTime();
                    if (bitmap != null) {
                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, smallImageWidth, smallImageHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                    }
                    mmr.release();
                }else{
                    FlyLog.d("get bitmap from ThumbnailUtils. url="+path);
                }
                if (bitmap != null) {
                    if (doubleBitmapCache != null) {
                        doubleBitmapCache.put(path, bitmap);
                    }
                }else{
                    FlyLog.d("get video bitmap Failed! url="+path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = (ImageView) mRecyclerView.findViewWithTag(url);
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

    }
}
