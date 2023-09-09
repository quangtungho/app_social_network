package vn.techres.photo.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import vn.techres.line.R;
import vn.techres.photo.Activities.fragment.BaseFragment;
import vn.techres.photo.Activities.fragment.ScrapBookFragment;
import vn.techres.photo.listener.OnChooseColorListener;
import vn.techres.photo.listener.OnShareImageListener;

public class ScrapBookActivity extends BaseFragmentActivity implements
        OnShareImageListener, OnChooseColorListener {
    public static final int PHOTO_TYPE = 1;
    public static final int FRAME_TYPE = 2;
    public static final String EXTRA_CREATED_METHOD_TYPE = "methodType";

    private int mSelectedColor = Color.GREEN;
    private boolean mClickedShareButton = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrap_book);

        if (savedInstanceState == null) {

            BaseFragment fragment = new ScrapBookFragment();

            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .commit();
        } else {
            mClickedShareButton = savedInstanceState.getBoolean("mClickedShareButton", false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mClickedShareButton", mClickedShareButton);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mClickedShareButton) {
            mClickedShareButton = false;

        }
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = (BaseFragment) getVisibleFragment();
        if (fragment instanceof ScrapBookFragment) {
            super.onBackPressed();
        } else {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.popBackStack();
        }
    }

    @Override
    public void onShareImage(String imagePath) {
        mClickedShareButton = true;
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        return fragmentManager.findFragmentById(R.id.frame_container);
    }

    @Override
    public void onShareFrame(String imagePath) {
        Toast.makeText(this, "Shared image frame: " + imagePath,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSelectedColor(int color) {
        mSelectedColor = color;
    }

    @Override
    public int getSelectedColor() {
        return mSelectedColor;
    }
}

