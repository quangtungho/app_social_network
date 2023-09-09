package vn.techres.line.view;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;

import vn.techres.line.R;


public class ScrollMulrowsEditText extends AppCompatEditText {

    private  String tag = "ScMulrowsEditText";
    private int mOffsetHeight;

    private int mHeight;


    private int mVert = 0;


    public ScrollMulrowsEditText(Context context) {
        super(context);
    }

    public ScrollMulrowsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttribute(context, attrs, 0);
    }

    public ScrollMulrowsEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int paddingTop;
        int paddingBottom;
        int height;
        int mLayoutHeight;

        //获得内容面板
        Layout mLayout = getLayout();
        //获得内容面板的高度
        mLayoutHeight = mLayout.getHeight();
        //获取上内边距
        paddingTop = getTotalPaddingTop();
        //获取下内边距
        paddingBottom = getTotalPaddingBottom();

        //获得控件的实际高度
        height = mHeight; // getHeight()

        //计算滑动距离的边界
        mOffsetHeight = mLayoutHeight + paddingTop + paddingBottom - height;

        setOnTouchListener();
    }

    private void initAttribute(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScrollMulrowsEditText, defStyleAttr, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.ScrollMulrowsEditText_sc_mul_edit_height) {
                mHeight = array.getDimensionPixelSize(attr, 0);
            }
        }
        array.recycle();
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        mVert = vert;

        if (vert == mOffsetHeight || vert == 0) {
            getParent().requestDisallowInterceptTouchEvent(false);
            Log.d(tag, "vert requestDisallowInterceptTouchEvent  false ");
        }
    }

    //滑动到上边缘
    public boolean isUpperEdge(){
        return mVert == 0;
    }

    //滑动到下边缘
    public boolean isLowerEdge(){
        return mVert == mOffsetHeight;
    }

    private float scrollBeginY;
    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchListener(){
        setOnTouchListener((v, event) -> {
            if(MotionEvent.ACTION_DOWN == event.getAction()){
                scrollBeginY = event.getY();
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
            Log.d(tag, "event.getY" + event.getY());
            if(canScrollVertically(1)){
                v.getParent().requestDisallowInterceptTouchEvent(!isUpperEdge() || !(event.getY() >= scrollBeginY));
            } else if(canScrollVertically(-1)){
                v.getParent().requestDisallowInterceptTouchEvent(!isLowerEdge() || !(event.getY() <= scrollBeginY));
            } else {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }


}
