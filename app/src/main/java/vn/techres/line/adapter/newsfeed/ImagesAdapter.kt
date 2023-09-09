package vn.techres.line.adapter.newsfeed


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_media_edit_my_post.view.*
import vn.techres.line.R
import vn.techres.line.data.model.utils.Media
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.MediaHandler


class ImagesAdapter(val context: Context) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolderImage>() {

    private var dataSource =  ArrayList<Media>()
    private var mediaHandler: MediaHandler? = null

    fun setDataSource(dataSource: ArrayList<Media>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setDeleteMediaHandler(mediaHandler: MediaHandler) {
        this.mediaHandler = mediaHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderImage {
        val view: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_media_edit_my_post, null)

        val width = parent.measuredWidth/3
        val height = parent.measuredHeight

        view.layoutParams = RecyclerView.LayoutParams(width, height)

        return ViewHolderImage(view)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    /**
     * Set media
     * Type: Video = 0, image = 1
     */
    override fun onBindViewHolder(holder: ViewHolderImage, position: Int) {
        val data = dataSource[position]
        if (data.type == 0){
            holder.imgPlay.visibility = View.VISIBLE
        }else{
            holder.imgPlay.visibility = View.GONE
        }

        if (data.typePath == 1){
            getImage(holder.imgMedia, data.original)
        }else{
            getImage(holder.imgMedia, getLinkDataNode(data.original?:""))

        }

        holder.itemView.setOnClickListener {
            mediaHandler!!.onMedia(dataSource, position)
        }

        holder.imgClose.setOnClickListener {
            mediaHandler!!.onRemoveMedia(position, dataSource[position].fileName)
        }
    }

    class ViewHolderImage(view: View) : RecyclerView.ViewHolder(view) {
        val imgMedia: ImageView = view.imgMedia
        val imgClose: ImageView = view.imgClose
        val imgPlay: ImageView = view.imgPlay
    }

    private fun getLinkDataNode(url: String): String {
        return String.format(
            "%s%s",
            CurrentConfigNodeJs.getConfigNodeJs(context).api_ads,
            url
        )
    }
    private fun getImage(imageView: ImageView, string: String?) {
        Glide.with(context)
            .load(string)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .apply(
                RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
            )
            .into(imageView)
    }
}

