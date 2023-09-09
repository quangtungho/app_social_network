package vn.techres.line.interfaces.address

import vn.techres.line.data.model.address.Address

interface SaveAddressHandler {
    fun onChooseAddressRepair(address: Address)
}