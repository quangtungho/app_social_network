package vn.techres.line.interfaces.gift

import vn.techres.line.data.model.chat.MessageGiftNotification

/**
 * @Author: Phạm Văn Nhân
 * @Date: 26/04/2022
 */
interface ChatGiftHandler {
    fun onGetGift(messageGiftNotification : MessageGiftNotification)
}