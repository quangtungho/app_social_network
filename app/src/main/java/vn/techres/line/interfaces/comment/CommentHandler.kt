package vn.techres.line.interfaces.comment

import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.utils.Media


interface CommentHandler {
    fun onLongClickComment(position : Int, data : Comment, type : String)
    fun showImage(position: Int, url : String)
    fun showImageComment(photo : String)
    fun replyComment(positionComment: Int, positionReply: Int, data : Comment, type : String, idCommentParent: String)
    fun clickProfile(position: Int, userId: Int)

    //Interface timeline
    fun onAvatar(url: String)
    fun onComment()
    fun onShare()
    fun onButtonMore()
    fun onMedia(url: ArrayList<Media>, position: Int)
    fun onBranchDetail(id: Int?)
    fun onBranchReviewDetail(id: Int?)
    fun onReaction(id: String, idReaction: Int)
    fun onReactionDetail()
    fun onSaveBranch(branchID : Int)
    fun onDetailRatingReview(serviceRate : Float, foodRate : Float, priceRate : Float, spaceRate : Float, hygieneRate : Float)
    fun onClickProfile(userID: Int)
    fun onReactionComment(idComment: String)
    fun onDetailReactionComment(data: ArrayList<Int>)

    fun onShowFullVideoYouTube(youtube: YouTube)
    fun onLink(url: String)
}