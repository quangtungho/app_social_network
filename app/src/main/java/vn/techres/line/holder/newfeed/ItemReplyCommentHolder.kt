package vn.techres.line.holder.newfeed

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemReplyCommentBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.comment.ReplyCommentHandler

class ItemReplyCommentHolder(val binding: ItemReplyCommentBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bin(
        dataSource: Comment,
        context: Context,
        positionComment: Int,
        idCommentParent: String,
        user: User,
        configNodeJs: ConfigNodeJs,
        replyCommentHandler: ReplyCommentHandler
    ) {
        binding.lnReplyComment.visibility = View.GONE

        Utils.getImage(
            binding.imgAvatar,
            dataSource.customer_avatar.thumb ?: "",
            configNodeJs
        )
        Utils.getImage(
            binding.imgAvatarReply,
            dataSource.customer_avatar.thumb ?: "",
            configNodeJs
        )
        binding.txtName.text = dataSource.customer_name
        binding.txtTime.text = TimeFormatHelper.timeAgoString(dataSource.created_at)

        if (dataSource.content!!.isEmpty()) {
            binding.txtContent.visibility = View.GONE
        } else {
            binding.txtContent.text = dataSource.content
        }

        //Check sticker or image
        if (dataSource.sticker!!.isEmpty() && dataSource.image_urls.isNotEmpty()) {
            binding.cvMedia.visibility = View.VISIBLE
            binding.imgSticker.visibility = View.GONE
            Utils.getGlide(binding.imgMedia, dataSource.image_urls[0].original, configNodeJs)
        } else if (dataSource.sticker!!.isNotEmpty() && dataSource.image_urls.isEmpty()) {
            binding.imgSticker.visibility = View.VISIBLE
            binding.cvMedia.visibility = View.GONE
            Utils.getGlide(binding.imgSticker, dataSource.sticker, configNodeJs)
        } else {
            binding.cvMedia.visibility = View.GONE
            binding.imgSticker.visibility = View.GONE
        }

        //Reaction comment
        if (dataSource.customer_like_ids.size == 0) {
            binding.lnReactionCommentReply.visibility = View.GONE
        } else {
            binding.lnReactionCommentReply.visibility = View.VISIBLE
        }
        binding.txtReactionCommentReply.text = dataSource.customer_like_ids.size.toString()
        if (dataSource.my_reaction_id == 1) {
            binding.txtFavorite.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            binding.txtFavorite.setTextColor(ContextCompat.getColor(context, R.color.text_color))
        }

        binding.lnComment.setOnLongClickListener {
            replyCommentHandler.onLongClickReplyComment(
                positionComment,
                position,
                dataSource,
                TechresEnum.TYPE_REPLY_COMMENT.toString()
            )
            true
        }

        binding.txtReply.setOnClickListener {
            replyCommentHandler.replyComment(
                positionComment,
                position,
                dataSource,
                TechresEnum.TYPE_REPLY_COMMENT.toString(),
                idCommentParent
            )
        }

        binding.txtName.setOnClickListener {
            dataSource.customer_id?.let { it1 ->
                replyCommentHandler.clickProfile(
                    position,
                    it1
                )
            }
        }

        binding.imgMedia.setOnClickListener {
            replyCommentHandler.showImage(position, dataSource.image_urls[0].original ?: "")
        }

        binding.imgAvatar.setOnClickListener {
            replyCommentHandler.showImage(position, dataSource.customer_avatar.original ?: "")
        }

        binding.txtFavorite.setOnClickListener {
            if (dataSource.my_reaction_id == 1) {
                dataSource.my_reaction_id = 0
                dataSource.customer_like_ids.removeIf { it == user.id }
                if (dataSource.customer_like_ids.size == 0) {
                    binding.lnReactionCommentReply.visibility = View.GONE
                } else {
                    binding.lnReactionCommentReply.visibility = View.VISIBLE
                }
                binding.txtReactionCommentReply.text = dataSource.customer_like_ids.size.toString()
                binding.txtFavorite.setTextColor(ContextCompat.getColor(context, R.color.text_gray))
            } else {
                dataSource.my_reaction_id = 1
                dataSource.customer_like_ids.add(user.id)
                binding.lnReactionCommentReply.visibility = View.VISIBLE
                binding.txtReactionCommentReply.text = dataSource.customer_like_ids.size.toString()
                binding.txtFavorite.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            replyCommentHandler.onReactionComment(dataSource.comment_id?:"")
        }

        binding.lnReactionCommentReply.setOnClickListener {
            replyCommentHandler.onDetailReactionComment(dataSource.customer_like_ids)
        }

    }
}