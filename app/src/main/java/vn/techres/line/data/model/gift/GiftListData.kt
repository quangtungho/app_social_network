package vn.techres.line.data.model.gift

import com.bluelinelabs.logansquare.annotation.JsonField
import java.io.Serializable
import java.util.ArrayList

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/14/2022
 */
class GiftListData : Serializable {

    @JsonField(name = ["list"])
    var list =  ArrayList<Gift>()

    @JsonField(name = ["limit"])
    var limit : Int? = 0

    @JsonField(name = ["total_record"])
    var total_record : Int? = 0
}