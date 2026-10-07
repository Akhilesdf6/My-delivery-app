package com.mydelivery.manager.data.local

import com.mydelivery.manager.data.model.CodStatus
import com.mydelivery.manager.data.model.ExpenseCategory
import com.mydelivery.manager.data.model.IncomeType
import com.mydelivery.manager.data.model.ShipmentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/** Pure JVM test: no Android or Robolectric needed. */
class ConvertersTest {
    private val c = Converters()

    @Test
    fun shipmentStatus_roundTrips() {
        for (v in ShipmentStatus.values()) {
            assertEquals(v.name, c.shipmentStatusToString(v))
            assertEquals(v, c.stringToShipmentStatus(c.shipmentStatusToString(v)))
        }
    }

    @Test
    fun codStatus_roundTrips_andNamesMatchDaoLiterals() {
        for (v in CodStatus.values()) {
            assertEquals(v, c.stringToCodStatus(c.codStatusToString(v)))
        }
        // CodDao uses these literals in SQL.
        assertEquals("PENDING", c.codStatusToString(CodStatus.PENDING))
        assertEquals("COLLECTED", c.codStatusToString(CodStatus.COLLECTED))
        assertEquals("DEPOSITED", c.codStatusToString(CodStatus.DEPOSITED))
    }

    @Test
    fun incomeType_roundTrips() {
        for (v in IncomeType.values()) {
            assertEquals(v, c.stringToIncomeType(c.incomeTypeToString(v)))
        }
    }

    @Test
    fun expenseCategory_roundTrips() {
        for (v in ExpenseCategory.values()) {
            assertEquals(v, c.stringToExpenseCategory(c.expenseCategoryToString(v)))
        }
    }

    @Test
    fun unknownStoredValue_failsLoudly() {
        try {
            c.stringToShipmentStatus("NOT_A_STATUS")
        } catch (e: IllegalArgumentException) {
            return
        }
        fail("Expected IllegalArgumentException")
    }
}
