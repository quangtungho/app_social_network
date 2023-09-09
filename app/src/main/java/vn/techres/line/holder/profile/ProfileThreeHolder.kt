package vn.techres.line.holder.profile

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R

class ProfileThreeHolder(view: View) : RecyclerView.ViewHolder(view) {
    //Friend
    val txtFriendNumber: TextView = view.findViewById(R.id.txtFriendNumber)
    val txtSearchFriend: TextView = view.findViewById(R.id.txtSearchFriend)
    val btnViewAll: Button = view.findViewById(R.id.btnViewAll)
    val recyclerViewFriends: RecyclerView = view.findViewById(R.id.recyclerViewFriends)
}