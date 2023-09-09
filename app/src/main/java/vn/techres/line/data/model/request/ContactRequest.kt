package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

class ContactRequest {
    @JsonField(name = ["list_phone"])
    var list_phone = mutableListOf<String>()
}