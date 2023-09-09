package vn.techres.line.adapter.librarydevice

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_imgae_device.view.*
import kotlinx.android.synthetic.main.item_video_device.view.*
import vn.techres.line.R
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.librarydevice.LibraryDeviceHandler
import vn.techres.line.data.model.librarydevice.FileDevice
import java.io.File

class LibraryDeviceAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var dataSource = ArrayList<FileDevice>()
    private var libraryDeviceHandler : LibraryDeviceHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private val header = 0
    private val video = 2
    private val image = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == header){
            val view =  LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_camera_device, parent, false)
            HeaderHolder(view!!)
        }else{
            if(viewType == video){
                val view =  LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video_device, parent, false)
                VideoHolder(view!!)
            }else{
                val view =  LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_imgae_device, parent, false)
                ImageHolder(view!!)
            }

        }
    }

    fun setDataSource(dataSource : ArrayList<FileDevice>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setLibraryDeviceHandler(libraryDeviceHandler : LibraryDeviceHandler){
        this.libraryDeviceHandler = libraryDeviceHandler
    }

    private fun getFile(string : String, imageView: ImageView){
        Glide.with(context)
            .load(File(string))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

            .apply(
                RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
            )
            .into(imageView)
    }

    private fun onBinHeader(holder: HeaderHolder){
        holder.itemView.setOnClickListener {
            libraryDeviceHandler!!.onChooseCamera()
        }
    }

    private fun onBinVideo(holder: VideoHolder, position: Int){
        getFile(dataSource[position].path!!, holder.imgVideoDevice)
        holder.itemView.setOnClickListener {
            libraryDeviceHandler!!.onChooseFile(dataSource[position])
        }
    }

    private fun onBinImage(holder: ImageHolder, position: Int){
        getFile(dataSource[position].path!!, holder.imgDevice)
        holder.itemView.setOnClickListener {
            libraryDeviceHandler!!.onChooseFile(dataSource[position])
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            header -> onBinHeader((holder as HeaderHolder))
            video -> onBinVideo((holder as VideoHolder), position)
            image -> onBinImage((holder as ImageHolder), position)
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0){
            header
        }else{
            if(dataSource[position].path!!.contains("mp4")){
                video
            }else{
                image
            }
        }
    }
    class HeaderHolder(view : View) : RecyclerView.ViewHolder(view){

    }
    class VideoHolder(view : View) : RecyclerView.ViewHolder(view){
        var imgVideoDevice = view.imgVideoDevice
    }
    class ImageHolder(view : View) : RecyclerView.ViewHolder(view){
        var imgDevice = view.imgDevice
    }
}