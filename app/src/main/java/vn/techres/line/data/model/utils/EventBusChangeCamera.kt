package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusChangeCamera() {
    @JsonField(name = ["change_camera"])
    var change_camera : Boolean = false
    constructor(changeCamera: Boolean) : this(){
        this.change_camera = changeCamera
    }
}