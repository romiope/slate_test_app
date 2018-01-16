package slate.com.slatetestapp

import android.app.IntentService
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionsIntentService : IntentService(TAG) {
    companion object {
        const val UPDATE_ACTION = "UPDATE_ACTION"
        const val IS_IN_AREA = "IS_IN_AREA"
        const val GEOFENCING_IDS = "GEOFENCING_IDS"
        private const val TAG = "Geofence Service"

        private fun getErrorString(errorCode: Int): String {
            return when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "GeoFence not available"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many GeoFences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
                else -> "Unknown error."
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        Intent(UPDATE_ACTION).apply {
            val geofenceTransition = geofencingEvent.geofenceTransition
            val status = when(geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_DWELL -> true
                Geofence.GEOFENCE_TRANSITION_ENTER -> true
                Geofence.GEOFENCE_TRANSITION_EXIT -> false
                else -> false
            }
            putExtra(IS_IN_AREA, status)

            putStringArrayListExtra(GEOFENCING_IDS, ArrayList(geofencingEvent.triggeringGeofences.map {
                it.requestId
            }))
            LocalBroadcastManager
                    .getInstance(applicationContext)
                    .sendBroadcast(this)
        }
    }
}
