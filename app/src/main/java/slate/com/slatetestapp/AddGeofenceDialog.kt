package slate.com.slatetestapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_add_geofance.view.*
import slate.com.slatetestapp.repository.entity.LocalGeofence
import java.util.regex.Pattern


class AddGeofenceDialog : DialogFragment() {
    companion object {
        const val ADD_GEOFENCE_DIALOG_TAG = "AddGeofenceDialogTag"

        const val LATITUDE_REGEXP = "^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))\$"
        const val LONGITUDE_REGEXP = "^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))\$"
    }

    private var callback: AddgeofenceListener? = null

    interface AddgeofenceListener {
        fun onNewGeofence(localGeofence: LocalGeofence)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
        = inflater.inflate(R.layout.fragment_add_geofance, container, false).apply {
            add_tv.setOnClickListener {

                val ssid = ssid_et.text.toString()
                if (TextUtils.isEmpty(ssid) || ssid.contains(" ")) {
                    Toast.makeText(context, context.getString(R.string.wrong_ssid), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val latitudeStr = latitude_et.text.toString()
                val latMatcher = Pattern.compile(LATITUDE_REGEXP)
                        .matcher(latitudeStr)
                if (!latMatcher.matches()) {
                    Toast.makeText(context, context.getString(R.string.wrong_latitude), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val longitudeStr = longtitude_et.text.toString()
                val longMatcher = Pattern.compile(LONGITUDE_REGEXP)
                        .matcher(longitudeStr)
                if (!longMatcher.matches()) {
                    Toast.makeText(context, context.getString(R.string.wrong_logitude), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val radiusString = radius_ev.text.toString()
                val radius = if (radiusString.isNullOrBlank()) (-1).toFloat() else Integer.parseInt(radiusString).toFloat()
                if (radius <= 99 || radius > 30000) {
                    Toast.makeText(context, context.getString(R.string.wrong_radius), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                callback?.onNewGeofence(LocalGeofence(ssid.trim(), latitudeStr.toDouble(), longitudeStr.toDouble(), radius))
                dismiss()
            }

            reject_tv.setOnClickListener {
                dismiss()
            }
        }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (activity is AddgeofenceListener) {
            callback = activity as AddgeofenceListener
        }
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }
}