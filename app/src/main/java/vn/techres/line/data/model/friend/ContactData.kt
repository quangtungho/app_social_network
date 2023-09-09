package vn.techres.line.data.model.friend

import com.bluelinelabs.logansquare.annotation.JsonField

class ContactData {
    @JsonField(name = ["list"])
    var list =  ArrayList<Contact>()
}