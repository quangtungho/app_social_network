package vn.techres.line.interfaces.profile

import vn.techres.line.data.model.utils.User


interface ProfileHandler {
    fun clickImage(url: String, position: Int)
    fun createPostProfile()
    fun onAddFriend()
    fun onUnfriend(data: User)
    fun onReplyFriend()
    fun onCancelRequestFriend()
    fun chat()
    fun clickButtonFriend()
    fun onAcceptFriend(data: User)
    fun onUnAcceptFriend(data: User)

//    fun clickMore()

    fun clickEditProfile()

    fun searchFriend()
    fun clickViewAllFriend()

    fun clickMediaImage()
    fun clickMediaVideo()
    fun clickMediaAlbum()

    fun clickReportUser()

    //Interface timeline
//    fun onAvatar(url: String, position: Int)
//    fun onComment(positionPost: Int, type: String, detailPost: PostReview, positionComment: Int)
//    fun onShare()
//    fun onButtonMore(position: Int)
//    fun onMedia(url: ArrayList<Media>, position: Int)
//    fun onBranchDetail(id: Int?)
//    fun onBranchReviewDetail(id: Int?)
//    fun onReaction(id: Int, idReaction: Int)
//    fun onReactionDetail(position: Int, id: Int?)
//    fun onSaveBranch(position : Int, branchID : Int)
//    fun onDetailRatingReview(service_rate : Float, food_rate : Float, price_rate : Float, space_rate : Float, hygiene_rate : Float)
//    fun onDetailValuePost(idReview: Int?)
//    fun onClickProfile(position: Int, userID: Int)
//    fun onSentComment(position: Int, idComment: Int, content: String)
//
//    fun onShowFullVideoYouTube(youtube: YouTube)
//    fun onLink(url: String)
//    fun onReactionComment(idPost: Int, idReaction: Int)
}