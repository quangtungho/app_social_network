package vn.techres.line.helper.topbottombehavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import org.jetbrains.annotations.NotNull;

public class LockableBottomSheetBehavior<V extends View> extends TopSheetBehavior<V> {
    private boolean mLocked = false;
    public LockableBottomSheetBehavior() {}

    public LockableBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    @Override
    public boolean onInterceptTouchEvent(@NotNull CoordinatorLayout parent, V child, @NotNull MotionEvent event) {
        boolean handled = false;

        if (!mLocked) {
            handled = super.onInterceptTouchEvent(parent, child, event);
        }

        return handled;
    }

    @Override
    public boolean onTouchEvent(@NotNull CoordinatorLayout parent, V child, @NotNull MotionEvent event) {
        boolean handled = false;

        if (!mLocked) {
            handled = super.onTouchEvent(parent, child, event);
        }

        return handled;
    }

    @Override
    public boolean onStartNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull V child, @NotNull View directTargetChild, @NotNull View target, int nestedScrollAxes) {
        boolean handled = false;

        if (!mLocked) {
            handled = super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
        }

        return handled;
    }

    @Override
    public void onNestedPreScroll(@NotNull CoordinatorLayout coordinatorLayout, @NotNull V child, @NotNull View target, int dx, int dy, @NotNull int[] consumed) {
        if (!mLocked) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        }
    }

    @Override
    public void onStopNestedScroll(@NotNull CoordinatorLayout coordinatorLayout, V child, @NotNull View target) {
        if (!mLocked) {
            super.onStopNestedScroll(coordinatorLayout, child, target);
        }
    }

    @Override
    public boolean onNestedPreFling(@NotNull CoordinatorLayout coordinatorLayout, @NotNull V child, @NotNull View target, float velocityX, float velocityY) {
        boolean handled = false;

        if (!mLocked) {
            handled = super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
        }

        return handled;

    }
}
