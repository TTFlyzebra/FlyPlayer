// Notify.aidl
package com.jancar.media;

// Declare any non-default types here with import statements

interface Notify {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void notifyMusic(inout List<String> list);

    void notifyVideo(inout List<String> list);

    void notifyImage(inout List<String> list);
}
