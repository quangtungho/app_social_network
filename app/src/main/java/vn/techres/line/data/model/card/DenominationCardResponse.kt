package vn.techres.line.data.model.card

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class DenominationCardResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data :  ArrayList<DenominationCard>? = null
}