package vn.techres.line.adapter.album

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.album.ClickFolderAlbum
import vn.techres.line.data.model.album.FolderAlbum
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemFolderAlbumBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser

 class FolderAlbumAdapter(var context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataSource = ArrayList<FolderAlbum>()
    private var configNodeJs = context?.let { CurrentConfigNodeJs.getConfigNodeJs(it) }
    private var user = context?.let { CurrentUser.getCurrentUser(it) }
    private var clickFolderAlbum : ClickFolderAlbum? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource : ArrayList<FolderAlbum>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    fun setClickFolder(clickFolderAlbum: ClickFolderAlbum) {
        this.clickFolderAlbum = clickFolderAlbum
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FolderAlbumHolder(ItemFolderAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        context?.let { configNodeJs?.let { it1 ->
            clickFolderAlbum?.let { it2 ->
                (holder as FolderAlbumHolder).bind(it,
                    it1, dataSource, position, it2
                )
            }
        } }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class FolderAlbumHolder(var binding : ItemFolderAlbumBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(
            context : Context,
            configNodeJs: ConfigNodeJs,
            dataSource : ArrayList<FolderAlbum>,
            position : Int,
            clickFolderAlbum : ClickFolderAlbum
            ){
            if(dataSource[position].is_share == 0)
                binding.share.visibility = View.GONE
            else
                binding.share.visibility = View.VISIBLE
            binding.folderName.text = dataSource[position].folder_name
            itemView.setOnClickListener {
                clickFolderAlbum.clickFolderAlbum(dataSource[position])
            }
            itemView.setOnLongClickListener {
                clickFolderAlbum.longClickFolderAlbum(dataSource[position])
                return@setOnLongClickListener true
            }
        }
    }


    companion object{
        const val CAMERA = 0
        const val MEDIA = 1
    }
}