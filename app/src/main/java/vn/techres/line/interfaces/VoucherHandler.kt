package vn.techres.line.interfaces

import vn.techres.line.data.model.voucher.Voucher

interface VoucherHandler {
    fun onClickVoucher(data: Voucher)
    fun onClickTakeVoucher(data: Voucher)
}