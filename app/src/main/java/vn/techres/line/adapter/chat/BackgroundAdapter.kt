package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.R
import vn.techres.line.data.model.chat.Background
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemBackgroundBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.SelectBackgroundHandler

class BackgroundAdapter(var context: Context) :
    RecyclerView.Adapter<BackgroundAdapter.ViewHolder>() {
    private var backgroundList = ArrayList<Background>()
    private var selectBackgroundHandler: SelectBackgroundHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var positionOld : Int? = null

    fun setSelectBackgroundHandler(selectBackgroundHandler: SelectBackgroundHandler?) {
        this.selectBackgroundHandler = selectBackgroundHandler
    }

    fun setDataSource(backgroundList: ArrayList<Background>) {
        this.backgroundList = backgroundList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBackgroundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return backgroundList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val background = backgroundList[position]
        holder.bind(context, configNodeJs, background)
        holder.itemView.setOnClickListener {
            if(background.is_Check == true){
                background.is_Check = false

            }else{
                if (positionOld != null){
                    backgroundList[positionOld ?: 0].is_Check = false
                    notifyItemChanged(positionOld ?: 0)
                }
                background.is_Check = true
                selectBackgroundHandler?.onSelectBackground(background)
            }
            notifyItemChanged(position)
            positionOld = position
        }
    }

    class ViewHolder(private val binding: ItemBackgroundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            context: Context,
            configNodeJs: ConfigNodeJs,
            background: Background,
        ) {
            Glide.with(binding.imgBackground)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        background.link_original
                    )
                )
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgBackground)
            if (background.is_Check == true) {
                binding.lnSelect.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.main_bg
                    )
                )
            } else {
                binding.lnSelect.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }
}