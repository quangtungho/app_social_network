package vn.techres.line.bindingAdapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import vn.techres.line.R

@BindingAdapter("markNotification")
fun View.markNotification(isView : Int?){
    if(isView == 0){
        setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
    }else{
        setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
    }
}