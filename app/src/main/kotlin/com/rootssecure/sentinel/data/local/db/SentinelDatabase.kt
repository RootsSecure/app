package com.rootssecure.sentinel.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rootssecure.sentinel.data.local.dao.AlertEventDao
import com.rootssecure.sentinel.data.local.dao.HeartbeatDao
import com.rootssecure.sentinel.data.local.entity.AlertEventEntity
import com.rootssecure.sentinel.data.local.entity.HeartbeatEntity
import com.rootssecure.sentinel.data.local.entity.PropertyInfoEntity
import com.rootssecure.sentinel.data.local.entity.PropertyDao

/**
 * Root Room database declaration.
 */
@Database(
    entities  = [
        AlertEventEntity::class, 
        HeartbeatEntity::class,
        PropertyInfoEntity::class
    ],
    version   = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun alertEventDao(): AlertEventDao
    abstract fun heartbeatDao(): HeartbeatDao
    abstract fun propertyDao(): PropertyDao
}
