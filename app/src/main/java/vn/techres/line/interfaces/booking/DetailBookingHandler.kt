package vn.techres.line.interfaces.booking

import vn.techres.line.data.model.booking.BookingFood

interface DetailBookingHandler {
    fun onGift(bookingFood : BookingFood)
}