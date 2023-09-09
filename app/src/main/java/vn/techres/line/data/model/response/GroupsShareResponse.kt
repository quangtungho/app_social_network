package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.GroupsShare

/**
 * @Author: Phạm Văn Nhân
 * @Date: 01/07/2022
 */
class GroupsShareResponse : BaseResponse(){
    @JsonField(name = ["data"])
    var data = GroupsShare()
}