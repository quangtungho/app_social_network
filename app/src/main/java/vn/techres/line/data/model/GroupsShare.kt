package vn.techres.line.data.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.chat.Group
import java.io.Serializable

/**
 * @Author: Phạm Văn Nhân
 * @Date: 01/07/2022
 */

@JsonObject
class GroupsShare : Serializable {
    @JsonField(name = ["list"])
    var list = ArrayList<Group>()

    @JsonField(name = ["groups"])
    var groups = ArrayList<Group>()

    @JsonField(name = ["group_personals"])
    var group_personals = ArrayList<Group>()
}