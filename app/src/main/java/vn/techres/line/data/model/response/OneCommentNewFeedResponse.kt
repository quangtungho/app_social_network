package vn.techres.line.data.model.response

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import vn.techres.line.data.model.newfeed.CommentNewFeed

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class OneCommentNewFeedResponse : BaseResponse() {
    @JsonField(name = ["data"])
    var data = CommentNewFeed()
}