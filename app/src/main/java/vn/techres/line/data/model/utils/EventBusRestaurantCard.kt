package vn.techres.line.data.model.utils

import com.bluelinelabs.logansquare.annotation.JsonField

class EventBusRestaurantCard() {
    @JsonField(name = ["check"])
    var check : Boolean = false
    constructor(check: Boolean) : this(){
        this.check = check
    }
}