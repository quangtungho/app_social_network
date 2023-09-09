package vn.techres.line.data.model
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class Tags : Serializable{
    @JsonField(name = ["list"])
    var list =  ArrayList<Tag>()
}