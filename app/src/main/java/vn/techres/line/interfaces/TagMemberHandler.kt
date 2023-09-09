package vn.techres.line.interfaces

import vn.techres.line.data.model.UserChat

interface TagMemberHandler {
    fun chooseMember(userChat : UserChat)
}