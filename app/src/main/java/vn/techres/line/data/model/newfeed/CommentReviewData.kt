package vn.techres.line.data.model.newfeed

import com.bluelinelabs.logansquare.annotation.JsonField

class CommentReviewData {
    @JsonField(name = ["list"])
    var list =  ArrayList<Comment>()
    @JsonField(name = ["limit"])
    var limit : Int? = 0
    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}