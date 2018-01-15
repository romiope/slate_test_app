package slate.com.slatetestapp.repository.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.android.gms.location.Geofence

@Entity
data class LocalGeofence(var SSID: String = "", var latitude: Double = 0.toDouble(),
                         var longitude: Double = 0.toDouble(), var radius: Float = 0f) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}

private const val GEOFENCE_UPDATE_TIME_MILIS = 300000
private const val EXPIRATION_TIME = 86400000L

fun LocalGeofence.toGeofence()
        = Geofence.Builder().apply {
    setRequestId(id.toString())
    setCircularRegion(latitude, longitude, radius)
    setNotificationResponsiveness(GEOFENCE_UPDATE_TIME_MILIS)
    setLoiteringDelay(GEOFENCE_UPDATE_TIME_MILIS)
    setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
    setExpirationDuration(EXPIRATION_TIME)
}.build()