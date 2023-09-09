package vn.techres.line.interfaces

import vn.techres.line.data.model.chat.Members

interface MemberGroupHandler {
    fun onAddFriend(member : Members)
    fun onAcceptFriend(member : Members)
    fun onChooseMember(member : Members)
}