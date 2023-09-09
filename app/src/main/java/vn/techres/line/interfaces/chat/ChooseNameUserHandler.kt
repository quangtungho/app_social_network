package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.chat.TagName

interface ChooseNameUserHandler {
    fun onChooseTagName(tagName: TagName)
    fun onChooseNameUser(sender : Sender)
}