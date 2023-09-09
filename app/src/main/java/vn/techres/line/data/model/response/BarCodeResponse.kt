package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class BarCodeResponse : BaseResponse(){
    @JsonField(name=["data"])
    var data : Int? = 0
}