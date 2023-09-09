package vn.techres.line.helper;

import android.os.Handler;

import androidx.appcompat.widget.SearchView;

public abstract class DelayedOnQueryTextListener implements SearchView.OnQueryTextListener  {
    private final Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        handler.removeCallbacks(runnable);
        runnable = () -> onDelayerQueryTextChange(s);
        handler.postDelayed(runnable, 500);
        return true;
    }

    public abstract void onDelayerQueryTextChange(String s);
}
