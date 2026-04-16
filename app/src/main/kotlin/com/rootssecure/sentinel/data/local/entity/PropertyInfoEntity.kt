package com.rootssecure.sentinel.data.local.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "property_info")
data class PropertyInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "property_name") val propertyName: String,
    @ColumnInfo(name = "owner_name")    val ownerName: String,
    @ColumnInfo(name = "address")       val address: String,
    @ColumnInfo(name = "mqtt_topic_id") val mqttTopicId: String,
    @ColumnInfo(name = "is_active")     val isActive: Boolean = false,
    @ColumnInfo(name = "site_image_url") val siteImageUrl: String? = null
)

@Dao
interface PropertyDao {
    @Query("SELECT * FROM property_info ORDER BY id ASC")
    fun getAllProperties(): Flow<List<PropertyInfoEntity>>

    @Query("SELECT * FROM property_info WHERE is_active = 1 LIMIT 1")
    fun getActiveProperty(): Flow<PropertyInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePropertyInfo(propertyInfo: PropertyInfoEntity)

    @Transaction
    suspend fun toggleActiveProperty(id: Int) {
        setAllInactive()
        setActive(id)
    }

    @Query("UPDATE property_info SET is_active = 0")
    suspend fun setAllInactive()

    @Query("UPDATE property_info SET is_active = 1 WHERE id = :id")
    suspend fun setActive(id: Int)
}
