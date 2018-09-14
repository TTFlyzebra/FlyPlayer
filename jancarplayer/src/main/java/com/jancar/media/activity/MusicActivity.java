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
    private String fmName[] = new String[]{"StorageFragment", "MusicPlayListFragment", "MusicPlayListFragment", "MusicPlayListFragment", "MusicPlayListFragment"};
    public List<String> musicList = new ArrayList<>();
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();

    private SeekBar seekBar;
    private TextView seekBarSartTime, seekBarEndTime;
    private ImageView playFore, playNext, play, leftMenu;
    private RelativeLayout leftLayout;
    private MarqueeTextView tvSingle, tvSinger, tvAlbum;
    private ImageView ivImage;
    private int seekPos;
    private int currenPos = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            seekPos = musicPlayer.getMediaPlay().getCurrentPosition();
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
        titles = new String[]{getString(R.string.storage), getString(R.string.single), getString(R.string.singer), getString(R.string.album), getString(R.string.folder)};

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
        tvSinger = (MarqueeTextView) findViewById(R.id.ac_music_singer);
        tvAlbum = (MarqueeTextView) findViewById(R.id.ac_music_album);
        ivImage = (ImageView) findViewById(R.id.ac_music_iv_image);
        tvSingle.enableMarquee(true);
        tvSinger.enableMarquee(true);
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
        if (musicUrlList != null && !musicUrlList.isEmpty()) {
            musicList.clear();
            musicList.addAll(musicUrlList);
            musicPlayer.play(musicList.get(0));
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
        leftLayout.animate().translationX(isShowLeftMenu ? -394 : 0).setDuration(300).start();
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
                if (musicPlayer.getMediaPlay().isPlaying()) {
                    musicPlayer.puase();
                } else {
                    musicPlayer.start();
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
        for (int i = 0; i < musicList.size(); i++) {
            if (musicList.get(i).equals(musicPlayer.getPlayUrl())) {
                currenPos = i;
                break;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(defaultBitmap==null){
                    defaultBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.media_music);
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
            if (url.endsWith(".mp3")) {
                FlyLog.d("start get id3 info url=%s",url);
                Mp3File mp3file = new Mp3File(url);
                if (mp3file.hasId3v2Tag()) {
                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                    artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? "" : id3v2Tag.getArtist();
                    album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? "" : id3v2Tag.getAlbum();
                    FlyLog.d("ID3Info->Track: " + id3v2Tag.getTrack());
                    FlyLog.d("ID3Info->Artist: " + artist);
                    FlyLog.d("ID3Info->Title: " + id3v2Tag.getTitle());
                    FlyLog.d("ID3Info->Album: " + album);
                    FlyLog.d("ID3Info->Year: " + id3v2Tag.getYear());
                    FlyLog.d("ID3Info->Genre: " + id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")");
                    FlyLog.d("ID3Info->Comment: " + id3v2Tag.getComment());
                    FlyLog.d("ID3Info->Lyrics: " + id3v2Tag.getLyrics());
                    FlyLog.d("ID3Info->Composer: " + id3v2Tag.getComposer());
                    FlyLog.d("ID3Info->Publisher: " + id3v2Tag.getPublisher());
                    FlyLog.d("ID3Info->Original artist: " + id3v2Tag.getOriginalArtist());
                    FlyLog.d("ID3Info->Album artist: " + id3v2Tag.getAlbumArtist());
                    FlyLog.d("ID3Info->Copyright: " + id3v2Tag.getCopyright());
                    FlyLog.d("ID3Info->URL: " + id3v2Tag.getUrl());
                    FlyLog.d("ID3Info->Encoder: " + id3v2Tag.getEncoder());
                    byte[] albumImageData = id3v2Tag.getAlbumImage();
                    if (albumImageData != null) {
                        FlyLog.d("ID3Info->Have album image data, length: " + albumImageData.length + " bytes");
                        FlyLog.d("ID3Info->Album image mime type: " + id3v2Tag.getAlbumImageMimeType());
                        bitmap = BitmapFactory.decodeByteArray(albumImageData, 0, albumImageData.length);
                    }
                } else if (mp3file.hasId3v1Tag()) {
                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                    artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? "" : id3v1Tag.getArtist();
                    album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? "" : id3v1Tag.getAlbum();
                    FlyLog.d("ID3Info->Track: " + id3v1Tag.getTrack());
                    FlyLog.d("ID3Info->Artist: " + artist);
                    FlyLog.d("ID3Info->Title: " + id3v1Tag.getTitle());
                    FlyLog.d("ID3Info->Album: " + album);
                    FlyLog.d("ID3Info->Year: " + id3v1Tag.getYear());
                    FlyLog.d("ID3Info->Genre: " + id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")");
                    FlyLog.d("ID3Info->Comment: " + id3v1Tag.getComment());
                }
                FlyLog.d("finish get id3 info url=%s",url);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ivImage.setImageBitmap(bitmap==null?defaultBitmap:bitmap);
                    tvSinger.setText(TextUtils.isEmpty(artist) ? getString(R.string.no_artist) : artist);
                    tvAlbum.setText(TextUtils.isEmpty(album) ? getString(R.string.no_album) : album);
                }
            });
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
        musicPlayer.getMediaPlay().seekTo(seekPos);
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
}
