package slate.com.slatetestapp

import android.app.Application
import android.arch.persistence.room.Room
import slate.com.slatetestapp.repository.AppDatabase

class App: Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java,"database").build()
    }
}