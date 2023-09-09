package vn.techres.line.adapter.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.luck.picture.lib.adapter.PictureImageGridAdapter
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.AnimUtils
import com.luck.picture.lib.tools.ValueOf
import vn.techres.line.R
import vn.techres.line.databinding.ItemCameraKeyboardBinding
import vn.techres.line.databinding.ItemMediaKeyboardBinding
import vn.techres.line.interfaces.util.MediaKeyboardHandler

class MediaKeyboardAdapter(var context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataSource = ArrayList<LocalMedia>()
    private val selectData: ArrayList<LocalMedia> = ArrayList()
    private var mediaKeyboardHandler : MediaKeyboardHandler? = null

    fun setDataSource(dataSource : ArrayList<LocalMedia>){
        this.dataSource = dataSource
    }
    fun setMediaKeyboardHandler(mediaKeyboardHandler : MediaKeyboardHandler?){
        this.mediaKeyboardHandler = mediaKeyboardHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            CAMERA ->{
                CameraHolder(ItemCameraKeyboardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            MEDIA ->{
                MediaKeyboardHolder(ItemMediaKeyboardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else ->{
                MediaKeyboardHolder(ItemMediaKeyboardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            CAMERA ->{
                (holder as CameraHolder).bind()
            }
            MEDIA ->{
                (holder as MediaKeyboardHolder).bind(dataSource[position], mediaKeyboardHandler)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class CameraHolder(var binding : ItemCameraKeyboardBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(){

        }
    }
    inner class MediaKeyboardHolder(var binding : ItemMediaKeyboardBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(localMedia: LocalMedia, mediaKeyboardHandler : MediaKeyboardHandler?){
            Glide.with(binding.imgMedia)
                .load(localMedia.path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(binding.imgMedia)
            notifyCheckChanged(binding.tvCheck, localMedia)
            binding.btnCheck.setOnClickListener {
                if(localMedia.isChecked){
                    dataSource[bindingAdapterPosition].isChecked = false
                    binding.tvCheck.isSelected = false
                    AnimUtils.disZoom(binding.imgMedia, true)
                    selectData.removeAt(selectData.indexOfFirst { it.id == localMedia.id })
                }else {
                    dataSource[bindingAdapterPosition].isChecked = true
                    mediaKeyboardHandler?.onChooseMedia(localMedia.path)
                    binding.tvCheck.isSelected = true
                    AnimUtils.zoom(binding.imgMedia, true)
                    selectData.add(localMedia)
                    binding.tvCheck.text = (selectData.size).toString()
                }
            }
            binding.root.setOnClickListener {

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0){
            CAMERA
        }else{
            MEDIA
        }
    }
    fun getSize() : Int{
        return dataSource.size
    }
    fun getData() : ArrayList<LocalMedia>{
        return dataSource
    }
    fun isDataEmpty(): Boolean {
        return dataSource.size == 0
    }

    /**
     * Update button status
     */
    private fun notifyCheckChanged(
        textView: TextView,
        imageBean: LocalMedia
    ) {
        textView.text = ""
        val size = selectData.size
        for (i in 0 until size) {
            val media = selectData[i]
            if (media.path == imageBean.path || media.id == imageBean.id) {
                imageBean.num = media.num
                media.setPosition(imageBean.getPosition())
                textView.text = ValueOf.toString(imageBean.num)
            }
        }
    }
    companion object{
        const val CAMERA = 0
        const val MEDIA = 1
    }
}