package vn.techres.line.adapter.account

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import vn.techres.line.R
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemLoadImageChatBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.chat.ImageMoreChatHandler

class LoadImageChatAdapter(var context: Context) :
    RecyclerView.Adapter<LoadImageChatAdapter.ViewHolder>() {
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var imageList = ArrayList<FileNodeJs>()
    private var imageMoreChatHandler: ImageMoreChatHandler? = null
    private var messagesByGroup = MessagesByGroup()
    private var isLocal = 0

    fun setImageMoreChatHandler(imageMoreChatHandler: ImageMoreChatHandler?) {
        this.imageMoreChatHandler = imageMoreChatHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(
        imageList: ArrayList<FileNodeJs>,
        isLocal: Int,
        messagesByGroup: MessagesByGroup
    ) {
        this.imageList = imageList
        this.isLocal = isLocal
        this.messagesByGroup = messagesByGroup
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLoadImageChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(configNodeJs, imageList[position], imageMoreChatHandler)
    }

    inner class ViewHolder(val binding: ItemLoadImageChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            configNodeJs: ConfigNodeJs,
            file: FileNodeJs,
            imageMoreChatHandler: ImageMoreChatHandler?
        ) {

            if (isLocal == 1) {
                Glide.with(binding.imgChat)
                    .load(
                        file.link_original ?: ""
                    )
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .placeholder(R.drawable.logo_aloline_placeholder)
                    .error(R.drawable.logo_aloline_placeholder)
                    .into(binding.imgChat)
//                Utils.resizeImage(context, imageList[0].link_original ?: "", binding.imgChat)
            } else {
                Glide.with(binding.imgChat)
                    .load(
                        String.format(
                            "%s%s",
                            CurrentConfigNodeJs.getConfigNodeJs(context).api_ads,
                            file.link_original
                        )
                    )
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .placeholder(R.drawable.logo_aloline_placeholder)
                    .error(R.drawable.logo_aloline_placeholder)
                    .into(binding.imgChat)

            }
            val lp: ViewGroup.LayoutParams = binding.imgChat.getLayoutParams()
            if (lp is FlexboxLayoutManager.LayoutParams) {
                val flexboxLp = lp
                flexboxLp.flexGrow = 1.0f
                flexboxLp.alignSelf = AlignItems.FLEX_END
            }
            binding.root.setOnClickListener {
                imageMoreChatHandler?.onCLickImageMore(imageList, bindingAdapterPosition)
            }
            binding.root.setOnLongClickListener {
                imageMoreChatHandler?.onRevokeImageMore(messagesByGroup, it)
                true
            }
        }
    }
}


