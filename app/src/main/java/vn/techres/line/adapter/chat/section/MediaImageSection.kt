package vn.techres.line.adapter.chat.section

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import vn.techres.line.R
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemSectionMediaImageChatBinding
import vn.techres.line.holder.media.HeaderMediaChatHolder
import vn.techres.line.holder.media.MediaImageChatHolder
import vn.techres.line.interfaces.chat.MediaHandler

class MediaImageSection(
    var configNodeJs: ConfigNodeJs,
    var title: String?,
    var dataSource: ArrayList<FileNodeJs>,
    private val mediaHandler: MediaHandler?,
    private val context:Context,
    val view : View
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_section_media_image_chat)
        .headerResourceId(R.layout.item_header_media_chat)
        .build()
) {
    var randomkey = ""
    var id: Int = 0
    override fun getContentItemsTotal(): Int {
        return dataSource.size
    }

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return HeaderMediaChatHolder(view!!)
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        view as ViewGroup
        return MediaImageChatHolder(
            ItemSectionMediaImageChatBinding.inflate(
                LayoutInflater.from(context),
                view,
                false
            )
        )
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as HeaderMediaChatHolder).tvDateMedia.text = title ?: ""
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as MediaImageChatHolder).bind(
            configNodeJs,
            dataSource,
            position,
            mediaHandler!!,
            view,
            context
        )
    }
}