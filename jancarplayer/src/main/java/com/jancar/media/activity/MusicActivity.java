package com.jancar.media.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.listener.IMusicPlayerListener;
import com.jancar.media.model.IMusicPlayer;
import com.jancar.media.model.MusicPlayer;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.StringTools;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.MarqueeTextView;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends BaseActivity implements
        SeekBar.OnSeekBarChangeListener,
        IMusicPlayerListener,
        FlyTabView.OnItemClickListener,
        View.OnClickListener {
    private FlyTabView tabView;
    private String titles[] = new String[]{"存储器", "单曲", "歌手", "专辑", "文件夹"};
    private String fmName[] = new String[]{"StorageFragment", "MusicPlayListFragment", "MusicArtistFragment", "MusicAlbumFragment", "MusicFloderFragment"};
    public List<String> musicList = new ArrayList<>();
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();

    private SeekBar seekBar;
    private TextView seekBarSartTime, seekBarEndTime;
    private ImageView playFore, playNext, play, leftMenu;
    private RelativeLayout leftLayout;
    private MarqueeTextView tvSingle, tvArtist, tvAlbum;
    private ImageView ivImage;
    private int seekPos;
    public int currenPos = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            seekPos = musicPlayer.getCurrentPosition();
            int min = seekPos / 1000 / 60;
            int sec = seekPos / 1000 % 60;
            String text = min + ":" + (sec > 9 ? sec : "0" + sec);
            seekBarSartTime.setText(text);
            seekBar.setProgress(seekPos);
            mHandler.postDelayed(seekBarTask, 1000);
        }
    };

    private Bitmap bitmap = null;
    private Bitmap defaultBitmap = null;
    private String artist = null;
    private String album = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        musicPlayer.init(getApplicationContext());
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
        leftLayout = (RelativeLayout) findViewById(R.id.ac_music_left_layout);
        tabView = (FlyTabView) findViewById(R.id.ac_music_tabview);
        tvSingle = (MarqueeTextView) findViewById(R.id.ac_music_single);
        tvArtist = (MarqueeTextView) findViewById(R.id.ac_music_artist);
        tvAlbum = (MarqueeTextView) findViewById(R.id.ac_music_album);
        ivImage = (ImageView) findViewById(R.id.ac_music_iv_image);
        tvSingle.enableMarquee(true);
        tvArtist.enableMarquee(true);
        tvAlbum.enableMarquee(true);

        playFore.setOnClickListener(this);
        playNext.setOnClickListener(this);
        play.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        leftMenu.setOnClickListener(this);
        tabView.setOnItemClickListener(this);

        tabView.setTitles(titles);

        replaceFragment(fmName[1]);
        tabView.setFocusPos(1);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        musicPlayer.removeListener(this);
        musicPlayer.stop();
        super.onDestroy();
    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {
        FlyLog.d("get music size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        if (musicUrlList == null) {
            FlyLog.d("musicUrlList = null return");
            return;
        }
        musicList.clear();
        if (musicUrlList.isEmpty()) {
            currenPos = 0;
            if(musicPlayer.isPlaying()){
                musicPlayer.stop();
            }
            FlyLog.d("musicPlayer stop");
            return;
        }
        musicList.addAll(musicUrlList);
        //TODO:判断当前列表有没更新，确定播放哪首歌曲
        if(!musicPlayer.isPlaying()){
            currenPos = 0;
            musicPlayer.play(musicList.get(currenPos));
            return;
        }

        if (currenPos >= musicUrlList.size()) {
            currenPos = 0;
            musicPlayer.play(musicList.get(currenPos));
            return;
        }
        String currentUrl = musicPlayer.getPlayUrl();
        //TODO:第一首扫到的歌曲不是按顺序排列的第一首歌的情况怎么处理
        if (!musicList.get(currenPos).equals(currentUrl)) {
            currenPos = 0;
            musicPlayer.play(musicList.get(currenPos));
        }
    }


    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos]);
        }
    }

    private boolean isShowLeftMenu = false;

    private void showOrHideLeftMenu() {
        isShowLeftMenu = !isShowLeftMenu;
        leftLayout.animate().translationX(isShowLeftMenu
                ? -394* DisplayUtils.getMetrices(this).widthPixels/1024
                : 0).setDuration(300).start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_music_left_menu:
                showOrHideLeftMenu();
                break;
            case R.id.ac_music_play_fore:
                playFore();
                break;
            case R.id.ac_music_play_next:
                playNext();
                break;
            case R.id.ac_music_play:
                try {
                    if (musicPlayer.isPlaying()) {
                        musicPlayer.puase();
                    } else {
                        musicPlayer.start();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void statusChange(int statu) {
        switch (statu) {
            case MusicPlayer.STATUS_COMPLETED:
                playNext();
                break;
            case MusicPlayer.STATUS_PLAYING:
                initSeekBar();
                upPlayInfo();
                break;
            case MusicPlayer.STATUS_ERROR:
            case MusicPlayer.STATUS_PAUSE:
            case MusicPlayer.STATUS_LOADING:
                break;
        }

        play.setImageResource(musicPlayer.isPlaying() ? R.drawable.media_pause : R.drawable.media_play);

    }

    private void initSeekBar() {
        int sumTime = musicPlayer.getMediaPlay().getDuration();
        seekBar.setMax(sumTime);
        int min = sumTime / 1000 / 60;
        int sec = sumTime / 1000 % 60;
        String text = min + ":" + (sec > 9 ? sec : "0" + sec);
        seekBarEndTime.setText(text);
        mHandler.removeCallbacks(seekBarTask);
        mHandler.post(seekBarTask);
    }

    private void upPlayInfo() {
        tvSingle.setText(StringTools.getNameByPath(musicPlayer.getPlayUrl()));
        currenPos = 0;
        for (int i = 0; i < musicList.size(); i++) {
            if (musicList.get(i).equals(musicPlayer.getPlayUrl())) {
                currenPos = i;
                break;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (defaultBitmap == null) {
                    defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.media_music);
                }
                initID3();
            }
        }).start();
    }

    private void initID3() {
        try {
            String url = musicPlayer.getPlayUrl();
            bitmap = null;
            artist = "";
            album = "";
            if (url.toLowerCase().endsWith(".mp3")) {
                FlyLog.d("start get id3 info url=%s", url);
                Mp3File mp3file = new Mp3File(url);
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                    artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? "" : id3v2Tag.getArtist();
                    album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? "" : id3v2Tag.getAlbum();
                    byte[] albumImageData = id3v2Tag.getAlbumImage();
                    if (albumImageData != null) {
                        bitmap = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                    }
                } else if (mp3file.hasId3v1Tag()) {
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? "" : id3v1Tag.getArtist();
                    album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? "" : id3v1Tag.getAlbum();
                }
                FlyLog.d("finish get id3 info url=%s", url);
            }
            if(!isStop) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(isStop) return;
                            ivImage.setImageBitmap(bitmap == null ? defaultBitmap : bitmap);
                            tvArtist.setText(TextUtils.isEmpty(artist) ? getString(R.string.no_artist) : artist);
                            tvAlbum.setText(TextUtils.isEmpty(album) ? getString(R.string.no_album) : album);
                        } catch (Exception e) {
                            FlyLog.e(e.toString());
                        }
                    }
                });
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekPos = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        musicPlayer.seekTo(seekPos);
    }

    private void playNext() {
        if (musicList != null && !musicList.isEmpty()) {
            if (currenPos < musicList.size() - 1) {
                currenPos++;
                musicPlayer.play(musicList.get(currenPos));
            }
        }
    }

    private void playFore() {
        if (musicList != null && !musicList.isEmpty()) {
            if (currenPos > 0) {
                currenPos--;
                musicPlayer.play(musicList.get(currenPos));
            }
        }
    }


    private boolean isStop = true;
    @Override
    protected void onStart() {
        super.onStart();
        if(musicPlayer.isPuase()){
            musicPlayer.start();
        }
        isStop = false;
    }

    @Override
    protected void onStop() {
        musicPlayer.puase();
        isStop = true;
        super.onStop();
    }
}
