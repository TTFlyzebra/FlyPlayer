package com.jancar.media.activity;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Image;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.TouchEventRelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PhotoActivity extends BaseActivity implements
        View.OnClickListener,
        ViewPager.OnPageChangeListener,
        FlyTabView.OnItemClickListener,
        TouchEventRelativeLayout.OnTouchEventListener {

    private FlyTabView tabView;
    private String titles[] = new String[]{"磁盘列表", "图片列表", "文件列表"};
    private String fmName[] = new String[]{"StorageFragment", "PhotoPlayListFragment", "PhotoFloderFragment"};
    public ViewPager viewPager;
    private MyPageAdapter adapter;
    public List<Image> imageList = new ArrayList<>();
    private HashMap<Integer, Integer> imageResIDs = new HashMap<>();
    private ImageView photoFore, photoNext, photoRotate, photoZoomIn, photoZoomOut;
    private ImageView leftMenu;
    private TouchEventRelativeLayout controlLayout;
    private TouchEventRelativeLayout leftLayout;
    public int currentItem = 0;
    public Image CURRENT_IMAGE = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int hideTime = 5000;
    private Runnable hideControlTask = new Runnable() {
        @Override
        public void run() {
            long time = System.currentTimeMillis() - touchTime;
            if (time > hideTime && !imageList.isEmpty()) {
                showLeftMenu(false);
                isShowLeftMenu = false;
                controlLayout.animate()
                        .translationY(150 * DisplayUtils.getMetrices(PhotoActivity.this).heightPixels / 600)
                        .setDuration(300).start();
                getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
                isShowControl = false;
            } else {
                mHandler.postDelayed(hideControlTask, time + 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        titles = new String[]{getString(R.string.disk_list), getString(R.string.photo_list), getString(R.string.file_list)};
        initView();

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
//                    showOrHideControlView();
                }
            }
        });

        usbMediaScan.addListener(this);
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.ac_photo_viewpager);
        photoFore = (ImageView) findViewById(R.id.ac_photo_play_fore);
        photoNext = (ImageView) findViewById(R.id.ac_photo_play_next);
        photoRotate = (ImageView) findViewById(R.id.ac_photo_rotate);
        photoZoomIn = (ImageView) findViewById(R.id.ac_photo_zoomin);
        photoZoomOut = (ImageView) findViewById(R.id.ac_photo_zoomout);
        leftMenu = (ImageView) findViewById(R.id.ac_photo_left_menu);
        leftLayout = (TouchEventRelativeLayout) findViewById(R.id.ac_photo_left_layout);
        controlLayout = (TouchEventRelativeLayout) findViewById(R.id.ac_photo_control);
        tabView = (FlyTabView) findViewById(R.id.ac_photo_tabview);

        adapter = new MyPageAdapter();
        viewPager.setAdapter(adapter);

        findViewById(R.id.ac_photo_root).setOnClickListener(this);
        viewPager.setOnClickListener(this);
        photoFore.setOnClickListener(this);
        photoNext.setOnClickListener(this);
        photoRotate.setOnClickListener(this);
        photoZoomIn.setOnClickListener(this);
        photoZoomOut.setOnClickListener(this);
        viewPager.addOnPageChangeListener(this);
        leftMenu.setOnClickListener(this);
        tabView.setOnItemClickListener(this);
        controlLayout.setOnClickListener(this);
        leftLayout.setOnTouchEventListener(this);
        controlLayout.setOnTouchEventListener(this);
        tabView.setTitles(titles);
        replaceFragment(fmName[1]);
        tabView.setFocusPos(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imageList.size() > currentItem) {
            viewPager.setCurrentItem(currentItem, false);
        }
    }

    @Override
    protected void onDestroy() {
        CURRENT_IMAGE = null;
        mHandler.removeCallbacksAndMessages(null);
        usbMediaScan.removeListener(this);
        super.onDestroy();
    }

    @Override
    public void changePath(String path) {
        imageList.clear();
        currentItem = 0;
        CURRENT_IMAGE = null;
        adapter.notifyDataSetChanged();
        super.changePath(path);
    }

    @Override
    public void imageUrlList(List<Image> imageUrlList) {
        FlyLog.d("get Image size=%d", imageUrlList == null ? 0 : imageUrlList.size());
        if (imageUrlList != null) {
//            imageList.clear();
            imageList.addAll(imageUrlList);
            if (!imageList.isEmpty() && CURRENT_IMAGE == null) {
                CURRENT_IMAGE = imageList.get(0);
                currentItem = 0;
            }

            if (imageList.isEmpty()) {
                mHandler.removeCallbacks(hideControlTask);
                controlLayout.animate().translationY(0).setDuration(300).start();
                getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                isShowControl = true;
                showLeftMenu(true);
            } else {
                showControlView(true);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        super.imageUrlList(imageUrlList);
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.ac_photo_play_fore:
                    viewPager.setCurrentItem(Math.max(0, currentItem - 1));
                    break;
                case R.id.ac_photo_play_next:
                    viewPager.setCurrentItem(Math.min(imageList == null ? 0 : imageList.size() - 1, currentItem + 1));
                    break;
                case R.id.ac_photo_rotate:
                    PhotoView photoView1 = (PhotoView) viewPager.findViewById(imageResIDs.get(currentItem));
                    photoView1.setRotationBy(90);
                    break;
                case R.id.ac_photo_zoomin:
                    PhotoView photoView2 = (PhotoView) viewPager.findViewById(imageResIDs.get(currentItem));
                    photoView2.setScale(Math.max(PhotoViewAttacher.DEFAULT_MIN_SCALE, photoView2.getScale() - 0.25f), true);
                    break;
                case R.id.ac_photo_zoomout:
                    PhotoView photoView3 = (PhotoView) viewPager.findViewById(imageResIDs.get(currentItem));
                    photoView3.setScale(Math.min(PhotoViewAttacher.DEFAULT_MAX_SCALE, photoView3.getScale() + 0.25f), true);
                    break;
                case R.id.ac_photo_left_menu:
                    isShowLeftMenu = !isShowLeftMenu;
                    showLeftMenu(isShowLeftMenu);
                    break;
                case R.id.ac_photo_root:
                case R.id.ac_photo_viewpager:
                    touchTime = 0;
                    showControlView(!isShowControl);
                    break;
                default:
                    if (v instanceof PhotoView) {
                        touchTime = 0;
                        showControlView(!isShowControl);
                    }
                    break;
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos]);
        }
    }

    private long touchTime;

    @Override
    public void onFlyTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                break;
        }
    }

    private boolean isShowControl = true;

    private void showControlView(boolean flag) {
        isShowControl = flag;
        if (flag) {
            controlLayout.animate().translationY(0).setDuration(300).start();
            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            mHandler.removeCallbacks(hideControlTask);
            mHandler.postDelayed(hideControlTask, hideTime);
        } else {
            mHandler.removeCallbacks(hideControlTask);
            mHandler.post(hideControlTask);
        }
    }

    private boolean isShowLeftMenu = false;

    private void showLeftMenu(boolean flag) {
        leftLayout.setVisibility(View.VISIBLE);
        leftLayout.animate()
                .translationX(flag ? -394 * DisplayUtils.getMetrices(this).widthPixels / 1024
                        : 0)
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (leftLayout.getX() > ((1024 - 394) * DisplayUtils.getMetrices(PhotoActivity.this).widthPixels / 1024)) {
                            leftLayout.setVisibility(View.GONE);
                        } else {
                            leftLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
        leftMenu.setImageResource(flag ? R.drawable.media_list_menu_open : R.drawable.media_list_menu_close);
    }

    /**
     * 在Fragment中调用此方法，同步列表选择框
     *
     * @param pos
     */
    public void setSelectItem(int pos) {
        currentItem = pos;
        CURRENT_IMAGE = imageList.get(currentItem);
        viewPager.setCurrentItem(pos);
    }

    /**
     * 在Fragment中调用此方法，同步列表选择框
     */
    public void setSelectItem(String url) {
        FlyLog.d("select start-----");
        for (int i = 0; i < imageList.size(); i++) {
            if (imageList.get(i).url.equals(url)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
        FlyLog.d("select end-----");
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentItem = position;
        CURRENT_IMAGE = imageList.get(currentItem);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private class MyPageAdapter extends PagerAdapter {
        private HashSet<PhotoView> viewSet = new HashSet<>();

        public MyPageAdapter() {
        }

        @Override
        public int getCount() {
            return imageList == null ? 0 : imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            viewSet.add((PhotoView) object);
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = null;
            Iterator it = viewSet.iterator();
            if(it.hasNext()) {
                photoView = (PhotoView) it.next();
                viewSet.remove(photoView);
            }else{
                photoView = new PhotoView(PhotoActivity.this);
            }
            photoView.setOnClickListener(PhotoActivity.this);
            imageResIDs.put(position, View.generateViewId());
            photoView.setId(imageResIDs.get(position));
            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photoView.setZoomable(true);
            Glide.with(PhotoActivity.this)
                    .load(imageList.get(position).url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.drawable.media_image_error)
                    .into(photoView);
            try {
                container.addView(photoView);
            }catch (Exception e){
                FlyLog.e(e.toString());
            }
            return photoView;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }
}
