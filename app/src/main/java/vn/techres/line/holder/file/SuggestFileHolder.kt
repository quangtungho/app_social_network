package vn.techres.line.holder.file

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemSuggestFileBinding
import vn.techres.line.helper.Utils.getMediaGlide
import vn.techres.line.interfaces.util.SuggestFileHandler

class SuggestFileHolder(private val binding : ItemSuggestFileBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(url: String, suggestHandler : SuggestFileHandler){
        getMediaGlide(binding.imgFile, url)
//        ChatUtils.setLayoutParams(
//            binding.imgFile,
//            Utils.getBitmapRotationImage(File(url))?.height ?: 0,
//            Utils.getBitmapRotationImage(File(url))?.width ?: 0
//        )
        binding.imgClose.setOnClickListener {
            suggestHandler.onClose(url)
        }
    }
}