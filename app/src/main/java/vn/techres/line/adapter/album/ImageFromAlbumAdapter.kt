package vn.techres.line.adapter.album

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.album.ClickImageAlbum
import vn.techres.line.data.model.album.ImageFolder
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemImageFromFolderBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils

class ImageFromAlbumAdapter(var context: Context?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataSource = ArrayList<ImageFolder>()
    private var configNodeJs = context?.let { CurrentConfigNodeJs.getConfigNodeJs(it) }
    private var user = context?.let { CurrentUser.getCurrentUser(it) }
    private var clickImageAlbum: ClickImageAlbum? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<ImageFolder>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickImage(clickImageAlbum: ClickImageAlbum) {
        this.clickImageAlbum = clickImageAlbum
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageFromFolderHolder(
            ItemImageFromFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        context?.let {
            configNodeJs?.let { it1 ->
                clickImageAlbum?.let { it2 ->
                    (holder as ImageFromFolderHolder).bind(
                        it,
                        it1, dataSource, position, it2
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class ImageFromFolderHolder(var binding: ItemImageFromFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            context: Context,
            configNodeJs: ConfigNodeJs,
            dataSource: ArrayList<ImageFolder>,
            position: Int,
            clickImageAlbum: ClickImageAlbum
        ) {
            var imageList = dataSource
            var params = binding.image.layoutParams
            params.width = TechResApplication.widthView / 2
            binding.image.layoutParams = params
            Utils.getGlide(binding.image,imageList[position].link, configNodeJs)
            if(imageList[position].link.contains(".mp4"))
                binding.playVideo.visibility = View.VISIBLE
            else
                binding.playVideo.visibility = View.GONE
            binding.image.setOnClickListener {
                clickImageAlbum.clickImageAlbum(dataSource, position)
            }
            binding.more.setOnClickListener {
                clickImageAlbum.clickImageAlbumMore(dataSource[position], binding.more)
            }
        }
    }


    companion object {

    }
}