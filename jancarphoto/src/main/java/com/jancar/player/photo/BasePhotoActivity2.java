package com.jancar.player.photo;

import com.jancar.media.data.Image;
import com.jancar.media.model.storage.Storage;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class BasePhotoActivity2 extends BasePhotoActivity1 {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_photo);
    }

    @Override
    public void initFragment() {
        fmName = new String[]{"PhotoStorageFragment", "PhotoPlayListFragment", "PhotoFloderFragment"};
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

    private void updateTabView() {
        int diskSum = Storage.getInstance().getStorageSum();
        int imgSum = imageList == null ? 0 : imageList.size();
        int fileSum = getFloderSum();
        FlyLog.d("updateTabView num=%d,%d,%d", diskSum, imgSum, fileSum);
        String floder = String.format(getString(R.string.disk_list) + "\n" + "(" + "%d" + ")", diskSum);
        String photo = String.format(getString(R.string.photo_list) + "\n" + "(" + "%d" + ")", imgSum);
        String file = String.format(getString(R.string.file_list) + "\n" + "(" + "%d" + ")", fileSum);
        titles = new String[]{floder, photo, file};
        tabView.setNewTitles(titles);
    }

}
