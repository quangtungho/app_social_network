package vn.techres.line.adapter.chat.section

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import vn.techres.line.R
import vn.techres.line.holder.media.MediaFileChatHolder
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemSectionMediaFileChatBinding
import vn.techres.line.holder.media.HeaderMediaChatHolder
import vn.techres.line.interfaces.chat.MediaHandler

class MediaFileSection(
    var configNodeJs: ConfigNodeJs,
    var title: String?,
    var dataSource: ArrayList<FileNodeJs>,
    private val mediaHandler: MediaHandler?,
    var context: Context
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_section_media_file_chat)
        .headerResourceId(R.layout.item_header_media_chat)
        .build()
) {
    override fun getContentItemsTotal(): Int {
        return dataSource.size
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        view as ViewGroup
        return MediaFileChatHolder(
            ItemSectionMediaFileChatBinding.inflate(
                LayoutInflater.from(context),
                view,
                false
            )
        )
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as MediaFileChatHolder).bind(
            configNodeJs,
            dataSource,
            position,
            mediaHandler!!
        )
    }

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return HeaderMediaChatHolder(view!!)
    }


    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as HeaderMediaChatHolder).tvDateMedia.text = title ?: ""
    }
}