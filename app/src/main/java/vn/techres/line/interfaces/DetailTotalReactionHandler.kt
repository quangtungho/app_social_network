package vn.techres.line.interfaces

import vn.techres.line.data.model.reaction.InteractiveUser

interface DetailTotalReactionHandler {
    fun clickAddFriend(interactiveUser: InteractiveUser)
    fun clickItemMyFriend(interactiveUser: InteractiveUser)
    fun onChat(interactiveUser: InteractiveUser)
}