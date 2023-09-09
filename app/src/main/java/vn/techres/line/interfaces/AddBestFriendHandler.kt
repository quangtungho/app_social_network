package vn.techres.line.interfaces

import vn.techres.line.data.model.friend.Friend

interface AddBestFriendHandler {
    fun chooseBestFriend(friend: Friend)
}