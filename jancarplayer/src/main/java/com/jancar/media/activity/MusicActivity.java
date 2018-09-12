package com.jancar.media.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.listener.IMusicPlayerListener;
import com.jancar.media.listener.IUsbMediaListener;
import com.jancar.media.model.IMusicPlayer;
import com.jancar.media.model.MusicPlayer;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends BaseActivity implements
        SeekBar.OnSeekBarChangeListener,
        IMusicPlayerListener,
        IUsbMediaListener,
        FlyTabView.OnItemClickListener,
        View.OnClickListener {
    private FlyTabView tabView;
    private String titles[] = new String[]{"存储器", "单曲", "歌手", "专辑", "文件夹"};
    private String fmName[] = new String[]{"StorageFragment", "MusicPlayListFragment", "StorageFragment", "StorageFragment", "StorageFragment"};
    public List<String> musicList = new ArrayList<>();
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();

    private SeekBar seekBar;
    private TextView seekBarSartTime, seekBarEndTime;
    private ImageView playFore, playNext, play, leftMenu;
    private RelativeLayout leftLayout;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            int playPos = musicPlayer.getMediaPlay().getCurrentPosition();
            int min = playPos / 1000 / 60;
            int sec = playPos / 1000 % 60;
            String text = min + ":" + (sec > 9 ? sec : "0" + sec);
            seekBarSartTime.setText(text);
            seekBar.setProgress(playPos);
            mHandler.postDelayed(seekBarTask,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        musicPlayer.init(getApplicationContext());
        musicPlayer.addListener(this);
        usbMediaScan.addListener(this);

        titles = new String[]{getString(R.string.storage), getString(R.string.single), getString(R.string.singer), getString(R.string.album), getString(R.string.folder)};
        initView();

        tabView.setTitles(titles);
        replaceFragment(fmName[1]);
        tabView.setFocusPos(1);

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
        tabView.setOnItemClickListener(this);
        playFore.setOnClickListener(this);
        playNext.setOnClickListener(this);
        play.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        leftMenu.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        usbMediaScan.removeListener(this);
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
    public void videoUrlList(List<String> videoUrlList) {

    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {

    }

    @Override
    public void usbRemove(String usbstore) {

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
                if(musicPlayer.getMediaPlay().isPlaying()){
                    musicPlayer.puase();
                }else{
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
                int videoTime = musicPlayer.getMediaPlay().getDuration();
                seekBar.setMax(videoTime);
                int min = videoTime / 1000 / 60;
                int sec = videoTime / 1000 % 60;
                String text = min + ":" + (sec > 9 ? sec : "0" + sec);
                seekBarEndTime.setText(text);
                mHandler.removeCallbacks(seekBarTask);
                mHandler.post(seekBarTask);
                break;
            case MusicPlayer.STATUS_ERROR:
            case MusicPlayer.STATUS_PAUSE:
            case MusicPlayer.STATUS_LOADING:
                break;
        }

        play.setImageResource(musicPlayer.isPlaying()?R.drawable.media_pause:R.drawable.media_play);

    }



    private int seekPos;
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

    private int currenPos = 0;
    private void playNext() {
        if(musicList!=null&&!musicList.isEmpty()){
            if(currenPos<musicList.size()-1){
                currenPos++;
                musicPlayer.play(musicList.get(currenPos));
            }
        }
    }

    private void playFore() {
        if(musicList!=null&&!musicList.isEmpty()){
            if(currenPos>0){
                currenPos--;
                musicPlayer.play(musicList.get(currenPos));
            }
        }
    }
}
