package slate.com.slatetestapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_geofance.view.*
import slate.com.slatetestapp.repository.entity.LocalGeofence


class AddGeofenceDialog : DialogFragment() {
    companion object {
        const val ADD_GEOFENCE_DIALOG_TAG = "AddGeofenceDialogTag"
    }

    private var callback: AddgeofenceListener? = null

    interface AddgeofenceListener {
        fun addGeofence(localGeofence: LocalGeofence)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
        = inflater.inflate(R.layout.fragment_add_geofance, container, false).apply {
                add_tv.setOnClickListener {
//                    callback?.addGeofence()
                    dismiss()
                }

                reject_tv.setOnClickListener {
                    dismiss()
                }
            }

    private fun validateFields(): Boolean {

        return true
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