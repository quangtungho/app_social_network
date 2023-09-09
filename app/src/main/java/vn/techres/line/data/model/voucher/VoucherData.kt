package vn.techres.line.data.model.voucher

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable
import java.util.ArrayList

@JsonObject
class VoucherData: Serializable {
    @JsonField(name = ["list"])
    var list =  ArrayList<Voucher>()
}