package vn.techres.line.interfaces

import vn.techres.line.data.model.friend.Friend

interface FriendHandler {
    fun clickItemMyFriend(position: Int, userID: Int)
    fun clickCancelFriendRequest(position: Int, data: Friend)
    fun clickAcceptFriend(position: Int, data: Friend)
}