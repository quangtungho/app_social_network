package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.data.model.chat.Sticker
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemStickerSuggestBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.keyboard.UtilitiesChatHandler

class StickerSuggestAdapter(var context : Context,var dataSource : ArrayList<Sticker>) : RecyclerView.Adapter<StickerSuggestAdapter.ViewHolder>() {

    private val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var utilitiesChatHandler: UtilitiesChatHandler? = null

    fun setUtilitiesChatHandler(utilitiesChatHandler: UtilitiesChatHandler?){
        this.utilitiesChatHandler = utilitiesChatHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemStickerSuggestBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(configNodeJs, dataSource[position], utilitiesChatHandler)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class ViewHolder(private val binding : ItemStickerSuggestBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(configNodeJs: ConfigNodeJs, sticker: Sticker, utilitiesChatHandler: UtilitiesChatHandler?){
            Glide.with(binding.imgSticker)
                .load(String.format("%s%s", configNodeJs.api_ads, sticker.link_original))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imgSticker)
            binding.imgSticker.setOnClickListener {
                utilitiesChatHandler?.onChooseSticker(sticker)
            }
        }
    }
}