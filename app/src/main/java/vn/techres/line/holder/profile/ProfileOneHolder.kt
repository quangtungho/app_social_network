package vn.techres.line.holder.profile

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R

class ProfileOneHolder(view: View) : RecyclerView.ViewHolder(view) {
    //Header profile
    val imgProfile: ImageView = view.findViewById(R.id.imgProfile)
    val imgCoverPhoto: ImageView = view.findViewById(R.id.imgCoverPhoto)
    val txtFullName: TextView = view.findViewById(R.id.txtFullName)

    //Buttons
    val lnCreatePostProfile: LinearLayout = view.findViewById(R.id.lnCreatePostProfile)
    val lnProfileFriendly: LinearLayout = view.findViewById(R.id.lnProfileFriendly)
    val lnAddFriendProfile: LinearLayout = view.findViewById(R.id.lnAddFriendProfile)
    val lnCancelRequestFriendProfile: LinearLayout = view.findViewById(R.id.lnCancelRequestFriendProfile)
    val lnReplyFriendProfile: LinearLayout = view.findViewById(R.id.lnReplyFriendProfile)
    val lnChatProfile: LinearLayout = view.findViewById(R.id.lnChatProfile)
    val imgProfileMore: ImageView = view.findViewById(R.id.imgProfileMore)
}