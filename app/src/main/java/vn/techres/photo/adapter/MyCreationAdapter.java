package vn.techres.photo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.techres.line.R;
import vn.techres.photo.ShareActivity;
import vn.techres.photo.model.MyAlbumMediaFile;

public class MyCreationAdapter extends RecyclerView.Adapter<MyCreationAdapter.MyViewHolder> {
    ArrayList<MyAlbumMediaFile> mMediaFile;
    private Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView galleryView;

        public MyViewHolder(View v) {
            super(v);
            galleryView = (ImageView) v.findViewById(R.id.galleryImage);
        }
    }

    public MyCreationAdapter(Context context, List<MyAlbumMediaFile> myMediaFile) {
        this.context = context;
        this.mMediaFile = (ArrayList<MyAlbumMediaFile>) myMediaFile;
    }

    @Override
    public MyCreationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_list, null, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final MyAlbumMediaFile mFile = mMediaFile.get(position);
        Glide.with(context).load(mFile.getMediaUri())
                .into(holder.galleryView);


        holder.galleryView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShareActivity.class);
            intent.putExtra("path", mFile.getMediaUri().getAbsolutePath());
            intent.putExtra("isCreation", true);

        });
    }

    @Override
    public int getItemCount() {
        return mMediaFile.size();
    }
}
