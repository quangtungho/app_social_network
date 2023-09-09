package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusFinishSyncPageGroup() {
    @JsonField(name = ["finish"])
    var page : Int = 0
    constructor(page: Int) : this(){
        this.page = page
    }
}