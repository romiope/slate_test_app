package slate.com.slatetestapp.repository.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class LocalGeofence(var SSID: String = "", var longitude: Double = 0.toDouble(),
                         var latitude: Double = 0.toDouble(), var radius: Int = 0) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}