package vn.techres.line.helper.keyboard

import android.view.View
import android.view.ViewGroup
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.sticker.StickerCategory
import vn.techres.line.data.model.chat.CategorySticker

class AppSticker(var categorySticker: CategorySticker) : StickerCategory<String> {

    override fun getStickers(): Array<Sticker<*>?> {
        val stickers: Array<Sticker<*>?> = arrayOfNulls(categorySticker.stickers?.size ?: 0)
        for (i in 0 until categorySticker.stickers?.size!!) {
            stickers[i] = Sticker<vn.techres.line.data.model.chat.Sticker>(categorySticker.stickers?.get(i))
        }
        return stickers
    }

    override fun getCategoryData(): String = categorySticker.link_original ?: ""

    override fun useCustomView(): Boolean = false

    override fun getView(viewGroup: ViewGroup?): View? = null

    override fun bindView(view: View?) {
    }

    override fun getEmptyView(viewGroup: ViewGroup?): View? = null
}