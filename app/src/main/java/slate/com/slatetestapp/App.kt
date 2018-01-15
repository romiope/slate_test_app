package slate.com.slatetestapp

import android.app.Application
import slate.com.slatetestapp.repository.Repository

class App: Application() {

    lateinit var repository : Repository

    override fun onCreate() {
        super.onCreate()
        repository = Repository(applicationContext)
    }
}