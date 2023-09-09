package vn.techres.line.data.model;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import java.util.List;

import vn.techres.line.helper.WriteLog;

public class WheelView extends androidx.appcompat.widget.AppCompatImageView {

    private LuckyRoundItemSelectedListener mLuckyRoundItemSelectedListener;

    private int mTargetIndex;
    private int mRoundOfNumber = 4;
    public boolean isRunning = false;

    private List<LuckyItem> mLuckyItemList;

    public WheelView(Context context) {
        super(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(List<LuckyItem> luckyItemList) {
        this.mLuckyItemList = luckyItemList;
    }

    public interface LuckyRoundItemSelectedListener {
        void luckyRoundItemSelected(int index);
    }

    public void setLuckyRoundItemSelectedListener(LuckyRoundItemSelectedListener listener) {
        this.mLuckyRoundItemSelectedListener = listener;
    }

    public void startLuckyWheelWithTargetIndex(int index) {
        rotateTo(index);
    }

    /**
     * @return
     */
    private float getAngleOfIndexTarget() {
        return (360f / mLuckyItemList.size()) * mTargetIndex;
    }

    /**
     * @param numberOfRound
     */
    public void setRound(int numberOfRound) {
        mRoundOfNumber = numberOfRound;
        WriteLog.INSTANCE.d("mRoundOfNumber", String.valueOf(mRoundOfNumber));
    }

    /**
     * @param index
     */
    public void rotateTo(int index) {
        mTargetIndex = index;
        setRotation(0);
        float targetAngle = 360f * mRoundOfNumber - getAngleOfIndexTarget() ;

        animate()
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(mRoundOfNumber * 1000L)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isRunning = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isRunning = false;
                        if (mLuckyRoundItemSelectedListener != null) {
                            mLuckyRoundItemSelectedListener.luckyRoundItemSelected(mTargetIndex);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        throw new UnsupportedOperationException();
                    }
                })
                .rotation(targetAngle)
                .start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
