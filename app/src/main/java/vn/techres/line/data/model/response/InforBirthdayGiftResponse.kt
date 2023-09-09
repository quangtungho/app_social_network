package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.utils.InfoBirthdayGift

class InforBirthdayGiftResponse: BaseResponse() {
    @JsonField(name = ["data"])
    var data = ArrayList<InfoBirthdayGift>()

}