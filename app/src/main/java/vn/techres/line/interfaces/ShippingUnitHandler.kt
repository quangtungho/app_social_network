package vn.techres.line.interfaces

import vn.techres.line.data.model.deliveries.ConfigDeliveries

interface ShippingUnitHandler {
    fun clickShippingUnit(configDeliveries: ConfigDeliveries)
}