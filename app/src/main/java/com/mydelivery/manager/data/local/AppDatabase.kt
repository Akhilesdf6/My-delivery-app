package com.mydelivery.manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mydelivery.manager.data.local.dao.AddressDao
import com.mydelivery.manager.data.local.dao.CodDao
import com.mydelivery.manager.data.local.dao.CustomerDao
import com.mydelivery.manager.data.local.dao.ExpenseDao
import com.mydelivery.manager.data.local.dao.IncomeDao
import com.mydelivery.manager.data.local.dao.ShipmentDao
import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
import com.mydelivery.manager.data.local.entity.ExpenseEntity
import com.mydelivery.manager.data.local.entity.IncomeEntity
import com.mydelivery.manager.data.local.entity.ShipmentEntity

@Database(
    entities = [
        CustomerEntity::class,
        AddressEntity::class,
        ShipmentEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        CodEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun addressDao(): AddressDao
    abstract fun shipmentDao(): ShipmentDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun codDao(): CodDao

    companion object {
        const val DATABASE_NAME = "my_delivery_manager.db"

        @Volatile
        private var instance: AppDatabase? = null

        /** The one and only production database instance. */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }

        // Deliberately NO fallbackToDestructiveMigration(): user data must never be wiped silently.
        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
    }
}
