package com.virtual5g.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import android.content.Context

@Entity(tableName = "last_network_measurement")
data class NetworkMeasurementEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val connectionType: String,
    val nrAttachment: String,
    val downloadMbps: Double?,
    val uploadMbps: Double?,
    val pingMs: Double?,
    val jitterMs: Double?,
    val packetLossPercent: Double?,
    val signalStrengthDbm: Int?,
    val signalLevel: Int?,
    val carrierName: String?,
    val timestampMillis: Long
) {
    companion object {
        /** Only one row is ever kept - we only need the single most recent snapshot. */
        const val SINGLETON_ID = 0
    }
}

@Dao
interface NetworkMeasurementDao {
    @Upsert
    suspend fun upsert(entity: NetworkMeasurementEntity)

    @Query("SELECT * FROM last_network_measurement WHERE id = ${NetworkMeasurementEntity.SINGLETON_ID} LIMIT 1")
    suspend fun getLast(): NetworkMeasurementEntity?
}

@Database(entities = [NetworkMeasurementEntity::class], version = 1, exportSchema = false)
abstract class Virtual5GDatabase : RoomDatabase() {
    abstract fun networkMeasurementDao(): NetworkMeasurementDao

    companion object {
        @Volatile private var instance: Virtual5GDatabase? = null

        fun getInstance(context: Context): Virtual5GDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    Virtual5GDatabase::class.java,
                    "virtual5g.db"
                ).build().also { instance = it }
            }
    }
}

/** Backwards-compatible alias used by the app and worker layers. */
typealias NetworkMeasurementDb = Virtual5GDatabase
