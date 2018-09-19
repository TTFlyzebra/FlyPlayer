package com.jancar.media.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import com.jancar.media.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;

/**
 * Created by 李冰锋 on 2017/1/3 9:34.
 * E-mail:libf@ppfuns.com
 * Package: pers.nelon1990.lrcview
 */
public class LrcView extends View implements View.OnTouchListener {
    public final static String TAG = LrcView.class.getSimpleName();

    private static final int NONE_LINE = -1;

    private int mReferenceLineColor;
    private int mAfterLineTextColor;
    private int mBeforeLineTextColor;
    private int mCurrentLineTextColor;

    private TextPaint mTextPaint;
    private Paint mReferenceLinePaint;

    private float mTextSize;
    private float mLineSpacing;

    private Layout mLayout;
    private VelocityTracker mVelocityTracker;
    private OverScroller mFlingScroller;
    private Lrc mLrc;
    private int mScreenWidth;

    private SpannableStringBuilder mLyricString;
    /**
     * K: time  V: value
     */
    private Map<Long, Integer> mTimeLineMap;
    private int mInitLyricHeight;
    private float mDeltaY;

    private IPlayer mPlayer;


    private float mLastMoveY;
    private boolean mIsDragging = false;
    private boolean mIsTouching = false;
    private boolean mIsFlinging = false;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnTouchListener(this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LrcView);
        String lyricText = typedArray.getString(R.styleable.LrcView_lrc);
        mTextSize = typedArray.getDimension(R.styleable.LrcView_textSize, 24);
        mLineSpacing = typedArray.getFloat(R.styleable.LrcView_lineSpacing, 1.0f);
        mCurrentLineTextColor = typedArray.getColor(R.styleable.LrcView_currentLineColor, Color.GRAY);
        mBeforeLineTextColor = typedArray.getColor(R.styleable.LrcView_beforeLineColor, Color.GRAY);
        mAfterLineTextColor = typedArray.getColor(R.styleable.LrcView_afterLineColor, Color.GRAY);
        mReferenceLineColor = typedArray.getColor(R.styleable.LrcView_referenceLineColor, Color.GRAY);

        mTimeLineMap = new HashMap<>();
        mLyricString = new SpannableStringBuilder();

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mCurrentLineTextColor);
        mTextPaint.setTextSize(mTextSize);

        mReferenceLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReferenceLinePaint.setColor(mReferenceLineColor);

        mFlingScroller = new OverScroller(context, new DecelerateInterpolator());

        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;

        load(lyricText, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0, mInitLyricHeight);
        if (mIsDragging /*|| mIsFlinging*/) {
            int y = getScrollY() + mLayout.getLineBaseline(mCurLine);
            canvas.drawLine(0, y, mScreenWidth, y, mReferenceLinePaint);
        }
        mLayout.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        float desiredWidth = StaticLayout.getDesiredWidth(mLyricString.toString(), mTextPaint);

        int width = parentWidth, height = parentHeight;
        switch (widthMode) {
            case EXACTLY:
                /*
                MATCH_PARENT
                 */
                width = parentWidth;
                break;
            case AT_MOST:
                /*
                WRAP_CONTENT
                 */
                width = (int) (desiredWidth + 0.5f);
                break;
            case MeasureSpec.UNSPECIFIED:
                width = parentWidth;
                break;
        }

        mLayout = new DynamicLayout(mLyricString, mTextPaint, width, Layout.Alignment.ALIGN_CENTER, mLineSpacing, 0, false);
        makeLyricSpanableString(mCurLine);
        makeTimeLineMap();

        switch (heightMode) {
            case EXACTLY:
                /*
                MATCH_PARENT
                 */
                height = parentHeight;
                break;
            case AT_MOST:
                /*
                WRAP_CONTENT
                 */
                height = (int) (mLayout.getHeight() - mTextSize + 0.5f);
                break;
            case MeasureSpec.UNSPECIFIED:
                width = parentHeight;
                break;
        }

        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(width, widthMode),
                MeasureSpec.makeMeasureSpec(height, heightMode)
        );

        /*
        歌词的Y轴方向的偏移程度
         */
        int[] location = new int[2];
        getLocationOnScreen(location);
        int screenY = location[1];
