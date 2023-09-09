package vn.techres.line.adapter.chat.section

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import vn.techres.line.R
import vn.techres.line.data.model.chat.LinkMessage
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.holder.media.HeaderMediaChatHolder
import vn.techres.line.holder.media.MediaLinkChatHolder
import vn.techres.line.interfaces.chat.MediaHandler

class MediaLinkSection(
    var context: Context,
    var configNodeJs: ConfigNodeJs,
    var title: String?,
    var dataSource: ArrayList<LinkMessage>,
    private val mediaHandler: MediaHandler?
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_section_media_link_chat)
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
        return MediaLinkChatHolder(view!!)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as HeaderMediaChatHolder).tvDateMedia.text = title ?: ""
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val link = dataSource[position]
        holder as MediaLinkChatHolder
        Utils.getMediaGlide(holder.imgLinkSuggest, link.media_thumb)

        if(StringUtils.isNullOrEmpty(link.author)){
            holder.tvAuthorLinkSuggest.hide()
        }else{
            holder.tvAuthorLinkSuggest.show()
            holder.tvAuthorLinkSuggest.text = link.author ?:""
        }

        if(StringUtils.isNullOrEmpty(link.title)){
            holder.tvTitleLinkSuggest.hide()
        }else{
            holder.tvTitleLinkSuggest.show()
            holder.tvTitleLinkSuggest.text = link.title ?:""
        }

        if(StringUtils.isNullOrEmpty(link.description)){
            holder.tvDescriptionLinkSuggest.hide()
        }else{
            holder.tvDescriptionLinkSuggest.show()
            holder.tvDescriptionLinkSuggest.text = link.description ?:""
        }

        holder.rlLinkSuggest.setOnClickListener {
            mediaHandler?.onReSendLink(link)
        }
        holder.tvSendLinkSuggest.setOnClickListener {
        }
    }
}