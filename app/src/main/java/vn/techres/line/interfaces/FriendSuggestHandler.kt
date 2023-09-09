package vn.techres.line.interfaces

interface FriendSuggestHandler {
    fun onClickItemFriend(id: Int, position : Int)
    fun onAddFriend(position : Int)
    fun onSkipAddFriend(position : Int)
}