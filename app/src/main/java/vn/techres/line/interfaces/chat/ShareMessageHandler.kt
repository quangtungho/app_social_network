package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.Group

interface ShareMessageHandler {
    fun onChooseGroup(data: Group, type: Int)
}