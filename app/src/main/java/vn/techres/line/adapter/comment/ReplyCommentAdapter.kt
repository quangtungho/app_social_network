package vn.techres.line.adapter.comment

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.holder.newfeed.ItemReplyCommentHolder
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.databinding.ItemReplyCommentBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.interfaces.comment.ReplyCommentHandler

class ReplyCommentAdapter (val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataSource = ArrayList<Comment>()
    private var positionComment = 0
    private var idCommentParent = ""
    private var replyCommentHandler: ReplyCommentHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var user = CurrentUser.getCurrentUser(context)

    @SuppressLint("UseRequireInsteadOfGet")

    fun setDataSource(dataSource: ArrayList<Comment>, positionComment: Int, idCommentParent: String) {
        this.dataSource = dataSource
        this.positionComment = positionComment
        this.idCommentParent = idCommentParent
        notifyDataSetChanged()
    }

    fun setEventReplyComment(replyCommentHandler: ReplyCommentHandler?) {
        this.replyCommentHandler = replyCommentHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemReplyCommentHolder(ItemReplyCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemReplyCommentHolder).bin(dataSource[position], context, positionComment, idCommentParent, user, configNodeJs, replyCommentHandler!!)

    }
}