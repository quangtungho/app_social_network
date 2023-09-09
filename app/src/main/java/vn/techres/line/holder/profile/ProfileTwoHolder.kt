package vn.techres.line.holder.profile

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R

class ProfileTwoHolder(view: View) : RecyclerView.ViewHolder(view) {
    //information
    val txtEditProfile: TextView = view.findViewById(R.id.txtEditProfile)
    val txtPhone: TextView = view.findViewById(R.id.txtPhone)
    val txtEmail: TextView = view.findViewById(R.id.txtEmail)
    val txtGender: TextView = view.findViewById(R.id.txtGender)
    val txtGender1: TextView = view.findViewById(R.id.txtGender1)
    val txtBirthday: TextView = view.findViewById(R.id.txtBirthday)
    val txtAddress: TextView = view.findViewById(R.id.txtAddress)

    val imageView6: ImageView = view.findViewById(R.id.imageView6)
    val imageView8: ImageView = view.findViewById(R.id.imageView8)
    val imageView9: ImageView = view.findViewById(R.id.imageView9)
    val txtEmail1: TextView = view.findViewById(R.id.txtEmail1)
    val txtAddress1: TextView = view.findViewById(R.id.txtAddress1)
    val txtReportUser: TextView = view.findViewById(R.id.txtReportUser)
}