package com.mydelivery.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mydelivery.manager.data.local.entity.DeliveryPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryPhotoDao {

    @Insert
    suspend fun insertPhoto(photo: DeliveryPhotoEntity): Long

    @Insert
    suspend fun insertPhotos(photos: List<DeliveryPhotoEntity>)

    @Query(
        "SELECT * FROM delivery_photos " +
            "WHERE shipmentId = :shipmentId " +
            "ORDER BY createdAt ASC, id ASC"
    )
    fun observePhotos(shipmentId: Long): Flow<List<DeliveryPhotoEntity>>

    @Query(
        "SELECT * FROM delivery_photos " +
            "WHERE shipmentId = :shipmentId " +
            "ORDER BY createdAt ASC, id ASC"
    )
    suspend fun getPhotos(shipmentId: Long): List<DeliveryPhotoEntity>

    @Query(
        "SELECT COUNT(*) FROM delivery_photos " +
            "WHERE shipmentId = :shipmentId"
    )
    suspend fun getPhotoCount(shipmentId: Long): Int

    @Delete
    suspend fun deletePhoto(photo: DeliveryPhotoEntity): Int

    @Query("DELETE FROM delivery_photos WHERE shipmentId = :shipmentId")
    suspend fun deletePhotosForShipment(shipmentId: Long): Int
}
