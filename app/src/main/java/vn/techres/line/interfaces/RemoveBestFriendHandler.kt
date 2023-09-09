package vn.techres.line.interfaces

import vn.techres.line.data.model.friend.Friend

interface RemoveBestFriendHandler {
    fun removeBestFriend(friend : Friend)
}