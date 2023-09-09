package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusEndCall() {
    @JsonField(name = ["isCall"])
    var isCall : Boolean = false
    constructor(isCall: Boolean) : this(){
        this.isCall = isCall
    }
}