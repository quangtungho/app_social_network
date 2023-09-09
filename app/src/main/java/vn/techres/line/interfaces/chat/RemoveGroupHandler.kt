package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.chat.Group

/**
 * @Author: Phạm Văn Nhân
 * @Date: 01/07/2022
 */
interface RemoveGroupHandler {
    fun removeGroup(data : Group)
}