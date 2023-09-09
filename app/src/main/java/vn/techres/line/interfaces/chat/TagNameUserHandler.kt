package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.Members

interface TagNameUserHandler {
    fun onTagName(member : Members)
}