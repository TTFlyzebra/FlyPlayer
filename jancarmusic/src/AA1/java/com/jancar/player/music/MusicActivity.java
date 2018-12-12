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
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jancar.key.KeyDef;
import com.jancar.media.Entry.StorageEntry;
import com.jancar.media.JacMediaSession;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.RtlTools;
import com.jancar.media.utils.StringTools;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.media.view.CircleImageView;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.MarqueeTextView;
import com.jancar.media.view.TouchEventRelativeLayout;
import com.jancar.media.view.lrcview.LrcView;
import com.jancar.player.music.banner.BannerView;
import com.jancar.player.music.banner.holder.HolderCreator;
import com.jancar.player.music.banner.holder.ViewHolder;
import com.jancar.player.music.model.listener.IMusicPlayerListener;
import com.jancar.player.music.model.musicplayer.IMusicPlayer;
import com.jancar.player.music.model.musicplayer.MusicPlayer;
import com.jancar.player.music.service.MusicService;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jancar.key.KeyDef.KeyAction.KEY_ACTION_UP;

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
    private String title = null;

    private int HIDE_TIME = 5000;
    private static final int REFRESH_SEEK_LRC_TIME = 1000;

    private MyReceiver mReceiver;
    private long touchTime;
    private int countSavePlaySeek = 0;
    private int SAVEPLAYSEEKTIME = 10;

    public static boolean isScan = true;

    private float music_left_list_width = 456;
    private Set<String> artistSet = new HashSet<>();
    private Set<String> albumSet = new HashSet<>();
    private List<String> groupList = new ArrayList<>();//文件夹
    private Map<String, List<String>> mHashMap = new HashMap<>();
    private int floderNum = 0;//存储器
    private int musicUrlNum = 0;

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
    private JacMediaSession jacMediaSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initMediaSession();
        music_left_list_width = getResources().getDimensionPixelSize(R.dimen.music_left_list_width);
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
//        titles = new String[]{getString(R.string.storage), getString(R.string.single), getString(R.string.artist), getString(R.string.album), getString(R.string.folder)};
        initView();
        musicPlayer.addListener(this);

        /**
         * 更新循环状态
         */
        loopStatusChange(musicPlayer.getLoopStatus());
    }

    private void initMediaSession() {
        jacMediaSession = new JacMediaSession(this) {
            @Override
            public boolean OnKeyEvent(int key, int state) throws RemoteException {
                boolean bRet = false;
                try {
                    KeyDef.KeyType keyType = KeyDef.KeyType.nativeToType(key);
                    KeyDef.KeyAction keyAction = KeyDef.KeyAction.nativeToType(state);
                    bRet = true;
                    switch (keyType) {
                        case KEY_PREV:
                            if (keyAction == KEY_ACTION_UP)
                                musicPlayer.playFore();
                            break;
                        case KEY_NEXT:
                            if (keyAction == KEY_ACTION_UP)
                                musicPlayer.playNext();
                            break;
                        case KEY_PAUSE:
                            if (keyAction == KEY_ACTION_UP)
                                musicPlayer.pause();
                            break;
                        case KEY_PLAY:
                            if (keyAction == KEY_ACTION_UP)
                                musicPlayer.start();
                            break;
                        default:
                            bRet = false;
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bRet;
            }
        };
        jacMediaSession.setActive(true);
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

//        tabView.setTitles(titles);
        replaceFragment(fmName[0], R.id.ac_replace_fragment);
        tabView.setFocusPos(0);
    }

    private boolean isStop = true;

    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        isStop = false;

    }

    @Override
    protected void onStop() {
        isStop = true;
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        jacMediaSession.setActive(false);
        mHandler.removeCallbacksAndMessages(null);
        musicPlayer.savePathUrl(currenPath);
        musicPlayer.removeListener(this);
        musicPlayer.stop();
        unregisterReceiver(mReceiver);
        abandonAudioFocus();
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
        super.onDestroy();
    }


    @Override
    public void notifyPathChange(String path) {
        FlyLog.d("notifyPathChange path=%s", path);
        if (isStop) return;
        isScan = true;
        if (musicPlayer.isPlaying()) {
            musicPlayer.savePathUrl(currenPath);
        }
        musicList.clear();
        albumSet.clear();
        artistSet.clear();
        groupList.clear();
        if (!musicPlayer.getPlayUrl().startsWith(path)) {
            musicPlayer.stop();
            tvAlbum.setText("");
            tvArtist.setText("");
            tvSingle.setText("");
        }
        musicPlayer.playSavePath(path);
        super.notifyPathChange(path);
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("get player.music size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        if (isStop) return;
        if (musicUrlList != null && !musicUrlList.isEmpty()) {
            musicList.addAll(musicUrlList);
            musicPlayer.setPlayUrls(musicList);
        }
        getGroupList(musicUrlList);
        super.musicUrlList(musicUrlList);
    }

    private void getGroupList(List<Music> musicUrlList) {
        try {

            if (musicUrlList != null && musicUrlList.size() > 0) {
                groupList.clear();
                for (int i = 0; i < musicUrlList.size(); i++) {
                    String url = musicUrlList.get(i).url;
                    int last = url.lastIndexOf(File.separator);
                    String path = url.substring(0, last);
                    if (mHashMap.get(path) == null) {
                        mHashMap.put(path, new ArrayList<String>());
                    }
                    mHashMap.get(path).add(url);
                }
                groupList.addAll(mHashMap.keySet());
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void scanFinish(String path) {
        FlyLog.d("scanFinish path=%s", path);
        if (isStop) return;
        isScan = false;
        if (musicList == null || musicList.isEmpty()) {
            replaceFragment(fmName[0], R.id.ac_replace_fragment);
            tabView.setFocusPos(0);
            showLeftMenu(true);
        }
        if (isShowLeftMenu) {
            showLeftMenu(true);
        }
        musicUrlNum = musicList.size();
        getArtistAndAlbum();
        updateTabView();
        super.scanFinish(path);
    }

    private void getArtistAndAlbum() {
        if (musicList != null && musicList.size() > 0) {
            for (int i = 0; i < musicList.size(); i++) {
                String art = musicList.get(i).artist;
                String alb = musicList.get(i).album;
                if (!artistSet.contains(art)) {
                    artistSet.add(art);
                }
                if (!albumSet.contains(alb)) {
                    albumSet.add(alb);
                }
            }
        }
    }

    private void updateTabView() {
        String storage = String.format(getString(R.string.storage) + "\n" + "(" + "%d" + ")", floderNum);
        String single = String.format(getString(R.string.single) + "\n" + "(" + "%d" + ")", musicUrlNum);
        String ar = String.format(getString(R.string.artist) + "\n" + "(" + "%d" + ")", artistSet.size());
        String al = String.format(getString(R.string.album) + "\n" + "(" + "%d" + ")", albumSet.size());
        String fol = String.format(getString(R.string.folder) + "\n" + "(" + "%d" + ")", groupList.size());
        titles = new String[]{storage, single, ar, al, fol};
        tabView.setTitles(titles);

    }

    @Override
    public void scanServiceConneted() {
        if (isStop) return;
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
        if (isStop) return;
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
        boolean isRtl = RtlTools.isLayoutRtl(leftLayout);
        leftLayout.animate().translationX(flag
                ? (isRtl ? (music_left_list_width + 56) : (-music_left_list_width + 56))
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
                ivImage.setImageResource(R.drawable.media_music_iv02);
                break;
        }
        jacMediaSession.notifyPlayState(statu);
        play.setImageResource(musicPlayer.isPlaying() ? R.drawable.media_pause : R.drawable.media_play);
        ivImage.setAnimatePlaying(musicPlayer.isPlaying());

    }

    @Override
    public void loopStatusChange(int staut) {
        jacMediaSession.notifyRepeat(staut);
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
        jacMediaSession.notifyProgress(sumTime, seekBarPos);
        seekBar.setMax(sumTime);
        int hou = sumTime / 3600000;
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
        title = StringTools.getNameByPath(musicPlayer.getPlayUrl());
        tvSingle.setText(title);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (defaultBitmap == null) {
                    defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.media_music_iv02);
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
            byte[] albumImageData = null;
            if (url.toLowerCase().endsWith(".mp3")) {
                FlyLog.d("start get id3 info url=%s", url);
                Mp3File mp3file = new Mp3File(url);
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                    artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? "" : id3v2Tag.getArtist();
                    album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? "" : id3v2Tag.getAlbum();
                    lyrics = TextUtils.isEmpty(id3v2Tag.getLyrics()) ? "" : id3v2Tag.getLyrics();
                    albumImageData = id3v2Tag.getAlbumImage();
                    if (albumImageData != null) {
                        bitmap = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                    }
                } else if (mp3file.hasId3v1Tag()) {
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? "" : id3v1Tag.getArtist();
                    album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? "" : id3v1Tag.getAlbum();
                }
                FlyLog.d("get id3 info url=%s", url);
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
            jacMediaSession.notifyPlayUri(title);
            jacMediaSession.notifyId3(title, artist, album, albumImageData);
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
        jacMediaSession.notifyProgress(seekPos, musicPlayer.getDuration());
        int hou = seekPos / 3600000;
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
                    String flag = SystemPropertiesProxy.get(MusicActivity.this, SystemPropertiesProxy.Property.PERSIST_KEY_GISMIX, "100");
                    if (flag.equals("0") && musicPlayer != null && musicPlayer.isPlaying()) {
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

    /**
     * 事件响应方法
     * 蓝牙连接上后消失提示框
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStorageEvent(StorageEntry event) {
        if (event.getType() == 0) {
            floderNum = event.getStorageNum();
        }
    }

}
