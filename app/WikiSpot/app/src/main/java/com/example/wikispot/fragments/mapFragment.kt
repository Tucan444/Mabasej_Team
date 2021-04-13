package com.example.wikispot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.wikispot.MapManagement
import com.example.wikispot.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class mapFragment : Fragment() {

    val args: mapFragmentArgs by navArgs()
    private var loadFromMapManager = true
    var location: LatLng? = null
    var markerTitle: String? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /*val pb = LatLng(49.11274928646463, 18.443442353021045)
        googleMap.addMarker(MarkerOptions().position(pb).title("Povazska Bystrica"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(pb))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pb, 16.0f)) */

        try {
            location = args.location
            markerTitle = args.markerTitle
            loadFromMapManager = false
        } catch (e: Throwable) { println("[debug] Exception in Map Fragment while getting args: $e") }

        if (loadFromMapManager) {
            googleMap.addMarker(MarkerOptions().position(MapManagement.connectedServerPosition!!).title(MapManagement.connectedServerTitle))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapManagement.connectedServerPosition, 16.0F))
        } else {
            googleMap.addMarker(MarkerOptions().position(location!!).title(markerTitle))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0F))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}