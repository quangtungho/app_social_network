package vn.techres.line.interfaces

import android.view.View
import vn.techres.line.data.model.address.Address

interface DeleteAddressHandler {
    fun onDeleteAddress(address: Address, view: View)
}