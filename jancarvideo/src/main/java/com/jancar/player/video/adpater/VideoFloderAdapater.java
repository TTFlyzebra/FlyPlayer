package com.jancar.player.video.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.data.FloderVideo;
import com.jancar.media.module.DoubleBitmapCache;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.MarqueeTextView;
import com.jancar.player.video.R;
import com.jancar.player.video.VideoActivity_AP1;
import com.ksyun.media.player.misc.KSYProbeMediaInfo;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;


/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class VideoFloderAdapater extends RecyclerView.Adapter<ViewHolder> {
    private List<FloderVideo> mList;
    private Context mContext;
    private int mColumnNum;
    private OnItemClickListener mOnItemClick;

    private static final int smallImageWidth = 192;
    private static final int smallImageHeight = 108;
    private RecyclerView mRecyclerView;
    private Set<GetVideoBitmatTask> tasks = new HashSet<>();
    private DoubleBitmapCache doubleBitmapCache;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public interface OnItemClickListener {
        void onItemClick(View v, FloderVideo floderVideo);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClick) {
        mOnItemClick = onItemClick;
    }

    public VideoFloderAdapater(Context context, List<FloderVideo> list, int columnNum,RecyclerView recyclerView) {
        mContext = context;
        mList = list;
        mColumnNum = columnNum;
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
        String crtUrl = ((VideoActivity_AP1)mContext).player.getPlayUrl();
        if (holder instanceof PhotoHolder) {
            PhotoHolder photoHolder = (PhotoHolder) holder;
//            Glide.with(mContext)
//                    .load((mList.get(position)).url)
//                    .placeholder(R.drawable.media_default_image)
//                    .error(R.drawable.media_image_error)
//                    .into(photoHolder.imageView1);

            photoHolder.imageView1.setTag(mList.get(position).url);
            Bitmap bitmap = doubleBitmapCache.get(mList.get(position).url);
            if (null != bitmap) {
                photoHolder.imageView1.setImageBitmap(bitmap);
            } else {
                photoHolder.imageView1.setImageResource(R.drawable.media_default_image);
                GetVideoBitmatTask task = new GetVideoBitmatTask(mList.get(position).url);
                task.execute(mList.get(position).url);
                tasks.add(task);
            }

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
}
