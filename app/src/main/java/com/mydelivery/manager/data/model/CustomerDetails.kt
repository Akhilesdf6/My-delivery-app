package com.mydelivery.manager.data.model

import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.data.local.entity.ShipmentEntity

data class CustomerDetails(
    val customer: CustomerEntity,
    val address: AddressEntity?,
    val shipment: ShipmentEntity?,
    val cod: CodEntity?,
)
