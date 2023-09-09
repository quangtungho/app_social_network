package vn.techres.line.data.model.reaction

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.response.BaseResponse

class ValueReactionResponse: BaseResponse(){
    @JsonField(name = ["data"])
    var data = ArrayList<ValueReaction>()
}