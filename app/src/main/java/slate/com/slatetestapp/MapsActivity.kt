package slate.com.slatetestapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.support.design.widget.Snackbar
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val LOCATION_PERMISSIONS_REQUEST = 101
        private const val GOOGLE_API_AVAILABILITY_REQUEST = 101
    }

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        checkAvailabilityOfPlayServices()
        requestPermissionAccesFineLocaction()
    }

    private fun checkAvailabilityOfPlayServices() {
        val availability = GoogleApiAvailability.getInstance()
        val errorCode = availability.isGooglePlayServicesAvailable(this)
        if (errorCode != ConnectionResult.SUCCESS) {
            availability.getErrorDialog(this, errorCode, GOOGLE_API_AVAILABILITY_REQUEST)
        }
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
                map?.apply {
                    val myLocation = LatLng(location.latitude, location.longitude)
                    val markerOptions = MarkerOptions()
                            .position(myLocation)
                            .title(getString(R.string.i_am_here))
                    addMarker(markerOptions)
                    moveCamera(CameraUpdateFactory.newLatLng(myLocation))
                }
            }
        }
    }

    private fun onError(text: String, onRetry: (View) -> Unit) {
        Snackbar.make(findViewById(R.id.coordinator_container), text, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(getString(R.string.retry), onRetry)
            setActionTextColor(Color.RED)
            show()
        }
    }
}
