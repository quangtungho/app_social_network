package vn.techres.line.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomTagRecyclerView extends RecyclerView {
    public CustomTagRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomTagRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTagRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = MeasureSpec.makeMeasureSpec(700, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpec);
    }
}
