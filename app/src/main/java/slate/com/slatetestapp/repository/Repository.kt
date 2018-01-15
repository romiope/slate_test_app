package slate.com.slatetestapp.repository

import android.arch.persistence.room.Room
import android.content.Context
import slate.com.slatetestapp.repository.entity.LocalGeofence
import kotlin.concurrent.thread

class Repository(context: Context) {

    val database by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java,"database")
                .build()
    }

    fun selectAll(): List<LocalGeofence> {
        var result:List<LocalGeofence> = listOf()
        thread {
            result = database.geofenceDao().selectAll()
        }.join()
        return result
    }

    fun insert(localGeofence: LocalGeofence) {
        thread {
            database.geofenceDao().insert(localGeofence)
        }.join()
    }

    fun clean() {
        thread {
            database.geofenceDao().clean()
        }.join()
    }

    fun count(): Int {
        var count = 0
        thread {
            count = database.geofenceDao().count()
        }.join()
        return count
    }
}