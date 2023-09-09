package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.Notifications

/**
 * @Author: Phạm Văn Nhân
 * @Date: 2/23/2022
 */
class NotificationsResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = Notifications()
}