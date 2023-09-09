package vn.techres.line.interfaces.gift

import vn.techres.line.data.model.gift.Gift


/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/14/2022
 */
interface GiftHandler {
    fun onGetGift(gift : Gift)
}