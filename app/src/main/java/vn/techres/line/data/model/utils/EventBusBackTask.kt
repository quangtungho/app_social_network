package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusBackTask() {
    @JsonField(name = ["is_back"])
    var is_back : Boolean = false
    constructor(isBack: Boolean) : this(){
        this.is_back = isBack
    }
}