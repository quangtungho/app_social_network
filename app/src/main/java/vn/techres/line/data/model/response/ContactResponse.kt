package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.friend.Contact

class ContactResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data =  ArrayList<Contact>()
}