package com.mydelivery.manager.data

import android.content.Context
import com.mydelivery.manager.data.local.AppDatabase
import com.mydelivery.manager.data.repository.CodRepository
import com.mydelivery.manager.data.repository.CustomerRepository
import com.mydelivery.manager.data.repository.ExpenseRepository
import com.mydelivery.manager.data.repository.IncomeRepository
import com.mydelivery.manager.data.repository.ShipmentRepository

/** Simple manual dependency holder. Everything is created lazily. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val customerRepository: CustomerRepository by lazy {
        CustomerRepository(
            customerDao = database.customerDao(),
            addressDao = database.addressDao(),
            shipmentDao = database.shipmentDao(),
            codDao = database.codDao()
        )
    }
    val shipmentRepository: ShipmentRepository by lazy {
        ShipmentRepository(
            dao = database.shipmentDao(),
            customerDao = database.customerDao(),
            addressDao = database.addressDao(),
            codDao = database.codDao(),
            deliveryPhotoDao = database.deliveryPhotoDao(),
            database = database
        )
    }
    val incomeRepository: IncomeRepository by lazy { IncomeRepository(database.incomeDao()) }
    val expenseRepository: ExpenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val codRepository: CodRepository by lazy { CodRepository(database.codDao()) }
}
