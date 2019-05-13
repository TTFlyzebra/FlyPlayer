package com.jancar.player.music.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jancar.media.data.Music;
import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.module.DoubleBitmapCache;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.R;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.nio.channels.ClosedByInterruptException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by yarolegovich on 07.03.2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private List<Music> mList;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private MusicPlayer musicPlayer = MusicPlayer.getInstance();
    private Context mContext;
    private DoubleBitmapCache doubleBitmapCache;
    private Set<GetVideoBitmatTask> tasks = new HashSet<>();
    private DiscreteScrollView discreteScrollView;

    public GalleryAdapter(Context context, List<Music> data, DiscreteScrollView recyclerView) {
        this.mContext = context;
        this.mList = data;
        doubleBitmapCache = DoubleBitmapCache.getInstance(context.getApplicationContext());
        discreteScrollView = recyclerView;
        discreteScrollView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(musicPlayer.getPlayUrl().equals(mList.get(position).url)){
            holder.bkground.setBackgroundResource(R.drawable.bk_re_blue);
        }else{
            holder.bkground.setBackgroundResource(R.drawable.bk_re_white);;
        }
        holder.image.setTag(mList.get(position).url);
        Bitmap bitmap = doubleBitmapCache.get(mList.get(position).url);
        if (null != bitmap) {
            holder.image.setImageBitmap(bitmap);
        } else {
            holder.image.setImageResource(R.drawable.media_music_lb);
            GetVideoBitmatTask task = new GetVideoBitmatTask(mList.get(position).url);
            task.execute(mList.get(position).url);
            tasks.add(task);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private FrameLayout bkground;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            bkground = itemView.findViewById(R.id.bkground);
        }
    }

    public class GetVideoBitmatTask extends AsyncTask<String, Bitmap, Bitmap> {
        private String url;

        GetVideoBitmatTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            final String url = strings[0];
            Bitmap bitmap = null;
            try {
                try {
                    byte[] albumImageData = null;
                    if (url.toLowerCase().endsWith(".mp3")) {
                        FlyLog.d("start get id3 info url=%s", url);
                        Mp3File mp3file = new Mp3File(url);
                        if (mp3file.hasId3v2Tag()) {
                            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                            albumImageData = id3v2Tag.getAlbumImage();
                            if (albumImageData != null) {
                                bitmap = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                            }
                        }
                        FlyLog.d("get id3 info url=%s", url);
                    }
                }catch (ClosedByInterruptException e){
                    FlyLog.d("Error ID3 Info");
                } catch (Exception e) {
                    FlyLog.e("url=%s,%s",url,e.toString());
                }
                if (bitmap != null) {
                    if (doubleBitmapCache != null) {
                        doubleBitmapCache.put(url, bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = (ImageView) discreteScrollView.findViewWithTag(url);
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }else{
                    imageView.setImageResource(R.drawable.media_music_lb);
                }
            }
        }

    }

    public void cancleAllTask() {
        for (GetVideoBitmatTask task : tasks) {
            task.cancel(true);
        }
        tasks.clear();
        mHandler.removeCallbacksAndMessages(null);
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
                    ImageView imageView = (ImageView) discreteScrollView.findViewWithTag(mList.get(i));
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
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int first = ((RecyclerView.LayoutManager) (discreteScrollView.getLayoutManager())).findFirstVisibleItemPosition();
//                int last = ((LinearLayoutManager) (discreteScrollView.getLayoutManager())).findLastVisibleItemPosition();
//                if (first >= 0 && last >= first) {
//                    loadImageView(first, last);
//                }
//            }
//        }, 0);

    }
}
