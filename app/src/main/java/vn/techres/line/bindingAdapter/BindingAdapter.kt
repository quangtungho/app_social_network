package vn.techres.line.bindingAdapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import vn.techres.line.helper.CurrentConfigNodeJs

@BindingAdapter(
    "glideUrl",
    "glideCenterCrop",
    "glideCircularCrop",
    requireAll = false
)
fun ImageView.bindGlideSrc(
    url : String?,
    centerCrop: Boolean = false,
    circularCrop: Boolean = false
) {
    if (url == null || url.isEmpty()) return

    createGlideRequest(
        context,
        url,
        centerCrop,
        circularCrop
    ).into(this)
}

private fun createGlideRequest(
    context: Context,
    url: String,
    centerCrop: Boolean,
    circularCrop: Boolean
): RequestBuilder<Drawable> {
    val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    val req = Glide.with(context).load(String.format("%s%s", configNodeJs.api_ads ?: "", url))
    if (centerCrop) req.centerCrop()
    if (circularCrop) req.circleCrop()
    return req
}

@BindingAdapter("goneIf")
fun View.bindGoneIf(gone: Boolean) {
    visibility = if (gone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

@BindingAdapter("text")
fun TextView.text(string: String?){
    text = string ?: ""
}