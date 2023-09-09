package vn.techres.line.data.model.chat

import androidx.room.TypeConverter
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class Reaction : Serializable {
    @JsonField(name = ["is_revoke"])
    var is_revoke: Int? = 0
    @JsonField(name = ["reactions_count"])
    var reactions_count: Int? = 0

    @JsonField(name = ["last_reactions"])
    var last_reactions: Int? = 0

    @JsonField(name = ["love"])
    var love: Int? = 0

    @JsonField(name = ["smile"])
    var smile: Int? = 0

    @JsonField(name = ["like"])
    var like: Int? = 0

    @JsonField(name = ["angry"])
    var angry: Int? = 0

    @JsonField(name = ["sad"])
    var sad: Int? = 0

    @JsonField(name = ["wow"])
    var wow: Int? = 0

    @JsonField(name = ["my_reaction"])
    var my_reaction: Int? = 0

    @JsonField(name = ["last_reactions_id"])
    var last_reactions_id: Int? = 0

    @JsonField(name = ["list_reaction_love"])
    var list_reaction_love: ListUserReaction? = null

    @JsonField(name = ["list_reaction_like"])
    var list_reaction_like: ListUserReaction? = null
    @JsonField(name = ["list_reaction_sad"])
    var list_reaction_sad: ListUserReaction? = null
    @JsonField(name = ["list_reaction_wow"])
    var list_reaction_wow: ListUserReaction? = null

    @JsonField(name = ["list_reaction_angry"])
    var list_reaction_angry: ListUserReaction? = null
    @JsonField(name = ["list_reaction_smile"])
    var list_reaction_smile: ListUserReaction? = null
    @JsonField(name = ["list_reaction_all"])
    var list_reaction_all: ListUserReaction? = null

    @TypeConverter
    fun fromReactionJson(reaction: Reaction): String {
        return Gson().toJson(reaction)
    }

    @TypeConverter
    fun toReactionJson(json: String): Reaction {
        return Gson().fromJson(
            json, object :
                TypeToken<Reaction>() {}.type
        )
    }
}