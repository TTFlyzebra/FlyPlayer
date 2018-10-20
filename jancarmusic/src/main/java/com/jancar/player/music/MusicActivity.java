package com.jancar.player.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Music;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.media.view.CircleImageView;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.MarqueeTextView;
import com.jancar.media.view.TouchEventRelativeLayout;
import com.jancar.media.view.lrcview.LrcView;
import com.jancar.player.music.model.listener.IMusicPlayerListener;
import com.jancar.player.music.model.musicplayer.IMusicPlayer;
import com.jancar.player.music.model.musicplayer.MusicPlayer;
import com.jancar.player.music.service.MusicService;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends BaseActivity implements
        SeekBar.OnSeekBarChangeListener,
        IMusicPlayerListener,
        FlyTabView.OnItemClickListener,
        View.OnClickListener,
        TouchEventRelativeLayout.OnTouchEventListener {
    private String titles[] = new String[]{"存储器", "单曲", "歌手", "专辑", "文件夹"};
    private String fmName[] = new String[]{"MusicStorageFragment", "MusicPlayListFragment",
            "MusicArtistFragment", "MusicAlbumFragment", "MusicFloderFragment"};
    public List<Music> musicList = new ArrayList<>();
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();
    private SeekBar seekBar;
    private TextView seekBarSartTime, seekBarEndTime;
    private ImageView playFore, playNext, play, leftMenu;
    private TouchEventRelativeLayout leftLayout;
    private MarqueeTextView tvSingle, tvArtist, tvAlbum;
    private CircleImageView ivImage;
    private ImageView ivLoop;
    private LrcView lrcView;
    private FlyTabView tabView;
    private LinearLayout llContent;

    private int seekBarPos;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Bitmap bitmap = null;
    private Bitmap defaultBitmap = null;
    private String artist = null;
    private String album = null;
    private String lyrics = null;

    private int HIDE_TIME = 5000;
    private static final int REFRESH_SEEK_LRC_TIME = 1000;

    private MyReceiver mReceiver;
    private long touchTime;
    private int countSavePlaySeek = 0;
    private int SAVEPLAYSEEKTIME = 10;

    public static boolean isScan = true;

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            countSavePlaySeek++;
            if (countSavePlaySeek % SAVEPLAYSEEKTIME == 0 && musicPlayer != null && musicPlayer.isPlaying()) {
                musicPlayer.savePathUrl(currenPath);
            }
            if (musicPlayer != null) {
                seekBarPos = musicPlayer.getCurrentPosition();
                lrcView.updateTime(seekBarPos);
                setSeekStartText(seekBarPos);
                seekBar.setProgress(seekBarPos);
            }
            mHandler.removeCallbacks(seekBarTask);
            mHandler.postDelayed(seekBarTask, REFRESH_SEEK_LRC_TIME);
        }
    };
    private Runnable hideLeftLayoutTask = new Runnable() {
        @Override
        public void run() {
            long time = System.currentTimeMillis() - touchTime;
            if (time > HIDE_TIME && !musicList.isEmpty()) {
                showLeftMenu(false);
            } else {
                mHandler.postDelayed(hideLeftLayoutTask, time + 100);
            }
        }
    };
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestAudioFocus();

        /**
         *监听通知栏退出广播
         */
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.MAIN_ACTION_BROADCAST_EXIT);
        registerReceiver(mReceiver, intentFilter);

        /**
         * 启动服务
         */
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        titles = new String[]{getString(R.string.storage), getString(R.string.single), getString(R.string.artist), getString(R.string.album), getString(R.string.folder)};
        initView();
        musicPlayer.addListener(this);
    }

    private void initView() {
        seekBar = (SeekBar) findViewById(R.id.ac_music_seekBar);
        seekBarSartTime = (TextView) findViewById(R.id.ac_music_currentTime);
        seekBarEndTime = (TextView) findViewById(R.id.ac_music_endTime);
        playFore = (ImageView) findViewById(R.id.ac_music_play_fore);
        playNext = (ImageView) findViewById(R.id.ac_music_play_next);
        play = (ImageView) findViewById(R.id.ac_music_play);
        leftMenu = (ImageView) findViewById(R.id.ac_music_left_menu);
        leftLayout = (TouchEventRelativeLayout) findViewById(R.id.ac_music_left_layout);
        tabView = (FlyTabView) findViewById(R.id.ac_music_tabview);
        tvSingle = (MarqueeTextView) findViewById(R.id.ac_music_single);
        tvArtist = (MarqueeTextView) findViewById(R.id.ac_music_artist);
        tvAlbum = (MarqueeTextView) findViewById(R.id.ac_music_album);
        ivImage = (CircleImageView) findViewById(R.id.ac_music_iv_image);
        ivLoop = (ImageView) findViewById(R.id.ac_music_iv_loop);
        lrcView = (LrcView) findViewById(R.id.ac_music_lrcview);
        llContent = (LinearLayout) findViewById(R.id.ac_music_content);

        tvSingle.enableMarquee(true);
        tvArtist.enableMarquee(true);
        tvAlbum.enableMarquee(true);

        playFore.setOnClickListener(this);
        playNext.setOnClickListener(this);
        play.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        leftMenu.setOnClickListener(this);
        tabView.setOnItemClickListener(this);
        ivLoop.setOnClickListener(this);
        leftLayout.setOnTouchEventListener(this);
        llContent.setOnClickListener(this);

        tabView.setTitles(titles);
        replaceFragment(fmName[1], R.id.ac_replace_fragment);
        tabView.setFocusPos(1);
    }

    private boolean isStop = true;

    protected void onStart() {
        super.onStart();
        isStop = false;
    }

    @Override
    protected void onStop() {
        isStop = true;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        musicPlayer.savePathUrl(currenPath);
        musicPlayer.removeListener(this);
        unregisterReceiver(mReceiver);
        abandonAudioFocus();
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
        super.onDestroy();
    }


    @Override
    public void notifyPathChange(String path) {
        FlyLog.d("notifyPathChange path=%s", path);
        isScan = true;
        if (musicPlayer.isPlaying()) {
            musicPlayer.savePathUrl(currenPath);
        }
        musicList.clear();
        musicPlayer.stop();
        tvAlbum.setText("");
        tvArtist.setText("");
        tvSingle.setText("");
        musicPlayer.playSavePath(path);
        super.notifyPathChange(path);
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("get player.music size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        if (musicUrlList != null && !musicUrlList.isEmpty()) {
            musicList.addAll(musicUrlList);
            musicPlayer.setPlayUrls(musicList);
        }
        super.musicUrlList(musicUrlList);
    }

    @Override
    public void scanFinish(String path) {
        FlyLog.d("scanFinish path=%s", path);
        isScan = false;
        if (musicList == null || musicList.isEmpty()) {
            replaceFragment(fmName[0], R.id.ac_replace_fragment);
            tabView.setFocusPos(0);
            showLeftMenu(true);
        }
        if (isShowLeftMenu) {
            showLeftMenu(true);
        }
        super.scanFinish(path);
    }

    @Override
    public void scanServiceConneted() {
        String path = null;
        try {
            Intent intent = getIntent();
            path = intent.getStringExtra("device");
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        if (!TextUtils.isEmpty(path)) {
            currenPath = path;
        }
        super.scanServiceConneted();
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        try {
            if (musicUrlList == null || musicUrlList.isEmpty()) {
                return;
            }
            try {
                for (int i = 0; i < musicUrlList.size(); i++) {
                    int sort = musicUrlList.get(i).sort;
                    musicList.get(sort).artist = musicUrlList.get(i).artist;
                    musicList.get(sort).album = musicUrlList.get(i).album;
                    musicList.get(sort).name = musicUrlList.get(i).name;
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        super.musicID3UrlList(musicUrlList);
    }

    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos], R.id.ac_replace_fragment);
        }
    }

    private boolean isShowLeftMenu = false;


    private void showLeftMenu(boolean flag) {
        isShowLeftMenu = flag;
        mHandler.removeCallbacks(hideLeftLayoutTask);
        leftLayout.animate().translationX(flag
                ? -394 * DisplayUtils.getMetrices(this).widthPixels / 1024
                : 0
        ).setDuration(300).start();
        if (flag) {
            mHandler.postDelayed(hideLeftLayoutTask, HIDE_TIME);
        }
        leftMenu.setImageResource(flag ? R.drawable.media_list_menu_open : R.drawable.media_list_menu_close);
    }

    @Override
    public void onFlyTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_music_content:
                showLeftMenu(false);
                break;
            case R.id.ac_music_left_menu:
                showLeftMenu(!isShowLeftMenu);
                break;
            case R.id.ac_music_play_fore:
                musicPlayer.playFore();
                break;
            case R.id.ac_music_play_next:
                musicPlayer.playNext();
                break;
            case R.id.ac_music_play:
                try {
                    if (musicPlayer.isPlaying()) {
                        musicPlayer.pause();
                    } else {
                        musicPlayer.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ac_music_iv_loop:
                musicPlayer.switchLoopStatus();
                break;
        }
    }

    @Override
    public void playStatusChange(int statu) {
        switch (statu) {
            case MusicPlayer.STATUS_COMPLETED:
            case MusicPlayer.STATUS_STARTPLAY:
                lrcView.setVisibility(View.GONE);
                break;
            case MusicPlayer.STATUS_PLAYING:
                initSeekBar();
                upPlayInfo();
                break;
            case MusicPlayer.STATUS_IDLE:
                tvSingle.setText("");
                tvArtist.setText("");
                tvAlbum.setText("");
                initSeekBar();
                ivImage.setImageResource(R.drawable.media_music);
                break;
        }
        play.setImageResource(musicPlayer.isPlaying() ? R.drawable.media_pause : R.drawable.media_play);
        ivImage.setAnimatePlaying(musicPlayer.isPlaying());

    }

    @Override
    public void loopStatusChange(int staut) {
        switch (staut) {
            case MusicPlayer.LOOP_ALL:
                ivLoop.setImageResource(R.drawable.media_loop_all);
                break;
            case MusicPlayer.LOOP_ONE:
                ivLoop.setImageResource(R.drawable.media_loop_one);
                break;
            case MusicPlayer.LOOP_RAND:
                ivLoop.setImageResource(R.drawable.media_loop_rand);
                break;
        }
    }

    private void initSeekBar() {
        int sumTime = musicPlayer.getDuration();
        seekBar.setMax(sumTime);
        int hou = sumTime / 360000;
        int min = sumTime / 60000 % 60;
        int sec = sumTime / 1000 % 60;
        String text = (hou == 0 ? "" : ((hou > 9 ? hou : "0" + hou) + ":")) + ""
                + (min > 9 ? min : "0" + min) + ":"
                + (sec > 9 ? sec : "0" + sec);
        seekBarEndTime.setText(text);
        mHandler.removeCallbacks(seekBarTask);
        mHandler.post(seekBarTask);
    }

    private void upPlayInfo() {
        tvSingle.setText(StringTools.getNameByPath(musicPlayer.getPlayUrl()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (defaultBitmap == null) {
                    defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.media_music);
                }
                loadID3Info();
            }
        }).start();
    }

    private void loadID3Info() {
        try {
            final String url = musicPlayer.getPlayUrl();
            bitmap = null;
            artist = "";
            album = "";
            lyrics = "";
            if (url.toLowerCase().endsWith(".mp3")) {
                FlyLog.d("start get id3 info url=%s", url);
                Mp3File mp3file = new Mp3File(url);
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                    artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? "" : id3v2Tag.getArtist();
                    album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? "" : id3v2Tag.getAlbum();
                    lyrics = TextUtils.isEmpty(id3v2Tag.getLyrics()) ? "" : id3v2Tag.getLyrics();
                    byte[] albumImageData = id3v2Tag.getAlbumImage();
                    if (albumImageData != null) {
                        bitmap = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                    }
                } else if (mp3file.hasId3v1Tag()) {
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? "" : id3v1Tag.getArtist();
                    album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? "" : id3v1Tag.getAlbum();
                }
                FlyLog.d("scanFinish get id3 info url=%s", url);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isStop) return;
                        ivImage.setImageBitmap(bitmap == null ? defaultBitmap : bitmap);
                        tvArtist.setText(TextUtils.isEmpty(artist) ? getString(R.string.no_artist) : artist);
                        tvAlbum.setText(TextUtils.isEmpty(album) ? getString(R.string.no_album) : album);
                        if (!TextUtils.isEmpty(lyrics)) {
                            FlyLog.d("id3 lrc=%s", lyrics);
                            lrcView.loadLrc(lyrics);
                        } else {
                            String lrcPath = StringTools.getlrcByPath(musicPlayer.getPlayUrl());
                            lrcView.loadLrc(new File(lrcPath));
                        }
                        lrcView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            });
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekBarPos = progress;
        setSeekStartText(seekBarPos);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(seekBarTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        musicPlayer.seekTo(seekBarPos);
        lrcView.updateTime(seekBarPos);
        mHandler.removeCallbacks(seekBarTask);
        mHandler.postDelayed(seekBarTask, REFRESH_SEEK_LRC_TIME);
    }

    public void setSeekStartText(int seekPos) {
        int hou = seekPos / 360000;
        int min = seekPos / 60000 % 60;
        int sec = seekPos / 1000 % 60;
        String text = (hou == 0 ? "" : ((hou > 9 ? hou : "0" + hou) + ":")) + ""
                + (min > 9 ? min : "0" + min) + ":"
                + (sec > 9 ? sec : "0" + sec);
        seekBarSartTime.setText(text);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MusicService.MAIN_ACTION_BROADCAST_EXIT)) {
                finish();
            }
        }
    }

    private boolean lostPause = false;
    private boolean jancarMixPause = true;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            FlyLog.d("onAudioFocusChange focusChange=%d", focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    //长期失去焦点，此处应该关闭播放器，释放资源
//                    pause();
                    finish();
                    lostPause = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (musicPlayer.isPlaying()) {
                        musicPlayer.pause();
                        lostPause = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    /**
                     * 是否混音
                     */
                    boolean flag = SystemProperties.getBoolean(SystemPropertiesProxy.Property.PERSIST_KEY_GISMIX, true);
                    if (!flag && musicPlayer.isPlaying()) {
                        musicPlayer.pause();
                        jancarMixPause = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (musicPlayer.isPause() && lostPause) {
                        musicPlayer.start();
                        lostPause = false;
                    }
                    if (jancarMixPause) {
                        musicPlayer.start();
                        jancarMixPause = false;
                    }
                    break;
            }
        }
    };

    private void requestAudioFocus() {
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }
}
