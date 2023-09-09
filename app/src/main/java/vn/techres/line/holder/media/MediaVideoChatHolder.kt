package vn.techres.line.holder.media

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_section_media_video_chat.view.*

class MediaVideoChatHolder(view : View) : RecyclerView.ViewHolder(view) {
    var imgMedia: ImageView = view.imgMedia
    var rltVideo: RelativeLayout = view.rltVideo
}