package com.jancar.player.music;

import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.model.storage.Storage;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicActivity_AA1 extends MusicActivity_AP1 {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_music_aa1);
    }

    @Override
    public void initFragment() {
        fmName = new String[]{"MusicStorageFragment", "MusicPlayListFragment_AA1",
                "MusicArtistFragment", "MusicAlbumFragment", "MusicFloderFragment"};
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateTabView();
    }


    @Override
    public void imageUrlList(List<Image> imageUrlList) {
        super.imageUrlList(imageUrlList);
        updateTabView();
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        super.musicID3UrlList(musicUrlList);
        updateTabView();
    }

    private void updateTabView() {
        Set<String> fileSet = new HashSet<>();
        Set<String> albumSet = new HashSet<>();
        Set<String> artistSet = new HashSet<>();
        for (Music music : musicList) {
            String url = music.url;
            int last = url.lastIndexOf(File.separator);
            String path = url.substring(0, last).intern();
            fileSet.add(path);
            albumSet.add(music.album);
            artistSet.add(music.artist);
        }

        int diskSum = Storage.getInstance().getStorageSum();
        int musicSum = musicList == null ? 0 : musicList.size();
        titles = new String[]{getString(R.string.storage), getString(R.string.single), getString(R.string.artist), getString(R.string.album), getString(R.string.folder)};
        String floder = String.format(getString(R.string.storage) + "\n" + "(" + "%d" + ")", diskSum);
        String music = String.format(getString(R.string.single) + "\n" + "(" + "%d" + ")", musicSum);
        String file = String.format(getString(R.string.folder) + "\n" + "(" + "%d" + ")", fileSet.size());
        String artist = String.format(getString(R.string.artist) + "\n" + "(" + "%d" + ")", artistSet.size());
        String album = String.format(getString(R.string.album) + "\n" + "(" + "%d" + ")", albumSet.size());
        titles = new String[]{floder, music, artist,album,file};
        if (tabView != null) {
            tabView.setNewTitles(titles);
        }
    }

    @Override
    public void storageList(List<StorageInfo> storageList) {
        updateTabView();
    }
}
