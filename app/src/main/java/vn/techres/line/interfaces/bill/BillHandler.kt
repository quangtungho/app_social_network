package vn.techres.line.interfaces.bill

import vn.techres.line.data.model.OrderDetailBill

interface BillHandler {
    fun onGiftFood(food : OrderDetailBill)
    fun onNoteFood(food : OrderDetailBill)
}