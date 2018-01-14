package slate.com.slatetestapp.repository

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

import slate.com.slatetestapp.repository.entity.LocalGeofence

import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface GeofenceDao {

    @Query("SELECT * FROM LocalGeofence")
    fun selectAll(): List<LocalGeofence>

    @Insert(onConflict = REPLACE)
    fun insert(localGeofence: LocalGeofence)

    @Query("DELETE FROM LocalGeofence")
    fun clean()
}
