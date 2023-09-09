package vn.techres.line.interfaces.comment

import vn.techres.line.data.model.reaction.InteractiveUser

interface ReactionCommentHandler {
    fun clickAddFriend(interactiveUser: InteractiveUser)
    fun clickItemMyFriend(interactiveUser: InteractiveUser)
    fun onChat(interactiveUser: InteractiveUser)
    fun clickProfile(position: Int, userId: Int)
    fun onAvatar(url: String, position: Int)
}