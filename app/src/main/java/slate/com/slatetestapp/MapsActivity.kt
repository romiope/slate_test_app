package slate.com.slatetestapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.ConnectivityManager
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import slate.com.slatetestapp.GeofenceTransitionsIntentService.Companion.GEOFENCING_IDS
import slate.com.slatetestapp.GeofenceTransitionsIntentService.Companion.IS_IN_AREA
import slate.com.slatetestapp.GeofenceTransitionsIntentService.Companion.UPDATE_ACTION
import slate.com.slatetestapp.repository.entity.LocalGeofence
import slate.com.slatetestapp.repository.entity.toGeofence
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, AddGeofenceDialog.AddgeofenceListener {
    companion object {
        private const val LOCATION_PERMISSIONS_REQUEST = 101
        private const val GOOGLE_API_AVAILABILITY_REQUEST = 102
    }

    private lateinit var geofencingClient: GeofencingClient
    private var map: GoogleMap? = null
    private val circles = mutableSetOf<Circle>()
    private val idsInArea = mutableSetOf<String>()

    private val repository by lazy {
        (application as App).repository
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceTransitionsIntentService::class.java)
        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private val isInRegionReceiver by lazy { object : BroadcastReceiver() {
            private val filter = IntentFilter(UPDATE_ACTION)

            fun subscribe() {
                LocalBroadcastManager.getInstance(applicationContext)
                        .registerReceiver(this, filter)
            }

            fun unsubscribe() {
                try {
                    LocalBroadcastManager.getInstance(applicationContext)
                            .unregisterReceiver(this)
                } catch (e: Exception) {}
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val isInArea = it.getBooleanExtra(IS_IN_AREA, false)
                    val geofenceIds: ArrayList<String> = it.getStringArrayListExtra(GEOFENCING_IDS)
                    if (isInArea) {
                        idsInArea.addAll(geofenceIds)
                    } else {
                        idsInArea.removeAll(geofenceIds)
                    }
                    updateIsInAreaIndicator()
                }
            }
        }
    }

    private val connectivityChanged by lazy { object : BroadcastReceiver() {
        private val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

        fun subscribe()
                = applicationContext.registerReceiver(this, filter)

        fun unsubscribe() {
            try {
                applicationContext.unregisterReceiver(this)
            } catch (e: Exception) {}
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updateIsInAreaIndicator()
            }
        }
    }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        setSupportActionBar(app_toolbar)

        isInRegionReceiver.subscribe()
        geofencingClient = LocationServices.getGeofencingClient(this)

        add_floating_action_btn.setOnClickListener {
            if (repository.count() >= 100) {
                Snackbar.make(coordinator_container, getString(R.string.too_many_geofences), Snackbar.LENGTH_INDEFINITE).apply {
                    setAction(getString(R.string.cleanup), {
                        repository.clean()
                    })
                    setActionTextColor(Color.RED)
                    show()
                }
            } else {
                AddGeofenceDialog().show(supportFragmentManager)
            }
        }

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        connectivityChanged.subscribe()
        checkAvailabilityOfPlayServices()
        requestPermissionAccesFineLocaction()
        updateIsInAreaIndicator()
    }

    override fun onPause() {
        super.onPause()
        connectivityChanged.unsubscribe()
    }

    override fun onDestroy() {
        isInRegionReceiver.unsubscribe()
        super.onDestroy()
    }

    private fun checkAvailabilityOfPlayServices() {
        val (availability, errorCode) = checkPlayservices()
        if (errorCode != ConnectionResult.SUCCESS) {
            availability.getErrorDialog(this, errorCode, GOOGLE_API_AVAILABILITY_REQUEST)
        }
    }

    private fun checkPlayservices(): Pair<GoogleApiAvailability, Int> {
        val availability = GoogleApiAvailability.getInstance()
        val errorCode = availability.isGooglePlayServicesAvailable(this)
        return Pair(availability, errorCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_API_AVAILABILITY_REQUEST) {
            if (resultCode != ConnectionResult.SUCCESS) {
                onError(getString(R.string.google_api_is_not_available)) {
                    checkAvailabilityOfPlayServices()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val (_, errorCode) = checkPlayservices()
        if (errorCode == ConnectionResult.SUCCESS &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            startTracking()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                stopTracking()
                repository.clean()
                idsInArea.clear()
                updateIsInAreaIndicator()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationPermissionGranted()
                } else {
                    onError(getString(R.string.location_permission_denied)) {
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSIONS_REQUEST)
                    }
                }
            }
        }
    }

    override fun onNewGeofence(localGeofence: LocalGeofence) {
        repository.insert(localGeofence)
        addGeofence(localGeofence.toGeofence())
        drawGeofenceOnMap(localGeofence)
        updateIsInAreaIndicator()
    }

    private fun updateIsInAreaIndicator() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiInfo = wifiManager.connectionInfo
        if (idsInArea.isNotEmpty() || wifiInfo.supplicantState == SupplicantState.COMPLETED
                && repository.selectAll().any { "\"${it.SSID}\"" == wifiInfo.ssid }) {
            indicator_view.visibility = View.VISIBLE
        } else {
            indicator_view.visibility = View.INVISIBLE
        }
    }

    private fun startTracking() {
        repository.selectAll().forEach {
            drawGeofenceOnMap(it)
            addGeofence(it.toGeofence())
        }
    }

    private fun stopTracking() {
        geofencingClient.removeGeofences(geofencePendingIntent)
                .addOnCompleteListener {
                    toast(getString(R.string.geofence_removed))
                }
                .addOnFailureListener {
                    toast(getString(R.string.geofence_remove_error))
                }
        circles.forEach { it.remove() }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(geofenses: Geofence) {
        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL
                    or GeofencingRequest.INITIAL_TRIGGER_ENTER
                    or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(listOf(geofenses))
        }.build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnCompleteListener {
                    toast(getString(R.string.geofence_added))
                }
                .addOnFailureListener {
                    toast(getString(R.string.geofence_add_error))
                }
    }

    private fun requestPermissionAccesFineLocaction() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSIONS_REQUEST)
            }
        } else {
            onLocationPermissionGranted()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationPermissionGranted() {
        LocationServices.getFusedLocationProviderClient(this)
                .lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                drawMyLocationOnMap(location)
            }
        }
        startTracking()
    }

    private fun drawMyLocationOnMap(location: Location): GoogleMap? {
        return map?.apply {
            val myLocation = LatLng(location.latitude, location.longitude)
            val markerOptions = MarkerOptions()
                    .position(myLocation)
                    .title(getString(R.string.i_am_here))
            addMarker(markerOptions)
            moveCamera(CameraUpdateFactory.newLatLng(myLocation))
        }
    }

    private fun drawGeofenceOnMap(geofence: LocalGeofence) {
        map?.apply {
            val circle = addCircle(CircleOptions()
                    .center(LatLng(geofence.latitude, geofence.longitude))
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(120, 100, 100, 180))
                    .radius(geofence.radius.toDouble()))
            circles.add(circle)
        }
    }

    private infix fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun onError(text: String, onRetry: (View) -> Unit) {
        Snackbar.make(coordinator_container, text, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(getString(R.string.retry), onRetry)
            setActionTextColor(Color.RED)
            show()
        }
    }
}
