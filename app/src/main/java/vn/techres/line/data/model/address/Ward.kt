package vn.techres.line.data.model.address

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Ward : Serializable{
    @JsonField(name = ["id"])
    var id : Int? = 0
    @JsonField(name = ["name"])
    var name : String? = ""
}