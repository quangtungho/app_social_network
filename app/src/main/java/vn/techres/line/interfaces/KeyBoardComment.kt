package vn.techres.line.interfaces

import vn.techres.line.data.model.chat.Sticker
import vn.techres.line.helper.keyboard.UtilitiesChatHandler

interface KeyBoardComment : UtilitiesChatHandler {
    override fun onVote() {
    }

    override fun onChooseFile() {
    }

    override fun onChooseBusinessCard() {
    }

    override fun onChooseSticker(sticker: Sticker) {
    }
}