package vn.techres.photo.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.techres.line.R;
import vn.techres.photo.Activities.fragment.ScrapBookFragment;


public class ScrapBGAdapter extends RecyclerView.Adapter<ScrapBGAdapter.ViewHolder> {
    Context context;
    String[] emojies;
    ScrapBookFragment fragment;

    public ScrapBGAdapter(Context context, String[] emojies, ScrapBookFragment fragment) {
        this.context = context;
        this.emojies = emojies;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.bg_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String sticker = emojies[position];
        Log.e("path", sticker);

        Glide.with(context).load(Uri.parse("file:///android_asset/backgrounds_image/" + sticker)).into(holder.emogies);
        holder.linearLayout.setOnClickListener(v -> fragment.setBackground(sticker));
    }

    @Override
    public int getItemCount() {
        return emojies.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView emogies;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.emoji_layout);
            emogies = itemView.findViewById(R.id.emogies);
        }
    }
}


