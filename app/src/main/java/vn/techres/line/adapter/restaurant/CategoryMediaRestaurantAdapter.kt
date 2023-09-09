package vn.techres.line.adapter.restaurant

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.CategoryMediaRestaurant
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemFolderAlbumBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.interfaces.restaurant.CategoryMediaRestaurantHandler

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
class CategoryMediaRestaurantAdapter(var context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataSource = ArrayList<CategoryMediaRestaurant>()
    private var configNodeJs = context?.let { CurrentConfigNodeJs.getConfigNodeJs(it) }
    private var user = context?.let { CurrentUser.getCurrentUser(it) }
    private var categoryMediaRestaurantHandler : CategoryMediaRestaurantHandler? = null
    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource : ArrayList<CategoryMediaRestaurant>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    fun setClickCategoryMediaRestaurant(categoryMediaRestaurantHandler: CategoryMediaRestaurantHandler) {
        this.categoryMediaRestaurantHandler = categoryMediaRestaurantHandler
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FolderAlbumHolder(ItemFolderAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        context?.let { configNodeJs?.let { it1 ->
            categoryMediaRestaurantHandler?.let { it2 ->
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
            dataSource : ArrayList<CategoryMediaRestaurant>,
            position : Int,
            categoryMediaRestaurantHandler : CategoryMediaRestaurantHandler
        ){
            binding.folderName.text = dataSource[position].folder_name
            itemView.setOnClickListener {
                categoryMediaRestaurantHandler.clickCategoryMediaRestaurant(dataSource[position])
            }
        }
    }


    companion object{
        const val CAMERA = 0
        const val MEDIA = 1
    }
}