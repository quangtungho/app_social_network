package vn.techres.line.holder.newfeed

import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R

class CreateNewFeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cvCreateReview: CardView = view.findViewById(R.id.cvCreateReview)
    val cvCreatePost: CardView = view.findViewById(R.id.cvCreatePost)
}