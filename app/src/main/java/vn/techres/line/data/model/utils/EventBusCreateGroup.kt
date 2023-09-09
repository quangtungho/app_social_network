package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.chat.Group

class EventBusCreateGroup() {
    @JsonField(name = ["group"])
    var group = Group()
    constructor(group : Group) : this(){
        this.group = group
    }
}