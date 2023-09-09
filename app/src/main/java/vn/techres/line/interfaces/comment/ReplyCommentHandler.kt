package vn.techres.line.interfaces.comment

import vn.techres.line.data.model.newfeed.Comment


interface ReplyCommentHandler {
    fun onLongClickReplyComment(positionComment: Int, positionReply: Int, data : Comment, type : String)
    fun showImage(position: Int, url : String)
    fun showImageComment(photo : String)
    fun replyComment(positionComment: Int, positionReply: Int, data : Comment, type : String, idCommentParent: String)
    fun clickProfile(position: Int, userId: Int)
    fun onReactionComment(idComment: String)
    fun onDetailReactionComment(data: ArrayList<Int>)
}