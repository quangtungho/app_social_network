package vn.techres.line.adapter.restaurant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.album.ImageFolder
import vn.techres.line.databinding.ItemMediaRestaurantAlbumBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.interfaces.restaurant.MediaRestaurantAlbumHandler

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
class MediaRestaurantFromAlbumAdapter(var context: Context) :
    RecyclerView.Adapter<MediaRestaurantFromAlbumAdapter.ItemHolder>() {

    private var dataSource = ArrayList<ImageFolder>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var mediaRestaurantAlbumHandler: MediaRestaurantAlbumHandler? = null

    fun setDataSource(dataSource: ArrayList<ImageFolder>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClick(mediaRestaurantAlbumHandler: MediaRestaurantAlbumHandler) {
        this.mediaRestaurantAlbumHandler = mediaRestaurantAlbumHandler
    }

    inner class ItemHolder(val binding: ItemMediaRestaurantAlbumBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemMediaRestaurantAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            Utils.showImage(
                binding.imgMedia,
                String.format("%s%s", nodeJs.api_ads, dataSource[position].link)
            )
            if (dataSource[position].link.contains(".mp4")) {
                binding.imgPlay.visibility = View.VISIBLE
            } else {
                binding.imgPlay.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                mediaRestaurantAlbumHandler!!.clickMediaRestaurantAlbum(dataSource, position)
            }
        }

    }

}
