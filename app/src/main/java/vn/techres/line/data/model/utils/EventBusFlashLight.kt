package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusFlashLight() {
    @JsonField(name = ["flash_light"])
    var flash_light : Boolean = false
    constructor(flashLight: Boolean) : this(){
        this.flash_light = flashLight
    }
}