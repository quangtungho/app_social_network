package vn.techres.line.holder.newfeed

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R

class FriendSuggestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val lnFriendSuggest: LinearLayout = view.findViewById(R.id.lnFriendSuggest)
    val tvSeeMoreFriendSuggest: TextView = view.findViewById(R.id.tvSeeMoreFriendSuggest)
    val recyclerViewFriendSuggest: RecyclerView = view.findViewById(R.id.recyclerViewFriendSuggest)
}