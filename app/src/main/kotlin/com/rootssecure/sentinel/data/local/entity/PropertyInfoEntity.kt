package com.rootssecure.sentinel.data.local.entity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "property_info")
data class PropertyInfoEntity(
    @PrimaryKey val id: Int = 1, // Only one property profile for now
    @ColumnInfo(name = "property_name") val propertyName: String,
    @ColumnInfo(name = "owner_name")    val ownerName: String,
    @ColumnInfo(name = "address")       val address: String,
    @ColumnInfo(name = "site_image_url") val siteImageUrl: String? = null
)

@Dao
interface PropertyDao {
    @Query("SELECT * FROM property_info WHERE id = 1 LIMIT 1")
    fun getPropertyInfo(): Flow<PropertyInfoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePropertyInfo(propertyInfo: PropertyInfoEntity)
}
