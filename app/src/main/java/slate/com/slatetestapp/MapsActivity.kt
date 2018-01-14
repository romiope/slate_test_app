package slate.com.slatetestapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapsActivity"
        private const val LOCATION_PERMISSIONS_REQUEST = 101
        private const val GOOGLE_API_AVAILABILITY_REQUEST = 101
    }

    private lateinit var map: GoogleMap

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
                onError()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        val sydney = LatLng(-34.0, 151.0)
//        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationPermissionGranted()
                } else {
                    onError()
                }
            }
        }
    }

    private fun requestPermissionAccesFineLocaction() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSIONS_REQUEST)
        } else {
            onLocationPermissionGranted()
        }
    }

    private fun onLocationPermissionGranted() {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private fun onError() {
        TODO("not implemented")
    }
}
