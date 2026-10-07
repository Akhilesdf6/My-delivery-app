package com.mydelivery.manager.data.model

// These names are stored as text in the database. Never rename a value without a migration.

enum class ShipmentStatus {
    PENDING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    UNDELIVERED,
    CANCELLED,
    RETURNED,
}

enum class CodStatus {
    PENDING,
    COLLECTED,
    DEPOSITED,
}

enum class IncomeType {
    DELIVERY_EARNING,
    OTHER,
}

enum class ExpenseCategory {
    PETROL,
    FOOD,
    PARKING,
    VEHICLE_MAINTENANCE,
    RECHARGE,
    OTHER,
}
