package vn.techres.line.holder.newfeed

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import vn.techres.line.R
import vn.techres.line.adapter.comment.ReplyCommentAdapter
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemCommentBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.comment.CommentHandler
import vn.techres.line.interfaces.comment.ReplyCommentHandler

class ItemCommentHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bin(
        dataSource: Comment,
        position: Int,
        context: Context,
        user: User,
        configNodeJs: ConfigNodeJs,
        replyCommentAdapter: ReplyCommentAdapter,
        commentHandler: CommentHandler,
        replyCommentHandler: ReplyCommentHandler
    ) {

        Utils.getImage(
            binding.imgAvatar,
            dataSource.customer_avatar.thumb ?: "",
            configNodeJs
        )
        Utils.getImage(binding.imgAvatarReply, user.avatar_three_image.thumb, configNodeJs)
        binding.txtName.text = dataSource.customer_name
        binding.txtTime.text = TimeFormatHelper.timeAgoString(dataSource.created_at)

        if (dataSource.content!!.isEmpty()) {
            binding.txtContent.visibility = View.GONE
        } else {
            binding.txtContent.visibility = View.VISIBLE
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
            binding.lnReactionComment.visibility = View.GONE
        } else {
            binding.lnReactionComment.visibility = View.VISIBLE
        }
        binding.txtReactionComment.text = dataSource.customer_like_ids.size.toString()
        if (dataSource.my_reaction_id == 1) {
            binding.txtFavorite.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            binding.txtFavorite.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.text_color
                )
            )
        }

        //Recyclerview reply
        replyCommentAdapter.setEventReplyComment(replyCommentHandler)
        val layoutManager = FlexboxLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        replyCommentAdapter.setDataSource(dataSource.reply_comment, position, dataSource.comment_id ?: "")
        binding.recyclerView.adapter = replyCommentAdapter

        //Comment hide
        if (dataSource.reply_comment.size == 0){
            binding.viewLineLevelCommentMain.visibility = View.GONE
            binding.lnLevelCommentMain.visibility = View.GONE
        }else{
            binding.viewLineLevelCommentMain.visibility = View.VISIBLE
            binding.lnLevelCommentMain.visibility = View.VISIBLE
        }

        if (dataSource.not_answered == true){
            binding.lnThreeComment.visibility = View.VISIBLE
            binding.lnListReply.visibility = View.GONE

            when (dataSource.reply_comment.size) {
                0 -> {
                    binding.lnThreeComment.visibility = View.GONE
                    binding.txtSeeMoreReply.visibility = View.GONE
                }
                1 -> {
                    binding.lnThreeComment.visibility = View.VISIBLE
                    binding.txtSeeMoreReply.visibility = View.GONE
                    binding.lnCommentReplyFirst.visibility = View.VISIBLE
                    binding.lnCommentReplySecond.visibility = View.GONE
                    binding.lnCommentReplyThird.visibility = View.GONE

                    binding.lineReplyFirst.visibility = View.GONE
                    binding.lineRadiusReplyFirst.visibility = View.VISIBLE
                    binding.lineReplySecond.visibility = View.GONE
                    binding.lineRadiusReplySecond.visibility = View.GONE
                    binding.lineReplyThird.visibility = View.GONE
                    binding.lineRadiusReplyThird.visibility = View.GONE
                    binding.lineRadiusReplySeeMore.visibility = View.GONE


                    Utils.getImage(
                        binding.imgAvatarReplyFirst,
                        dataSource.reply_comment[0].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplyFirst.text = dataSource.reply_comment[0].customer_name
                    binding.txtContentReplyFirst.text = dataSource.reply_comment[0].content
                }
                2 -> {
                    binding.lnThreeComment.visibility = View.VISIBLE
                    binding.txtSeeMoreReply.visibility = View.GONE
                    binding.lnCommentReplyFirst.visibility = View.VISIBLE
                    binding.lnCommentReplySecond.visibility = View.VISIBLE
                    binding.lnCommentReplyThird.visibility = View.GONE

                    binding.lineReplyFirst.visibility = View.VISIBLE
                    binding.lineRadiusReplyFirst.visibility = View.VISIBLE
                    binding.lineReplySecond.visibility = View.GONE
                    binding.lineRadiusReplySecond.visibility = View.VISIBLE
                    binding.lineReplyThird.visibility = View.GONE
                    binding.lineRadiusReplyThird.visibility = View.GONE
                    binding.lineRadiusReplySeeMore.visibility = View.GONE

                    Utils.getImage(
                        binding.imgAvatarReplyFirst,
                        dataSource.reply_comment[0].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplyFirst.text = dataSource.reply_comment[0].customer_name
                    binding.txtContentReplyFirst.text = dataSource.reply_comment[0].content
                    Utils.getImage(
                        binding.imgAvatarReplySecond,
                        dataSource.reply_comment[1].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplySecond.text = dataSource.reply_comment[1].customer_name
                    binding.txtContentReplySecond.text = dataSource.reply_comment[1].content
                }
                3 -> {
                    binding.lnThreeComment.visibility = View.VISIBLE
                    binding.txtSeeMoreReply.visibility = View.GONE
                    binding.lnCommentReplyFirst.visibility = View.VISIBLE
                    binding.lnCommentReplySecond.visibility = View.VISIBLE
                    binding.lnCommentReplyThird.visibility = View.VISIBLE

                    binding.lineReplyFirst.visibility = View.VISIBLE
                    binding.lineRadiusReplyFirst.visibility = View.VISIBLE
                    binding.lineReplySecond.visibility = View.VISIBLE
                    binding.lineRadiusReplySecond.visibility = View.VISIBLE
                    binding.lineReplyThird.visibility = View.GONE
                    binding.lineRadiusReplyThird.visibility = View.VISIBLE
                    binding.lineRadiusReplySeeMore.visibility = View.GONE

                    Utils.getImage(
                        binding.imgAvatarReplyFirst,
                        dataSource.reply_comment[0].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplyFirst.text = dataSource.reply_comment[0].customer_name
                    binding.txtContentReplyFirst.text = dataSource.reply_comment[0].content
                    Utils.getImage(
                        binding.imgAvatarReplySecond,
                        dataSource.reply_comment[1].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplySecond.text = dataSource.reply_comment[1].customer_name
                    binding.txtContentReplySecond.text = dataSource.reply_comment[1].content
                    Utils.getImage(
                        binding.imgAvatarReplyThird,
                        dataSource.reply_comment[2].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplyThird.text = dataSource.reply_comment[2].customer_name
                    binding.txtContentReplyThird.text = dataSource.reply_comment[2].content
                }
                else -> {
                    binding.lnThreeComment.visibility = View.VISIBLE
                    binding.txtSeeMoreReply.visibility = View.VISIBLE
                    binding.lnCommentReplyFirst.visibility = View.VISIBLE
                    binding.lnCommentReplySecond.visibility = View.VISIBLE
                    binding.lnCommentReplyThird.visibility = View.VISIBLE

                    binding.lineReplyFirst.visibility = View.VISIBLE
                    binding.lineRadiusReplyFirst.visibility = View.VISIBLE
                    binding.lineReplySecond.visibility = View.VISIBLE
                    binding.lineRadiusReplySecond.visibility = View.VISIBLE
                    binding.lineReplyThird.visibility = View.VISIBLE
                    binding.lineRadiusReplyThird.visibility = View.VISIBLE
                    binding.lineRadiusReplySeeMore.visibility = View.VISIBLE

                    Utils.getImage(
                        binding.imgAvatarReplyFirst,
                        dataSource.reply_comment[0].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplyFirst.text = dataSource.reply_comment[0].customer_name
                    binding.txtContentReplyFirst.text = dataSource.reply_comment[0].content
                    Utils.getImage(
                        binding.imgAvatarReplySecond,
                        dataSource.reply_comment[1].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplySecond.text = dataSource.reply_comment[1].customer_name
                    binding.txtContentReplySecond.text = dataSource.reply_comment[1].content
                    Utils.getImage(
                        binding.imgAvatarReplyThird,
                        dataSource.reply_comment[2].customer_avatar.thumb ?: "",
                        configNodeJs
                    )
                    binding.txtNameReplyThird.text = dataSource.reply_comment[2].customer_name
                    binding.txtContentReplyThird.text = dataSource.reply_comment[2].content

                    binding.txtSeeMoreReply.text = String.format(
                        "%s %s %s",
                        "Xem",
                        dataSource.reply_comment.size - 3,
                        "phản hồi khác..."
                    )
                }
            }
        }else{
            if (dataSource.reply_comment.size != 0){
                binding.lnListReply.visibility = View.VISIBLE
                binding.lnThreeComment.visibility = View.GONE
                binding.lnLevelCommentMain.visibility = View.GONE
            }
        }

        binding.lnThreeComment.setOnClickListener {
            binding.lnListReply.visibility = View.VISIBLE
            binding.lnThreeComment.visibility = View.GONE
            binding.lnLevelCommentMain.visibility = View.GONE
        }

        binding.txtFavorite.setOnClickListener {
            if (dataSource.my_reaction_id == 1) {
                dataSource.my_reaction_id = 0
                dataSource.customer_like_ids.removeIf { it == user.id }
                if (dataSource.customer_like_ids.size == 0) {
                    binding.lnReactionComment.visibility = View.GONE
                } else {
                    binding.lnReactionComment.visibility = View.VISIBLE
                }
                binding.txtReactionComment.text =
                    dataSource.customer_like_ids.size.toString()
                binding.txtFavorite.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.text_gray
                    )
                )
            } else {
                dataSource.my_reaction_id = 1
                dataSource.customer_like_ids.add(user.id)
                binding.lnReactionComment.visibility = View.VISIBLE
                binding.txtReactionComment.text =
                    dataSource.customer_like_ids.size.toString()
                binding.txtFavorite.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
            }
            commentHandler.onReactionComment(dataSource.comment_id ?: "")
        }


        binding.lnReactionComment.setOnClickListener {
            commentHandler.onDetailReactionComment(dataSource.customer_like_ids)
        }



        binding.ctlMain.setOnLongClickListener {
            commentHandler.onLongClickComment(
                position,
                dataSource,
                TechresEnum.TYPE_COMMENT.toString()
            )
            true
        }

        binding.txtName.setOnClickListener {
            dataSource.customer_id?.let { it1 ->
                commentHandler.clickProfile(
                    position,
                    it1
                )
            }
        }

        binding.imgMedia.setOnClickListener {
            commentHandler.showImage(position, dataSource.image_urls[0].original ?: "")
        }

        binding.imgAvatar.setOnClickListener {
            commentHandler.showImage(position, dataSource.customer_avatar.original ?: "")
        }

        binding.lnReplyComment.setOnClickListener {
            commentHandler.replyComment(
                position,
                0,
                dataSource,
                TechresEnum.TYPE_COMMENT.toString(),
                ""
            )
        }

        binding.txtReply.setOnClickListener {
            commentHandler.replyComment(
                position,
                0,
                dataSource,
                TechresEnum.TYPE_COMMENT.toString(),
                ""
            )
        }
    }
}