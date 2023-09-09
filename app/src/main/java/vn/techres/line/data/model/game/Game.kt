package vn.techres.line.data.model.game

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Game : Serializable {
    @JsonField(name=["name"])
    var name : String? = ""

    @JsonField(name=["_id"])
    var _id : String? = ""

    @JsonField(name=["reward"])
    var reward : String? = ""

    @JsonField(name=["status"])
    var statusGame : Int? = 0

    @JsonField(name=["total_player"])
    var total_player : Int? = 0

    @JsonField(name=["contain"])
    var contain : String? = ""

    @JsonField(name=["rule"])
    var rule : String? = ""

    @JsonField(name=["avatar"])
    var avatar : String? = ""

    @JsonField(name=["createdAt"])
    var createdAt : String? = ""

    @JsonField(name=["updateAt"])
    var updateAt : String? = ""

    @JsonField(name=["prefix"])
    var prefix : String? = ""

    @JsonField(name=["normalize_name"])
    var normalize_name : String? = ""

    @JsonField(name=["link"])
    var link : String? = ""

    @JsonField(name=["type"])
    var type : Int? = 0
}