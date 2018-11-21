package com.jancar.player.photo;

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
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Image;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.RtlTools;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.TouchEventRelativeLayout;
import com.jancar.player.photo.data.PhotoViewPool;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class PhotoActivity extends BaseActivity implements
        View.OnClickListener,
        ViewPager.OnPageChangeListener,
        FlyTabView.OnItemClickListener,
        TouchEventRelativeLayout.OnTouchEventListener {

    private FlyTabView tabView;
    private String titles[] = new String[]{"磁盘列表", "图片列表", "文件列表"};
    private String fmName[] = new String[]{"PhotoStorageFragment", "PhotoPlayListFragment", "PhotoFloderFragment"};
    public ViewPager viewPager;
    private MyPageAdapter adapter;
    public List<Image> imageList = new ArrayList<>();
    private Hashtable<Integer, Integer> imageResIDs = new Hashtable<>();
    private ImageView photoFore, photoNext, photoRotate, photoZoomIn, photoZoomOut;
    private ImageView leftMenu;
    private TouchEventRelativeLayout controlLayout;
    private TouchEventRelativeLayout leftLayout;
    public int currentItem = 0;
    public Image CURRENT_IMAGE = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int hideTime = 5000;
    public static boolean isScan = true;

    private int mAnimaDurtion = 300;
    private float screen_width = 1024;
    private float screen_height = 600;
    private float photo_bottom_menu_height = 120;
    private float photo_left_list_width = 400;

    private Runnable hideControlTask = new Runnable() {
        @Override
        public void run() {
            long time = System.currentTimeMillis() - touchTime;
            if (time > hideTime && !imageList.isEmpty()) {
                showLeftMenu(false);
                isShowLeftMenu = false;
                controlLayout.animate()
                        .translationY(photo_bottom_menu_height)
                        .setDuration(mAnimaDurtion).start();
                getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
                isShowControl = false;
            } else {
                mHandler.postDelayed(hideControlTask, time + 100);
            }
        }
    };
    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                touchTime = 0;
                showControlView(!isShowControl);
            } catch (Exception e) {
                FlyLog.e();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        screen_width = getResources().getDimensionPixelSize(R.dimen.photo_screen_width);
        screen_height = getResources().getDimensionPixelSize(R.dimen.photo_screen_height);
        photo_bottom_menu_height = getResources().getDimensionPixelSize(R.dimen.photo_bottom_menu_height);
        photo_left_list_width = getResources().getDimensionPixelSize(R.dimen.photo_left_list_width);

        titles = new String[]{getString(R.string.disk_list), getString(R.string.photo_list), getString(R.string.file_list)};
        initView();

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    showControlView(true);
                }
            }
        });

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
        replaceFragment(fmName[1], R.id.ac_replace_fragment);
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
        viewPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void notifyPathChange(String path) {
        FlyLog.d("notifyPathChange path=%s", path);
        if (isStop) return;
        isScan = true;
        imageList.clear();
        currentItem = 0;
        CURRENT_IMAGE = null;
        adapter.notifyDataSetChanged();
        super.notifyPathChange(path);
    }

    @Override
    public void imageUrlList(List<Image> imageUrlList) {
        FlyLog.d("get Image size=%d", imageUrlList == null ? 0 : imageUrlList.size());
        if (isStop) return;
        if (imageUrlList != null) {
//            imageList.clear();
            imageList.addAll(imageUrlList);
            if (!imageList.isEmpty() && CURRENT_IMAGE == null) {
                CURRENT_IMAGE = imageList.get(0);
                currentItem = 0;
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        super.imageUrlList(imageUrlList);
    }

    @Override
    public void scanFinish(String path) {
        FlyLog.d("scanFinish path=%s", path);
        if (isStop) return;
        isScan = false;
        if (imageList == null || imageList.isEmpty()) {
            replaceFragment(fmName[0], R.id.ac_replace_fragment);
            tabView.setFocusPos(0);
            showControlView(true);
            showLeftMenu(true);
        }
        adapter.notifyDataSetChanged();
        super.scanFinish(path);
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
                    break;
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos], R.id.ac_replace_fragment);
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
            controlLayout.animate().translationY(0).setDuration(mAnimaDurtion).start();
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
        boolean isRtl = RtlTools.isLayoutRtl(leftLayout);
        leftLayout.animate()
                .translationX(flag ?
                        isRtl ? photo_left_list_width : -(photo_left_list_width)
                        : 0)
                .setDuration(mAnimaDurtion)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (leftLayout.getX() > (screen_width-photo_left_list_width)
                                || leftLayout.getX() < (-photo_left_list_width)) {
                            leftLayout.setVisibility(View.INVISIBLE);
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
            PhotoView photoView = (PhotoView) object;
            photoView.recycle();
            PhotoViewPool.recycle(photoView);
            container.removeView(photoView);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = PhotoViewPool.obtain(PhotoActivity.this);
            photoView.setOnClickListener(photoOnClickListener);
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
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            return photoView;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

}
