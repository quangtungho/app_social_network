package vn.techres.line.interfaces

import vn.techres.line.data.model.address.Address

interface ChooseAddressHandler {
    fun onChooseAddress(address: Address)
}