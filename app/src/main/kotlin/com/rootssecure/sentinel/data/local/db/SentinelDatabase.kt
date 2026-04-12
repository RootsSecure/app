package com.rootssecure.sentinel.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rootssecure.sentinel.data.local.dao.AlertEventDao
import com.rootssecure.sentinel.data.local.dao.HeartbeatDao
import com.rootssecure.sentinel.data.local.entity.AlertEventEntity
import com.rootssecure.sentinel.data.local.entity.HeartbeatEntity

/**
 * Root Room database declaration.
 *
 * [version] must be incremented whenever the schema changes. Pair with a
 * [androidx.room.migration.Migration] to preserve existing data, or use
 * [fallbackToDestructiveMigration] during development.
 */
@Database(
    entities  = [AlertEventEntity::class, HeartbeatEntity::class],
    version   = 1,
    exportSchema = true
)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun alertEventDao(): AlertEventDao
    abstract fun heartbeatDao(): HeartbeatDao
}
