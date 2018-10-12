// IMedia.aidl
package com.jancar.media;

import com.jancar.media.Notify;
// Declare any non-default types here with import statements

interface FlyMedia {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     *
     */
    void scanDisk(String disk);

    void notify(Notify notify);

    void registerNotify(Notify notify);

    void unregisterNotify(Notify notify);
}