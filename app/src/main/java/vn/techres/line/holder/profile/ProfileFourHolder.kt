package vn.techres.line.holder.profile

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R

class ProfileFourHolder(view: View) : RecyclerView.ViewHolder(view) {
    //Media
    val btnImage: Button = view.findViewById(R.id.btnImage)
    val btnVideo: Button = view.findViewById(R.id.btnVideo)
    val btnAlbum: Button = view.findViewById(R.id.btnAlbum)
}