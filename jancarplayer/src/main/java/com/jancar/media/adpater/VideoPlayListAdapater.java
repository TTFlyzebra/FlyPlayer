package com.jancar.media.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
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

import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.data.Video;
import com.jancar.media.module.DoubleBitmapCache;
import com.jancar.media.utils.BitmapTools;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.ksyun.media.player.misc.KSYProbeMediaInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<GetDvrVideoBitmatTask> tasks = new HashSet<>();
    private DoubleBitmapCache doubleBitmapCache;
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
            holder.imageView.setImageResource(R.drawable.media_default_image);
            GetDvrVideoBitmatTask task = new GetDvrVideoBitmatTask(mList.get(position).url);
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

        if (url.equals(((VideoActivity) mContext).player.getPlayUrl())) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }
    }

    public void cancleAllTask() {
        for (GetDvrVideoBitmatTask task : tasks) {
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
                    GetDvrVideoBitmatTask task = new GetDvrVideoBitmatTask(mList.get(i).url);
                    task.execute(mList.get(i).url);
                    tasks.add(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private Handler mHandler = new Handler(Looper.getMainLooper());

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


    public class GetDvrVideoBitmatTask extends AsyncTask<String, Bitmap, Bitmap> {
        private String url;

        GetDvrVideoBitmatTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                final String path = strings[0];
                KSYProbeMediaInfo ksyProbeMediaInfo = new KSYProbeMediaInfo();
                bitmap = ksyProbeMediaInfo.getVideoThumbnailAtTime(strings[0], 1, smallImageWidth, smallImageHeight);
//                FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
//                mmr.setDataSource(path);
//                mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
//                mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
//                bitmap = mmr.getFrameAtTime(-1, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
//                if (bitmap != null) {
//                    bitmap = BitmapTools.zoomImg(bitmap, smallImageWidth, smallImageHeight);
//                }
//                mmr.release();
                if (bitmap != null) {
                    if (doubleBitmapCache != null) {
                        doubleBitmapCache.put(path, bitmap);
                    }
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
