package com.jancar.media.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.module.DoubleBitmapCache;
import com.jancar.media.utils.BitmapTools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GridView 适配器
 */
public class FileListGridViewAdapter extends BaseAdapter {

    private Context mContext;
    private Set<GetDvrVideoBitmatTask> tasks = new HashSet<>();
    private DoubleBitmapCache doubleBitmapCache;
    private static final int smallImageWidth = 101;
    private static final int smallImageHeight = 96;

    private GridView mGridView;
    /**
     * 每个分组下的每个子项的 GridView 数据集合
     */
    private List<String> itemGridList;

    public FileListGridViewAdapter(Context mContext, List<String> itemGridList, GridView gridView) {
        this.mContext = mContext;
        this.itemGridList = itemGridList;
        this.mGridView = gridView;
        doubleBitmapCache = DoubleBitmapCache.getInstance(mContext.getApplicationContext());
    }

    @Override
    public int getCount() {
        return itemGridList==null?0:itemGridList.size();
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
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.gridview_item, null);
        }
        TextView tvGridView = (TextView) convertView.findViewById(R.id.tv_gridview);
        tvGridView.setText(itemGridList.get(position));
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_gridview);
        imageView.setTag(itemGridList.get(position));
        Bitmap bitmap = doubleBitmapCache.get(itemGridList.get(position));
        if (null != bitmap) {
            imageView.setImageBitmap(bitmap);
        }else{
            GetDvrVideoBitmatTask task = new GetDvrVideoBitmatTask(itemGridList.get(position));
            task.execute(itemGridList.get(position));
            tasks.add(task);
        }
        return convertView;
    }

    public class GetDvrVideoBitmatTask extends AsyncTask<String, Bitmap, Bitmap> {
        private String path;

        GetDvrVideoBitmatTask(String dvrFile) {
            this.path = dvrFile;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                final String path = strings[0];
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(strings[0]);
                bitmap = retriever.getFrameAtTime(-1);
                if (bitmap != null) {
                    bitmap = BitmapTools.zoomImg(bitmap, smallImageWidth, smallImageHeight);
                }
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
            ImageView imageView = (ImageView) mGridView.findViewWithTag(path);
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

    }
}
