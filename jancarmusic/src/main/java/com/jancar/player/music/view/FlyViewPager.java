package com.jancar.player.music.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jancar.media.data.Music;
import com.jancar.player.music.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Author FlyZebra
 * 2018/12/31 14:52
 * Describ:
 **/
public class FlyViewPager extends ViewPager {
    private List<Music> list = new ArrayList<>();
    private MyPageAdapter myPageAdapter;

    public FlyViewPager(Context context) {
        this(context, null);
    }

    public FlyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setPageTransformer(true, new ScaleAlphaPageTransformer());
        myPageAdapter = new MyPageAdapter();
        setAdapter(myPageAdapter);
    }

    public void setList(List<Music> musicList) {
        list.clear();
        list.add(musicList.get(musicList.size()-1));
        list.addAll(musicList);
        list.add(musicList.get(0));

        myPageAdapter.notifyDataSetChanged();
        setCurrentItem(1,false);
    }

    private class MyPageAdapter extends PagerAdapter {
        private HashSet<ImageView> viewSet = new HashSet<>();

        public MyPageAdapter() {
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView photoView = (ImageView) object;
            viewSet.add(photoView);
            container.removeView(photoView);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView photoView = null;
            Iterator it = viewSet.iterator();
            if (it.hasNext()) {
                photoView = (ImageView) it.next();
                viewSet.remove(photoView);
            } else {
                photoView = new ImageView(getContext());
                photoView.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            photoView.setImageResource(R.drawable.ic_music_bak);

            container.addView(photoView);
            return photoView;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    public class ScaleAlphaPageTransformer implements ViewPager.PageTransformer{
        public static final float MAX_SCALE = 1.0f;
        public static final float MIN_SCALE = 0.6f;
        public static final float MAX_ALPHA = 1.0f;
        public static final float MIN_ALPHA = 0.5f;

        private boolean alpha = true;
        private boolean scale = true;

        @Override
        public void transformPage(View page, float position) {

            if (position < -1) {
                position = -1;
            } else if (position > 1) {
                position = 1;
            }

            float tempScale = position < 0 ? 1 + position : 1 - position;

            if(scale){
                float slope = (MAX_SCALE - MIN_SCALE) / 1;
                //一个公式
                float scaleValue = MIN_SCALE + tempScale * slope;
                page.setScaleX(scaleValue);
                page.setScaleY(scaleValue);
            }
            if(alpha){
                //模糊
                float alope = (MAX_ALPHA - MIN_ALPHA) / 1;
                float alphaValue = MIN_ALPHA + tempScale * alope;
                page.setAlpha(alphaValue);
            }
        }

        /***
         * 设置是否模糊和改变大小
         * @param alpha
         * @param scale
         */
        public void setType(boolean alpha, boolean scale){
            this.alpha = alpha;
            this.scale = scale;
        }
    }

}
