package vn.techres.line.interfaces.newsfeed

import vn.techres.data.line.model.PostReview
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.utils.Media

interface NewsFeedHandler {
    fun onMyAvatar()
    fun onAvatar(url: String, position: Int)
    fun onPost()
    fun onReview()
    fun onComment(positionPost: Int, type: String, detailPost: PostReview, positionComment: Int)
    fun onShare()
    fun onButtonMore(position: Int)
    fun onMedia(url: ArrayList<Media>, position: Int)
    fun onBranchDetail(id: Int?)
    fun onBranchReviewDetail(id: String?)
    fun onReaction(id: String, idReaction: Int)
    fun onReactionDetail(position: Int, id: String?)
    fun onSaveBranch(position : Int, branchID : Int)
    fun onDetailRatingReview(serviceRate : Float, foodRate : Float, priceRate : Float, spaceRate : Float, hygieneRate : Float)
    fun onDetailValuePost(idReview: String?)
    fun onClickProfile(position: Int, userID: Int)
    fun onSentComment(position: Int, id: String, content: String)
//    fun onLevelValue(position: Int, levelID: Int)
    fun onShowFullVideoYouTube(youtube: YouTube)
    fun onLink(url: String)
    fun onReactionComment(idPost: String, idComment: String)
    fun onMedia(url: ArrayList<Media>, position: Int, seekTo : Int)

}