package vn.techres.line.interfaces

import vn.techres.line.data.model.chat.CategorySticker

interface StickerHandler {
    fun onClick(sticker : CategorySticker)
}