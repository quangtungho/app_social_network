package vn.techres.line.interfaces.newsfeed

interface FriendSuggestNewFeedHandler {
    fun onClickItemFriend(position : Int)
    fun onAddFriend(position : Int)
    fun onSeeMore()
    fun onClickAvatar(url: String, position : Int)
}