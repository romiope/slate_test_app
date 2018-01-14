package slate.com.slatetestapp.repository;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import slate.com.slatetestapp.repository.entity.LocalGeofence;

@Database(entities = {LocalGeofence.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GeofenceDao geofenceDao();
}
