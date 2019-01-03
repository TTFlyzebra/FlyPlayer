package com.jancar.player.music.adpater;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.R;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.util.List;

/**
 * Created by yarolegovich on 16.03.2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private int itemHeight;
    private List<Music> data;

    public GalleryAdapter(List<Music> data) {
        this.data = data;
    }
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Activity context = (Activity) recyclerView.getContext();
        Point windowDimensions = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(windowDimensions);
        itemHeight = Math.round(windowDimensions.y * 0.6f);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_gallery, parent, false);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                itemHeight);
        v.setLayoutParams(params);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Glide.with(holder.itemView.getContext())
//                .load(data.get(position).getResource())
//                .into(holder.image);
        holder.image.setImageResource(R.drawable.ic_music_bak);
        final String url = data.get(position).url;
        final ImageView imageView = holder.image;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] albumImageData = null;
                    if (url.toLowerCase().endsWith(".mp3")) {
                        FlyLog.d("start get id3 info url=%s", url);
                        Mp3File mp3file = new Mp3File(url);
                        if (mp3file.hasId3v2Tag()) {
                            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                            albumImageData = id3v2Tag.getAlbumImage();
                            if (albumImageData != null) {
                                final Bitmap bitmap = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bitmap);
                                    }
                                });
                            }
                        }
                        FlyLog.d("get id3 info url=%s", url);
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        }).start();

    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private View overlay;
        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            overlay = itemView.findViewById(R.id.overlay);
        }

        public void setOverlayColor(@ColorInt int color) {
            overlay.setBackgroundColor(color);
        }
    }
}
