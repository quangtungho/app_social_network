package vn.techres.line.adapter.chat.section

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import vn.techres.line.R
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.holder.media.HeaderMediaChatHolder
import vn.techres.line.holder.media.MediaVideoChatHolder
import vn.techres.line.interfaces.chat.MediaHandler

class MediaVideoSection(
    var configNodeJs: ConfigNodeJs,
    var title: String?,
    var dataSource: ArrayList<FileNodeJs>,
    private val mediaHandler: MediaHandler?,
    private val context:Context,
    val view:View
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_section_media_video_chat)
        .headerResourceId(R.layout.item_header_media_chat)
        .build()
) {
    override fun getContentItemsTotal(): Int {
        return dataSource.size
    }
    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return HeaderMediaChatHolder(view!!)
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        return MediaVideoChatHolder(view!!)
    }
    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as HeaderMediaChatHolder).tvDateMedia.text = title ?: ""
    }
    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        view.post {
            (holder as MediaVideoChatHolder).imgMedia.layoutParams.width = view.width / 3
            holder.imgMedia.layoutParams.height = view.width / 3
            holder.rltVideo.layoutParams.height = view.width / 3
            holder.rltVideo.layoutParams.width = view.width / 3
        }
//        Utils.getGlide((holder as MediaVideoChatHolder).imgMedia, dataSource[position].link_original, configNodeJs)
        val linkOriginal =
            FileUtils.getInternalStogeDir(dataSource[position].name_file ?: "", context) ?: ""
        Utils.getMediaGlide(
            (holder as MediaVideoChatHolder).imgMedia,
            linkOriginal
        )
        holder.itemView.setOnClickListener {
            mediaHandler?.onClickMedia(dataSource, position)
        }
    }
}