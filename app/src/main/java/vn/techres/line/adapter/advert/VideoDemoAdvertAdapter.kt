package vn.techres.line.adapter.advert

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZVideoPlayerStandard
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import vn.techres.line.R
import vn.techres.line.data.model.avert.VideoDemoAdvert
import vn.techres.line.databinding.ItemVideoDemoAdvertBinding
import vn.techres.line.helper.CurrentConfigNodeJs

class VideoDemoAdvertAdapter(var context: Context) :
    RecyclerView.Adapter<VideoDemoAdvertAdapter.ItemHolder>() {
    private var dataSource = ArrayList<VideoDemoAdvert>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<VideoDemoAdvert>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemVideoDemoAdvertBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemVideoDemoAdvertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.videoPlayer.setUp(
                String.format("%s%s", nodeJs.api_ads, dataSource[position].url),
                JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,
                ""
            )
            Glide.with(context)
                .load(String.format("%s%s", nodeJs.api_ads, dataSource[position].url))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                )
                .into(binding.videoPlayer.thumbImageView)
            if (dataSource[position].auto == 1) {
                binding.videoPlayer.startButton.performClick()
            }
        }

    }
    override fun getItemCount(): Int {
        return dataSource.size
    }

}