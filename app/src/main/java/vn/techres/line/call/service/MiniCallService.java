package vn.techres.line.call.service;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import vn.techres.line.R;
import vn.techres.line.data.model.eventbus.MsgEvent;
import vn.techres.line.data.model.utils.ConfigNodeJs;
import vn.techres.line.helper.CurrentConfigNodeJs;
import vn.techres.line.helper.techresenum.TechresEnum;
import vn.techres.line.helper.utils.ChatUtils;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public class MiniCallService extends Service {
    private WindowManager wm;
    private View view;
    private WindowManager.LayoutParams params;
    private int touchSlop = 0;
    private final int margin = 0;
    final int statusBarHeight = BarUtils.getStatusBarHeight();
    private int screenWidth = 0;
    private int screenHeight = 0;
    private PowerManager.WakeLock wakeLock;
    private static boolean isStarted = false;
    long elapsedRealtime;
    private ConfigNodeJs configNodeJs = CurrentConfigNodeJs.INSTANCE.getConfigNodeJs(this);

    String name;
    String avatar;

    @Override
    public void onCreate() {
        super.onCreate();
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isStarted) {
            return START_NOT_STICKY;
        }
        isStarted = true;

        elapsedRealtime = intent.getExtras().getLong(TechresEnum.EXTRA_CHORONOMETER.toString(), 0);
        name = intent.getExtras().getString(TechresEnum.EXTRA_NAME_PERSONAL.toString());
        avatar = intent.getExtras().getString(TechresEnum.EXTRA_AVATAR_PERSONAL.toString());

        try {
            showFloatingWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MsgEvent<Object> messageEvent) {
        int code = messageEvent.getCode();
        if (code == MsgEvent.CODE_ON_CALL_ENDED) {
            hideFloatBox();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        releaseWakeLock();
        super.onDestroy();
        try {
            wm.removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isStarted = false;
    }

    @SuppressLint("InflateParams")
    private void showFloatingWindow() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        screenWidth = ScreenUtils.getScreenWidth();
        screenHeight = ScreenUtils.getScreenHeight();
        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.type = type;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        params.format = PixelFormat.TRANSLUCENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.END | Gravity.TOP;

        view = LayoutInflater.from(this).inflate(R.layout.mini_mode_call, null);

        view.setOnTouchListener(onTouchListener);
        params.x = margin;
        params.y = statusBarHeight;
        wm.addView(view, params);
        showInfo();
    }

    private void showInfo() {
        TextView nameUser = view.findViewById(R.id.nameUser);
        ImageView avatarView = view.findViewById(R.id.avatar);
        Chronometer chronometer = view.findViewById(R.id.chronometer);

        chronometer.setBase(elapsedRealtime);
        chronometer.start();

        ChatUtils.INSTANCE.callPhotoAvatar(avatar, avatarView, configNodeJs.getApi_ads());
        nameUser.setText(name);
    }

    private void hideFloatBox() {
        stopSelf();
    }

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        int startX = 0;
        int startY = 0;
        boolean isPerformClick = false;
        int finalMoveX = 0;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("click", "onTouch: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    startX = (int) event.getX();
                    startY = (int) event.getY();
                    isPerformClick = true;
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    //判断是CLICK还是MOVE
                    //只要移动过，就认为不是点击
                    if (Math.abs(startX - event.getX()) >= touchSlop || Math.abs(startY - event.getY()) >= touchSlop) {
                        isPerformClick = false;
                    }
//                    LogUtil.d(TAG, "event.rawX = " + event.rawX + "; startX = " + startX)
                    params.x = screenWidth - (int) (event.getRawX() - startX) - view.getWidth();
                    //这里修复了刚开始移动的时候，悬浮窗的y坐标是不正确的，要减去状态栏的高度
                    params.y = (int) (event.getRawY() - startY - statusBarHeight);
                    if (params.x < margin) params.x = margin;
                    if (params.x > screenWidth - margin) params.x = screenWidth - margin;

                    if (params.y + view.getHeight() + statusBarHeight > screenHeight - margin)
                        params.y =
                                screenHeight - statusBarHeight - view.getHeight();

//                    LogUtil.d(TAG, "x---->" + params.x)
//                    LogUtil.d(TAG, "y---->" + params.y)
                    updateViewLayout(); //更新v 的位置
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    if (isPerformClick) {
                        v.performClick();
                        try {
                            clickToResume();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    //判断v是在Window中的位置，以中间为界
                    if (params.x + v.getMeasuredWidth() / 2 >= wm.getDefaultDisplay().getWidth() / 2) {
                        finalMoveX = wm.getDefaultDisplay().getWidth() - v.getMeasuredWidth() - margin;
                    } else {
                        finalMoveX = margin;
                    }
                    stickToSide();
                    return !isPerformClick;
                }
            }
            return false;
        }

        private void stickToSide() {
            ValueAnimator animator =
                    ValueAnimator.ofInt(params.x, finalMoveX).setDuration(Math.abs(params.x - finalMoveX));
            animator.setInterpolator(new BounceInterpolator());
            animator.addUpdateListener(animation -> {
                params.x = (int) animation.getAnimatedValue();
                updateViewLayout();
            });
            animator.start();
        }
    };

    private void clickToResume() throws ClassNotFoundException {
        Intent i = new Intent(this, Class.forName("com.techres.vn.tms.videocall.mvp.ui.activity.CallActivity"));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
        hideFloatBox();
    }

    private void updateViewLayout() {
        if (wm != null && view != null) {
            wm.updateViewLayout(view, params);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }
    }
}
