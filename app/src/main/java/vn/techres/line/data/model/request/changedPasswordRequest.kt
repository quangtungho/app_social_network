package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable

class ChangedPasswordRequest: Serializable {
    @JsonField(name=["password"])
    var password:String?=null

    @JsonField(name=["new_password"])
    var new_password:String?=null

    @JsonField(name=["chat_token"])
    var node_access_token:String?=null
}