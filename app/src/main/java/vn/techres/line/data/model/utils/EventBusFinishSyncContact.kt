package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusFinishSyncContact() {
    @JsonField(name = ["finish"])
    var finish : Boolean = false
    constructor(finish: Boolean) : this(){
        this.finish = finish
    }
}