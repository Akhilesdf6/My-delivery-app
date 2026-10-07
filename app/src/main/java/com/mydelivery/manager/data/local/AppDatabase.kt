package com.mydelivery.manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mydelivery.manager.data.local.dao.AddressDao
import com.mydelivery.manager.data.local.dao.CodDao
import com.mydelivery.manager.data.local.dao.DeliveryPhotoDao
import com.mydelivery.manager.data.local.dao.CustomerDao
import com.mydelivery.manager.data.local.dao.ExpenseDao
import com.mydelivery.manager.data.local.dao.IncomeDao
import com.mydelivery.manager.data.local.dao.ShipmentDao
import com.mydelivery.manager.data.local.entity.AddressEntity
import com.mydelivery.manager.data.local.entity.CodEntity
import com.mydelivery.manager.data.local.entity.CustomerEntity
import com.mydelivery.manager.data.local.entity.DeliveryPhotoEntity
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
        DeliveryPhotoEntity::class,
    ],
    version = 2,
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
    abstract fun deliveryPhotoDao(): DeliveryPhotoDao

    companion object {
        const val DATABASE_NAME = "my_delivery_manager.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(
                database: SupportSQLiteDatabase,
            ) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS delivery_photos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        shipmentId INTEGER NOT NULL,
                        uri TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(shipmentId)
                            REFERENCES shipments(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_delivery_photos_shipmentId
                    ON delivery_photos(shipmentId)
                    """.trimIndent(),
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