//        mInitLyricHeight = (mScreenHeight - screenY - getNavigationBarHeight()) / 2;
        mInitLyricHeight = getHeight() / 2;
    }

    private void makeTimeLineMap() {
        if (mTimeLineMap != null) {
            mTimeLineMap.clear();
        }
        for (int i = 0; i < mLrc.getTimeTags().size(); i++) {
            Lrc.LrcPair lrcPair = mLrc.getTimeTags().get(i);
            Long time = lrcPair.first;
            int lineHeight = mLayout.getLineBottom(i);
            mTimeLineMap.put(time, lineHeight);
        }
    }

    private int mCurLine = NONE_LINE; //当前滚动至的行数

    private void onLine(int pLine, long pTime, final int pLineHeight, String pLrcText) {
        if (mCurLine == pLine) {
            return;
        }
        Log.d(TAG, "pLine: " + pLine + "\n" +
                "pTime: " + pTime + "\n" +
                "pLineHeight: " + pLineHeight + "\n" +
                "pLrcText: " + pLrcText + "\n");
        mCurLine = pLine;
        makeLyricSpanableString(pLine);

        if (mIsDragging || mIsFlinging) {
            /*
             手拖
             */
        } else {
            /*
             播放
             */
            if (pLineHeight > 0) {
                smoothScrollTo(0, pLineHeight);
            }
        }
    }

    private void makeLyricSpanableString(int pLine) {
        mLyricString.clearSpans();

        if (pLine <= NONE_LINE) {
            mLyricString.setSpan(new ForegroundColorSpan(mBeforeLineTextColor),
                    0, mLyricString.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return;
        }

        int lineStart = 0;
        int lineEnd = 0;

        if (pLine >= 0 && pLine < mLayout.getLineCount()) {
            lineStart = mLayout.getLineStart(pLine);
            lineEnd = mLayout.getLineEnd(pLine);
        }

        if (lineStart >= 1) {
            mLyricString.setSpan(new ForegroundColorSpan(mAfterLineTextColor),
                    0, lineStart - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        mLyricString.setSpan(new ForegroundColorSpan(mCurrentLineTextColor),
                lineStart, lineEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        if (lineEnd <= mLyricString.length() - 1) {
            mLyricString.setSpan(new ForegroundColorSpan(mBeforeLineTextColor),
                    lineEnd < mLyricString.length() ? lineEnd : mLyricString.length(), mLyricString.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    public void bind(@NonNull IPlayer pPlayer) {
        mPlayer = pPlayer;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (mPlayer.isPlay()) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    updateLineOnPlaying();
                                }
                            });
                        }
                        Thread.sleep(100);
                    } catch (Exception pE) {
                        pE.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    public void setAfterLineTextColor(@ColorInt int pAfterLineTextColor) {
        mAfterLineTextColor = pAfterLineTextColor;
        makeLyricSpanableString(mCurLine);
        invalidate();
    }

    public void setBeforeLineTextColor(@ColorInt int pBeforeLineTextColor) {
        mBeforeLineTextColor = pBeforeLineTextColor;
        makeLyricSpanableString(mCurLine);
        invalidate();
    }

    public void setCurrentLineTextColor(@ColorInt int pCurrentLineTextColor) {
        mCurrentLineTextColor = pCurrentLineTextColor;
        makeLyricSpanableString(mCurLine);
        invalidate();
    }

    public void setReferenceLineColor(@ColorInt int pReferenceLineColor) {
        mReferenceLineColor = pReferenceLineColor;
        invalidate();
    }

    public void load(String lrc) {
        load(lrc, true);
    }

    public void clear() {
        mCurLine = NONE_LINE;
        mLrc = Lrc.parse(null);
        mLyricString.clear();
        mLayout = new DynamicLayout(mLyricString, mTextPaint, getWidth(), Layout.Alignment.ALIGN_CENTER, mLineSpacing, 0, false);

        makeTimeLineMap();
        smoothScrollTo(0, 0);
        invalidate();
    }

    public boolean isEmpty() {
        return mLrc != null && mLrc.isEmpty();
    }

    private void load(String lrc, boolean invalidate) {
        clear();
        mLrc = Lrc.parse(lrc);
        for (int i = 0; i < mLrc.getTimeTags().size(); i++) {
            Lrc.LrcPair lrcPair = mLrc.getTimeTags().get(i);
            String lrcText = lrcPair.second;
            mLyricString.append(lrcText).append("\n");
        }
        makeTimeLineMap();
        makeLyricSpanableString(NONE_LINE);
        if (invalidate) {
            invalidate();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mLrc.isEmpty()) {
            return true;
        }


        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouching = true;

                mLastMoveY = event.getY();
                mVelocityTracker.clear();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsDragging) {
                    mIsDragging = true;
                    listenerOnScrollStart();
                }

                mDeltaY = mLastMoveY - event.getY();
                mLastMoveY = event.getY();
                scrollBy(0, (int) (mDeltaY + 0.5f));
                updateLineOnDrawing();

                mVelocityTracker.addMovement(event);

                listenerOnDragging();
                listenerOnScroll();
                break;
            case MotionEvent.ACTION_UP:
                mIsTouching = false;
                mIsDragging = false;

                startFling();
                break;
        }
        return true;
    }

    /**
     * 在滑动时，更新选定的歌词行
     */
    private void updateLineOnDrawing() {
        /*
        判断当前滑动到第几行
         */
        int minMaxHright = Integer.MAX_VALUE; //大于当前 scrollY 的最小高度
        long minMaxTime = -1; //上方高度所对应的时间值
        for (Long time : mTimeLineMap.keySet()) {
            Integer height = mTimeLineMap.get(time);
            if (getScrollY() < height
                    && height < minMaxHright) {
                minMaxHright = height;
                minMaxTime = time;
            }
        }
        Log.d(TAG, "updateLineOnDrawing: " + minMaxTime);

        for (Lrc.LrcPair lrcPair : mLrc.getTimeTags()) {
            if (lrcPair.first.equals(minMaxTime)) {
                onLine(mLrc.getTimeTags().indexOf(lrcPair), minMaxTime, minMaxHright, lrcPair.second);
                break;
            }
        }
    }

    /**
     * 播放时,更新歌词行
     */
    private void updateLineOnPlaying() {
        if (!mIsDragging) {
            long currentPosition = mPlayer.getCurrentPosition();
            Log.d(TAG, "updateLineOnPlaying: " + currentPosition);
            /*
            小于当前时间的最大值
             */
            long maxMinTime = Long.MIN_VALUE;
            for (Long aLong : mTimeLineMap.keySet()) {
                if (aLong < currentPosition && aLong > maxMinTime) {
                    maxMinTime = aLong;
                }
            }

            Integer integer = mTimeLineMap.get(maxMinTime);
            int lineHeight = (integer == null) ? 0 : integer;

            for (Lrc.LrcPair lrcPair : mLrc.getTimeTags()) {
                Log.d(TAG, "updateLineOnPlaying >>>>>>>>>>>>>>>>>>>> lrcPair: " + lrcPair.first + "                    " + lrcPair.second);
                Log.d(TAG, "updateLineOnPlaying >>>>>>>>>>>>>>>>>>>> maxMinTime: " + maxMinTime);

                if (lrcPair.first.equals(maxMinTime)) {
                    onLine(mLrc.getTimeTags().indexOf(lrcPair), maxMinTime, lineHeight, lrcPair.second);
                    break;
                }
            }
        }
    }

    private void smoothScrollTo(int x, int y) {
        mFlingScroller.startScroll(getScrollX(), getScrollY(), x, y - getScrollY(), 300);
        invalidate();
    }


    private void startFling() {
        mVelocityTracker.computeCurrentVelocity(1000);
        float yVelocity = Math.abs(mVelocityTracker.getYVelocity()) * (mDeltaY > 0 ? 1 : -1);
        float xVelocity = Math.abs(mVelocityTracker.getXVelocity()) * (mDeltaY > 0 ? 1 : -1);

        mFlingScroller.fling(
                getScrollX(), getScrollY(),
                ((int) (xVelocity + 0.5f)), ((int) (yVelocity + 0.5f)),
                0, 0,
                0, (int) (mLayout.getHeight() - mLayout.getLineBaseline(mCurLine) * (1 + mLineSpacing) + 0.5f),
                0, (int) (mTextSize * (1 + mLineSpacing) * 1 + 0.5f)
        );
        mIsFlinging = true;
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mFlingScroller.computeScrollOffset() && !mIsTouching) {
            int currX = mFlingScroller.getCurrX();
            int currY = mFlingScroller.getCurrY();

            scrollTo(currX, currY);
            if (mIsFlinging) {
                updateLineOnDrawing();
            }
            /*
            调用回调
             */
            listenerOnFling();
            listenerOnScroll();
        } else {
            if (mFlingScroller.computeScrollOffset()) {
                mFlingScroller.forceFinished(true);
            }

            if (mIsFlinging) {
                mIsFlinging = false;
                /*
                调用回调
                 */
                listenerOnScrollFinish();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
    }

    /**
     * lrc歌詞的封裝
     */
    private static class Lrc {
        private Map<String, String> idTags;
        private List<LrcPair> timeTags;

        private Lrc() {
            idTags = new HashMap<>();
            timeTags = new ArrayList<>();
        }

        List<LrcPair> getTimeTags() {
            return timeTags;
        }

        public Map<String, String> getIdTags() {
            return idTags;
        }

        public boolean isEmpty() {
            return idTags.isEmpty() && timeTags.isEmpty();
        }

        static Lrc parse(String txt) {
            Lrc lrc = new Lrc();
            if (txt == null) {
                return lrc;
            }
            try {
                Matcher matcher = Pattern.compile("[^\n]+")
                        .matcher(txt);
                while (matcher.find()) {
                    /*
                    逐行解析
                    */
                    String singleLine = matcher.group();

                    long time = 0;
                    String lrcTxt;

                    Matcher singleLineMatcher = Pattern.compile("[^\\[\\]]+")
                            .matcher(singleLine.trim());
                    while (singleLineMatcher.find()) {
                        String matcherResult = singleLineMatcher.group();

                        if (singleLineMatcher.start() == 1) {
                            /*
                            tag部分
                             */
                            if (Pattern.compile("\\d+:\\d+\\.\\d+").matcher(matcherResult).matches()) {
                                /*
                                time tag
                                 */
                                // FIXME: 2017/1/9 在 API 19 会有问题
                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                                    Date date = new SimpleDateFormat("mm:ss.SS")
                                            .parse(matcherResult.trim());
                                    time = date.getTime();
                                } else {
                                    String[] split = matcherResult.trim().split(":");
                                    String[] strings = split[1].trim().split("\\.");

                                    Integer min = Integer.valueOf(split[0]);
                                    Integer sec = Integer.valueOf(strings[0]);
                                    Integer msc = Integer.valueOf(strings[1]);

                                    time = (min * 60 + sec) * 1000 + msc;
                                }
                            } else {
                                /*
                                id tag
                                 */
                                if (matcherResult.contains("t_time")) {
                                    // TODO: 2017/1/3
                                } else {
                                    Matcher idTagMatcher = Pattern.compile("[^:]+")
                                            .matcher(matcherResult.trim());
                                    String k = null, v;
                                    if (idTagMatcher.find()) {
                                        k = idTagMatcher.group();
                                    }

                                    if (idTagMatcher.find()) {
                                        v = idTagMatcher.group();
                                        lrc.idTags.put(k, v);
                                    }
                                }
                            }
                        } else {
                            /*
                            歌词部分
                             */
                            lrcTxt = matcherResult.trim();
                            lrc.timeTags.add(new LrcPair(time, lrcTxt));
                        }
                    }
                }

                Collections.sort(lrc.timeTags);
            } catch (ParseException pE) {
                pE.printStackTrace();
                lrc.idTags.clear();
                lrc.timeTags.clear();
            }

            return lrc;
        }

        static class LrcPair extends Pair<Long, String> implements Comparable<LrcPair> {
            /**
             * Constructor for a Pair.
             *
             * @param first  the first object in the Pair
             * @param second the second object in the pair
             */
            LrcPair(Long first, String second) {
                super(first, second);
            }

            @Override
            public int compareTo(@NonNull LrcPair o) {
                return this.first > o.first ? 1 : -1;
            }
        }
    }

    public interface IPlayer {
        long getCurrentPosition();

        boolean isPlay();
    }

    private List<ScrollListener> mScrollerListener = new ArrayList<>();

    public void addScrollListener(ScrollListener pScrollListener) {
        mScrollerListener.add(pScrollListener);
    }

    public void remove(ScrollListener pListener) {
        mScrollerListener.remove(pListener);
    }

    public void removeAllListener() {
        mScrollerListener.clear();
    }

    private void listenerOnScrollStart() {
        for (ScrollListener scrollListener : mScrollerListener) {
            scrollListener.onScrollStart();
        }
    }

    private void listenerOnFling() {
        for (ScrollListener scrollListener : mScrollerListener) {
            scrollListener.onFling();
        }
    }

    private void listenerOnDragging() {
        for (ScrollListener scrollListener : mScrollerListener) {
            scrollListener.onDragging();
        }
    }

    private void listenerOnScroll() {
        for (ScrollListener scrollListener : mScrollerListener) {
            scrollListener.onScroll();
        }
    }

    private void listenerOnScrollFinish() {
        for (ScrollListener scrollListener : mScrollerListener) {
            scrollListener.onScrollFinish();
        }
    }

    public interface ScrollListener {
        void onScrollStart();

        void onFling();

        void onDragging();

        void onScroll();

        void onScrollFinish();
    }


}
