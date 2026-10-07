package com.mydelivery.manager.data.local

import androidx.room.TypeConverter
import com.mydelivery.manager.data.model.CodStatus
import com.mydelivery.manager.data.model.ExpenseCategory
import com.mydelivery.manager.data.model.IncomeType
import com.mydelivery.manager.data.model.ShipmentStatus

/**
 * Enums are stored as their names. An unknown stored value throws
 * IllegalArgumentException instead of silently becoming something else.
 */
class Converters {
    @TypeConverter fun shipmentStatusToString(value: ShipmentStatus): String = value.name
    @TypeConverter fun stringToShipmentStatus(value: String): ShipmentStatus = ShipmentStatus.valueOf(value)

    @TypeConverter fun codStatusToString(value: CodStatus): String = value.name
    @TypeConverter fun stringToCodStatus(value: String): CodStatus = CodStatus.valueOf(value)

    @TypeConverter fun incomeTypeToString(value: IncomeType): String = value.name
    @TypeConverter fun stringToIncomeType(value: String): IncomeType = IncomeType.valueOf(value)

    @TypeConverter fun expenseCategoryToString(value: ExpenseCategory): String = value.name
    @TypeConverter fun stringToExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
