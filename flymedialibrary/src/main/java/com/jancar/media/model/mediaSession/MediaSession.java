package com.jancar.media.model.mediaSession;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import com.jancar.key.KeyDef;
import com.jancar.media.JacMediaSession;
import com.jancar.media.model.listener.IMediaEventListerner;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.List;

import static com.jancar.key.KeyDef.KeyAction.KEY_ACTION_UP;

public class MediaSession implements IMediaSession {
    private JacMediaSession jacMediaSession;
    private List<IMediaEventListerner> iMediaEventListerners = new ArrayList<>();
    private Handler mHander = new Handler(Looper.getMainLooper());

    public MediaSession(Context context) {
        jacMediaSession = new JacMediaSession(context) {
            @Override
            public boolean OnKeyEvent(int key, int state) throws RemoteException {
                FlyLog.d("OnKeyEvent key=%d,state=%d",key,state);
                boolean bRet = false;
                try {
                    KeyDef.KeyType keyType = KeyDef.KeyType.nativeToType(key);
                    KeyDef.KeyAction keyAction = KeyDef.KeyAction.nativeToType(state);
                    bRet = true;
                    switch (keyType) {
                        case KEY_PREV:
                            if (keyAction == KEY_ACTION_UP) {
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.playPrev();
                                        }
                                    }
                                });
                            }
                            break;
                        case KEY_NEXT:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.playNext();
                                        }
                                    }
                                });
                            break;
                        case KEY_PAUSE:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.pause();
                                        }
                                    }
                                });
                            break;
                        case KEY_PLAY:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.start();
                                        }
                                    }
                                });
                            break;
                        case KEY_PPAUSE:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.playOrPause();
                                        }
                                    }
                                });
                            break;
                        case KEY_FF:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.KEY_FF();
                                        }
                                    }
                                });
                            break;
                        case KEY_FB:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.KEY_FB();
                                        }
                                    }
                                });
                            break;
                        case KEY_REPEAT:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.KEY_REPEAT();
                                        }
                                    }
                                });
                            break;
                        case KEY_SHUFFLE:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.KEY_SHUFFLE();
                                        }
                                    }
                                });
                            break;
                        case KEY_LIST:
                            if (keyAction == KEY_ACTION_UP)
                                mHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (IMediaEventListerner listerner : iMediaEventListerners) {
                                            listerner.KEY_LIST();
                                        }
                                    }
                                });
                            break;
                        default:
                            bRet = false;
                            break;
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                return bRet;
            }
        };
    }

    @Override
    public void notifyPlayState(int playStatus) {
        FlyLog.d("notifyPlayState %d",playStatus);
        jacMediaSession.notifyPlayState(playStatus);
    }

    @Override
    public void init() {
        jacMediaSession.setActive(true);
    }

    @Override
    public void release() {
        jacMediaSession.setActive(false);
        mHander.removeCallbacksAndMessages(null);
    }

    @Override
    public void notifyRepeat(int staut) {
        jacMediaSession.notifyRepeat(staut);
    }

    @Override
    public void notifyPlayId(int current, int total) {
        FlyLog.d("notifyPlayId %d,%d",current,total);
        jacMediaSession.notifyPlayId(current,total);
    }

    @Override
    public void notifyProgress(int position, int duration) {
        FlyLog.d("notifyProgress %d,%d", position, duration);
        jacMediaSession.notifyProgress(position, duration);
    }

    @Override
    public void notifyPlayUri(String url) {
        FlyLog.d("notifyPlayUri "+ url);
        jacMediaSession.notifyPlayUri(url);
    }

    @Override
    public void notifyId3(String title, String artist, String album, byte[] albumImageData) {
        FlyLog.d("onID3 title=%s,artist=%s,album=%s", title, artist, album);
        jacMediaSession.notifyId3(title, artist, album, albumImageData);
    }

    @Override
    public void addEventListener(IMediaEventListerner listerner) {
        iMediaEventListerners.add(listerner);
    }

    @Override
    public void removeEventListener(IMediaEventListerner listerner) {
        iMediaEventListerners.remove(listerner);

    }
}
