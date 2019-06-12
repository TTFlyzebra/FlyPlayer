package com.jancar.player.video;

import com.jancar.media.data.StorageInfo;
import com.jancar.media.data.Video;
import com.jancar.media.model.storage.Storage;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class VideoActivity_APA extends VideoActivity_AP1 {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.giraffe_player_a);
    }

    @Override
    public void initFragment() {
        fmName = new String[]{"VideoStorageFragment", "VideoPlayListFragment_AP2", "VideoFloderFragment"};
    }

    @Override
    public void setLeftImg(boolean flag) {
        leftMenu.setImageResource(flag ? R.drawable.media_list_menu_open_video : R.drawable.media_list_menu_close_video);

    }

    @Override
    public void setPlayImg() {
        play_pause.setImageResource(player.isPlaying() ? R.drawable.media_pause_video : R.drawable.media_play_video);
    }

    @Override
    public void setPlayForeImg() {
        play_fore.setImageResource(R.drawable.media_fore_01_video);
    }

    @Override
    public void setPlayNextImg() {
        play_next.setImageResource(R.drawable.media_next_01_video);
    }

    @Override
    public void setPlayForePressedImg() {
        play_fore.setImageResource(R.drawable.media_fore_video);
    }

    @Override
    public void setPlayNextPressedImg() {
        play_next.setImageResource(R.drawable.media_next_video);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateTabView();
    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        super.videoUrlList(videoUrlList);
        updateTabView();
    }

    private void updateTabView() {
        int diskSum = Storage.getInstance().getStorageSum();
        int imgSum = videoList == null ? 0 : videoList.size();
        int fileSum = getFloderSum();
        FlyLog.d("updateTabView num=%d,%d,%d", diskSum, imgSum, fileSum);
        String floder = String.format(getString(R.string.disk_list) + "\n" + "(" + "%d" + ")", diskSum);
        String photo = String.format(getString(R.string.play_list) + "\n" + "(" + "%d" + ")", imgSum);
        String file = String.format(getString(R.string.file_list) + "\n" + "(" + "%d" + ")", fileSum);
        titles = new String[]{floder, photo, file};
        if (tabView != null) {
            tabView.setNewTitles(titles);
        }
    }

    @Override
    public void storageList(List<StorageInfo> storageList) {
        updateTabView();
    }
}
