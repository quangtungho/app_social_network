package vn.techres.line.holder.message

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemMessageViewerBinding
import vn.techres.line.helper.utils.loadImage

class MessageViewerHolder(private val binding: ItemMessageViewerBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(dataSource : ArrayList<MessageViewer>, position : Int, configNodeJs: ConfigNodeJs){
        binding.imgAvatarViewer.loadImage(String.format("%s%s", configNodeJs.api_ads, dataSource[position].avatar?.medium), R.drawable.ic_user_placeholder_circle, true)
        binding.tvCountViewer.apply {
            if(dataSource.size > 7){
                text = (dataSource.size - 7).toString()
                visibility = View.VISIBLE
            }else{
                visibility = View.GONE
            }
        }
    }
}