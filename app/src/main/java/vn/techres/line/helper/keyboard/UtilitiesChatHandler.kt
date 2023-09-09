package vn.techres.line.helper.keyboard

import vn.techres.line.data.model.chat.Sticker

interface UtilitiesChatHandler {
    fun onVote()
    fun onChooseFile()
    fun onChooseBusinessCard()
    fun onChooseSticker(sticker: Sticker)
    fun onImportantMessage()
}