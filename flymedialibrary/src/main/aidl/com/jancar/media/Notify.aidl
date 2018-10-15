// Notify.aidl
package com.jancar.media;

import com.jancar.media.data.Music;
import com.jancar.media.data.Image;
import com.jancar.media.data.Video;

// Declare any non-default types here with import statements

interface Notify {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void notifyMusic(inout List<Music> list);

    void notifyVideo(inout List<Video> list);

    void notifyImage(inout List<Image> list);

    void notifyID3Music(inout List<Music> list);

    void notifyPath(String path);

    void notifyFinish(String path);
}
