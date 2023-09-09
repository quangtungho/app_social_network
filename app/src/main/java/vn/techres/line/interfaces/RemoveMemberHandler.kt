package vn.techres.line.interfaces

import vn.techres.line.data.model.UserChat

interface RemoveMemberHandler {
    fun removeMember(userChat : UserChat)
}