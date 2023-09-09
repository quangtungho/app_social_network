package vn.techres.line.bindingAdapter

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import vn.techres.line.R
import vn.techres.line.data.model.chat.Group
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.techresenum.TechResEnumChat

@BindingAdapter(
    "avatarGroup",
    "avatarPersonal",
    "conversationType"
)
fun ImageView.avatarGroup(
    avatarGroup: String?,
    avatarPersonal: String?,
    conversationType: String
) {
    if (conversationType == TechResEnumChat.PERSONAL.toString()) {
        createGlideRequest(context, avatarPersonal ?: "")
    } else {
        createGlideRequest(context, avatarGroup ?: "")
    }
}

private fun createGlideRequest(
    context: Context,
    url: String,
): RequestBuilder<Drawable> {
    val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    return Glide
        .with(context)
        .load(String.format("%s%s", configNodeJs.api_ads, url))
        .circleCrop()
        .placeholder(R.drawable.ic_user_placeholder)
        .error(R.drawable.ic_user_placeholder)
}

@BindingAdapter(
    "nameGroup"
)
fun TextView.nameGroup(
    group: Group
) {
   if((group.last_message_type ?: "").isNotEmpty()){
        text = if (group.conversation_type == TechResEnumChat.PERSONAL.toString()) {
            group.member.full_name
        } else {
            group.name
        }
        if(group.member.number ?: 0 > 0) {
            typeface = Typeface.DEFAULT_BOLD
        }else{
            typeface = Typeface.DEFAULT
        }
    }else{
       typeface = Typeface.DEFAULT
    }
}

@BindingAdapter(
    "countChat"
)
fun TextView.countChat(number: Int?) {
    visibility = View.GONE
    if (number != null) {
        if (number > 0) {
            text = if (number <= 99) {
                number.toString()
            } else {
                context.resources.getString(R.string.limit_count_chat)
            }
            visibility = View.VISIBLE
        }
    }
}

@BindingAdapter(
    "timeLastMessage",
    "lastMessageType"
)
fun TextView.timeLastMessage(timeLastMessage: String?, lastMessageType: String?) {
    text = if ((lastMessageType ?: "").isNotEmpty()) {
        timeLastMessage ?: ""
    } else {
        ""
    }
}