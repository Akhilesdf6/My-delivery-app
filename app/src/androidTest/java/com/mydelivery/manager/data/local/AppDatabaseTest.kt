package com.mydelivery.manager.data.local

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
import com.mydelivery.manager.data.local.entity.ExpenseEntity
import com.mydelivery.manager.data.local.entity.IncomeEntity
import com.mydelivery.manager.data.local.entity.ShipmentEntity
import com.mydelivery.manager.data.model.CodStatus
import com.mydelivery.manager.data.model.ExpenseCategory
import com.mydelivery.manager.data.model.IncomeType
import com.mydelivery.manager.data.model.ShipmentStatus
import com.mydelivery.manager.data.repository.AddShipmentResult
import com.mydelivery.manager.data.repository.CustomerRepository
import com.mydelivery.manager.data.repository.IncomeRepository
import com.mydelivery.manager.data.repository.ShipmentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Uses an in-memory Room database created fresh for every test.
 * It is completely separate from the real my_delivery_manager.db file.
 * All values below are obviously synthetic test values.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase
    private val t0 = 1_700_000_000_000L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun test(block: suspend () -> Unit) {
        runBlocking { block() }
    }

    private inline fun <reified T : Throwable> expectFailure(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) return
            throw e
        }
        fail("Expected ${T::class.java.simpleName}")
    }

    private fun customer(name: String? = "Test Customer", phone: String? = "0000000001") =
        CustomerEntity(name = name, phone = phone, createdAt = t0, updatedAt = t0)

    private fun address(customerId: Long, pincode: String? = "000001", locality: String? = "Test Locality") =
        AddressEntity(
            customerId = customerId,
            fullAddress = "Test address $pincode",
            locality = locality,
            pincode = pincode,
            createdAt = t0,
            updatedAt = t0,
        )

    private fun shipment(
        shipmentId: String? = "TEST-1",
        customerId: Long? = null,
        addressId: Long? = null,
        status: ShipmentStatus = ShipmentStatus.PENDING,
        cod: Long? = null,
        createdAt: Long = t0,
    ) = ShipmentEntity(
        shipmentId = shipmentId,
        customerId = customerId,
        addressId = addressId,
        codAmountPaise = cod,
        status = status,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    // 1 + 2
    @Test
    fun customer_insertAndRetrieve() = test {
        val id = db.customerDao().insertCustomer(customer())
        val loaded = db.customerDao().getCustomerById(id)
        assertNotNull(loaded)
        assertEquals("Test Customer", loaded!!.name)
        assertEquals("0000000001", loaded.phone)
        assertEquals(1, db.customerDao().getAllCustomers().first().size)
    }

    @Test
    fun customer_missingInfoStaysNull() = test {
        val id = db.customerDao().insertCustomer(customer(name = null, phone = null))
        val loaded = db.customerDao().getCustomerById(id)!!
        assertNull(loaded.name)
        assertNull(loaded.phone)
    }

    // 3
    @Test
    fun customer_phoneLookupAndSearch() = test {
        val repo = CustomerRepository(db.customerDao(), db.addressDao())
        repo.addCustomer(customer(name = "Alpha Test", phone = "0000000001"))
        repo.addCustomer(customer(name = "Beta Test", phone = "0000000002"))
        assertEquals("Alpha Test", repo.getCustomerByPhone("0000000001")!!.name)
        assertNull(repo.getCustomerByPhone("9999999999"))
        assertEquals(listOf("Beta Test"), repo.searchCustomers("0002").map { it.name })
        assertEquals(listOf("Alpha Test"), repo.searchCustomers("alpha").map { it.name })
        assertEquals(2, repo.searchCustomers("Test").size)
        assertTrue(repo.searchCustomers("   ").isEmpty())
    }

    @Test
    fun customerSearch_treatsPercentLiterally() = test {
        val repo = CustomerRepository(db.customerDao(), db.addressDao())
        repo.addCustomer(customer(name = "Alpha Test"))
        assertTrue(repo.searchCustomers("%").isEmpty())
        assertTrue(repo.searchCustomers("_").isEmpty())
    }

    // 4
    @Test
    fun customer_hasMultipleAddresses() = test {
        val cid = db.customerDao().insertCustomer(customer())
        db.addressDao().insertAddress(address(cid, pincode = "000001").copy(isPrimary = true))
        db.addressDao().insertAddress(address(cid, pincode = "000002"))
        val list = db.addressDao().getAddressesForCustomer(cid)
        assertEquals(2, list.size)
        assertTrue(list.first().isPrimary)
        assertEquals(1, db.addressDao().getAddressesByPincode("000002").size)
    }

    // 5 + 6
    @Test
    fun shipment_linkedToCustomer_andFoundByShipmentId() = test {
        val cid = db.customerDao().insertCustomer(customer())
        val aid = db.addressDao().insertAddress(address(cid))
        val sid = db.shipmentDao().insertShipment(shipment("TEST-100", cid, aid))
        val byId = db.shipmentDao().getShipmentById(sid)!!
        assertEquals(cid, byId.customerId)
        assertEquals(aid, byId.addressId)
        assertEquals(sid, db.shipmentDao().getShipmentByShipmentId("TEST-100")!!.id)
        assertEquals(1, db.shipmentDao().getShipmentsForCustomer(cid).size)
        assertNull(db.shipmentDao().getShipmentByShipmentId("NOPE"))
    }

    // 7
    @Test
    fun shipment_duplicateShipmentIdIsRejected() = test {
        val repo = ShipmentRepository(db.shipmentDao())
        assertTrue(repo.addShipment(shipment("TEST-200")) is AddShipmentResult.Added)
        assertTrue(repo.shipmentIdExists("TEST-200"))
        assertFalse(repo.shipmentIdExists("TEST-201"))

        val second = repo.addShipment(shipment("  TEST-200  ", cod = 5000))
        assertTrue(second is AddShipmentResult.Duplicate)
        assertNotNull((second as AddShipmentResult.Duplicate).existing)
        assertEquals(1, repo.getAllShipments().size)
        // original untouched
        assertNull(repo.getShipmentByShipmentId("TEST-200")!!.codAmountPaise)
    }

    @Test
    fun shipment_unknownIdsAreNotTreatedAsDuplicates() = test {
        val repo = ShipmentRepository(db.shipmentDao())
        assertTrue(repo.addShipment(shipment(null)) is AddShipmentResult.Added)
        assertTrue(repo.addShipment(shipment("   ")) is AddShipmentResult.Added)
        assertEquals(2, repo.getAllShipments().size)
    }

    // 8
    @Test
    fun shipment_everyStatusIsStored() = test {
        for ((i, status) in ShipmentStatus.values().withIndex()) {
            db.shipmentDao().insertShipment(shipment("TEST-S$i", status = status))
        }
        for (status in ShipmentStatus.values()) {
            val found = db.shipmentDao().getShipmentsByStatus(status)
            assertEquals(1, found.size)
            assertEquals(status, found.single().status)
        }
        assertEquals(ShipmentStatus.values().size, db.shipmentDao().observeShipments().first().size)
    }

    // 9
    @Test
    fun cod_linkedToShipment_andOnlyOnePerShipment() = test {
        val sid = db.shipmentDao().insertShipment(shipment("TEST-300", cod = 25_000))
        db.codDao().insertCod(
            CodEntity(shipmentId = sid, amountPaise = 25_000, createdAt = t0, updatedAt = t0),
        )
        assertEquals(25_000L, db.codDao().getCodForShipment(sid)!!.amountPaise)
        assertEquals(1, db.codDao().getPendingCod().size)
        assertTrue(db.codDao().getCollectedCod().isEmpty())
        assertTrue(db.codDao().getDepositedCod().isEmpty())

        val saved = db.codDao().getCodForShipment(sid)!!
        db.codDao().updateCod(saved.copy(status = CodStatus.COLLECTED, collectedAt = t0 + 1))
        assertTrue(db.codDao().getPendingCod().isEmpty())
        assertEquals(1, db.codDao().getCollectedCod().size)
        db.codDao().updateCod(saved.copy(status = CodStatus.DEPOSITED, depositedAt = t0 + 2))
        assertEquals(1, db.codDao().getDepositedCod().size)

        expectFailure<SQLiteException> {
            runBlocking {
                db.codDao().insertCod(
                    CodEntity(shipmentId = sid, amountPaise = 1, createdAt = t0, updatedAt = t0),
                )
            }
        }
    }

    // 10
    @Test
    fun income_linkedToShipment() = test {
        val sid = db.shipmentDao().insertShipment(shipment("TEST-400"))
        val repo = IncomeRepository(db.incomeDao())
        repo.addIncome(
            IncomeEntity(
                shipmentId = sid,
                incomeDate = t0,
                type = IncomeType.DELIVERY_EARNING,
                grossAmountPaise = 3_000,
                tdsAmountPaise = 60,
                netAmountPaise = 2_940,
                createdAt = t0,
            ),
        )
        val list = repo.getIncomeForShipment(sid)
        assertEquals(1, list.size)
        assertEquals(IncomeType.DELIVERY_EARNING, list.single().type)
        assertEquals(1, repo.observeIncome().first().size)
    }

    @Test
    fun income_inconsistentAmountsAreRejected() = test {
        val repo = IncomeRepository(db.incomeDao())
        expectFailure<IllegalArgumentException> {
            runBlocking {
                repo.addIncome(
                    IncomeEntity(
                        incomeDate = t0,
                        grossAmountPaise = 3_000,
                        tdsAmountPaise = 60,
                        netAmountPaise = 3_000,
                        createdAt = t0,
                    ),
                )
            }
        }
    }

    // 11 + 13 (category round trip through the DB)
    @Test
    fun expense_insertAndCategoryRoundTrip() = test {
        for ((i, cat) in ExpenseCategory.values().withIndex()) {
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    amountPaise = 1_000L * (i + 1),
                    category = cat,
                    expenseDate = t0,
                    createdAt = t0,
                ),
            )
        }
        val all = db.expenseDao().observeExpenses().first()
        assertEquals(ExpenseCategory.values().size, all.size)
        assertEquals(ExpenseCategory.values().toSet(), all.map { it.category }.toSet())
    }

    // 12
    @Test
    fun money_isStoredExactlyAsLongPaise() = test {
        val rs1050 = 105_000L
        val huge = 9_000_000_000_000L // 90 billion rupees: far beyond Int, still exact
        db.expenseDao().insertExpense(
            ExpenseEntity(amountPaise = huge, category = ExpenseCategory.OTHER, expenseDate = t0, createdAt = t0),
        )
        db.incomeDao().insertIncome(
            IncomeEntity(
                incomeDate = t0,
                grossAmountPaise = rs1050,
                tdsAmountPaise = 2_100,
                netAmountPaise = rs1050 - 2_100,
                createdAt = t0,
            ),
        )
        assertEquals(huge, db.expenseDao().observeExpenses().first().single().amountPaise)
        val income = db.incomeDao().observeIncome().first().single()
        assertEquals(105_000L, income.grossAmountPaise)
        assertEquals(102_900L, income.netAmountPaise)
    }

    // 14
    @Test
    fun dateRangeQueries_areStartInclusiveEndExclusive() = test {
        val day = 86_400_000L
        for (offset in listOf(0L, 1L, 2L)) {
            db.incomeDao().insertIncome(
                IncomeEntity(
                    incomeDate = t0 + offset * day,
                    grossAmountPaise = 3_000,
                    tdsAmountPaise = 0,
                    netAmountPaise = 3_000,
                    createdAt = t0,
                ),
            )
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    amountPaise = 500,
                    category = ExpenseCategory.FOOD,
                    expenseDate = t0 + offset * day,
                    createdAt = t0,
                ),
            )
        }
        assertEquals(2, db.incomeDao().getIncomeByDateRange(t0, t0 + 2 * day).size)
        assertEquals(1, db.incomeDao().getIncomeByDateRange(t0 + day, t0 + 2 * day).size)
        assertEquals(3, db.incomeDao().getIncomeByDateRange(t0, t0 + 2 * day + 1).size)
        assertEquals(2, db.expenseDao().getExpensesByDateRange(t0, t0 + 2 * day).size)
        assertTrue(db.expenseDao().getExpensesByDateRange(t0 + 5 * day, t0 + 6 * day).isEmpty())
    }

    // 15
    @Test
    fun historicalData_cannotBeDeletedByAccident() = test {
        val cid = db.customerDao().insertCustomer(customer())
        val aid = db.addressDao().insertAddress(address(cid))
        val sid = db.shipmentDao().insertShipment(shipment("TEST-500", cid, aid, cod = 10_000))
        db.codDao().insertCod(CodEntity(shipmentId = sid, amountPaise = 10_000, createdAt = t0, updatedAt = t0))
        db.incomeDao().insertIncome(
            IncomeEntity(
                shipmentId = sid,
                incomeDate = t0,
                grossAmountPaise = 3_000,
                tdsAmountPaise = 0,
                netAmountPaise = 3_000,
                createdAt = t0,
            ),
        )
        val raw = db.openHelper.writableDatabase
        expectFailure<SQLiteException> { raw.execSQL("DELETE FROM customers WHERE id = $cid") }
        expectFailure<SQLiteException> { raw.execSQL("DELETE FROM addresses WHERE id = $aid") }
        expectFailure<SQLiteException> { raw.execSQL("DELETE FROM shipments WHERE id = $sid") }

        assertNotNull(db.customerDao().getCustomerById(cid))
        assertNotNull(db.addressDao().getAddressById(aid))
        assertNotNull(db.shipmentDao().getShipmentById(sid))
        assertNotNull(db.codDao().getCodForShipment(sid))
        assertEquals(1, db.incomeDao().getIncomeForShipment(sid).size)
    }

    @Test
    fun editingCustomer_doesNotTouchHistory() = test {
        val repo = CustomerRepository(db.customerDao(), db.addressDao(), clock = { t0 + 99 })
        val cid = repo.addCustomer(customer(name = "Old Name"))
        val aid = repo.addAddress(address(cid))
        db.shipmentDao().insertShipment(shipment("TEST-600", cid, aid))

        repo.updateCustomer(repo.getCustomerById(cid)!!.copy(name = "New Name"))

        assertEquals("New Name", repo.getCustomerById(cid)!!.name)
        assertEquals(t0 + 99, repo.getCustomerById(cid)!!.updatedAt)
        assertEquals(1, db.shipmentDao().getShipmentsForCustomer(cid).size)
        assertEquals(aid, db.shipmentDao().getShipmentByShipmentId("TEST-600")!!.addressId)
    }
}
