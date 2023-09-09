package vn.techres.line.holder.media

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_section_media_link_chat.view.*
import vn.techres.line.widget.TechResTextView
import vn.techres.line.widget.TechResTextViewBold

class MediaLinkChatHolder(view : View) : RecyclerView.ViewHolder(view){
    var  rlLinkSuggest: RelativeLayout = view.rlLinkSuggest
    var  imgLinkSuggest: ImageView = view.imgLinkSuggest
    var  tvAuthorLinkSuggest: TechResTextView = view.tvAuthorLinkSuggest
    var  tvTitleLinkSuggest: TechResTextViewBold = view.tvTitleLinkSuggest
    var  tvDescriptionLinkSuggest: TechResTextView = view.tvDescriptionLinkSuggest
    var  tvSendLinkSuggest: TechResTextView = view.tvSendLinkSuggest
}