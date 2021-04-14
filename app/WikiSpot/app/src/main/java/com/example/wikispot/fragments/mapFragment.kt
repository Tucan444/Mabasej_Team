package com.example.wikispot.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.wikispot.CustomBackstackVariables
import com.example.wikispot.MapManagement
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.modelsForAdapters.PlaceSupplier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import java.time.Clock

class mapFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    val args: mapFragmentArgs by navArgs()
    private var loadFromMapManager = true
    private var loadLastCoordinates = false
    var location: LatLng? = null
    var lastClickedMarkerTitle = ""

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
            loadLastCoordinates = args.loadLastCoordinates
            loadFromMapManager = false
        } catch (e: Throwable) { println("[debug] Exception in Map Fragment while getting args: $e") }

        if (loadFromMapManager) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapManagement.connectedServerPosition, 15.0F))
        } else if (loadLastCoordinates){
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapManagement.lastCoordinates, 15F))
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0F))
        }

        // loading other markers
        for (n in PlaceSupplier.places.indices) {
            val coordinates = PlaceSupplier.places[n]?.location!!.split(",")
            googleMap.addMarker(MarkerOptions().position(LatLng(coordinates[0].toDouble(), coordinates[1].toDouble())).title(PlaceSupplier.places[n]?.title))
        }

        googleMap.setOnMarkerClickListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.let {
            if (marker.title == lastClickedMarkerTitle) {
                for (n in PlaceSupplier.places.indices) {
                    if (marker.title == PlaceSupplier.places[n]!!.title) {
                        CustomBackstackVariables.infoFragmentBackDestination = "mapFragment"
                        MapManagement.lastCoordinates = marker.position
                        ServerManagement.selectedServerId = PlaceSupplier.places[n]!!.id!!
                        val action = mapFragmentDirections.mapFragmentToInfoFragment()
                        Navigation.findNavController(navControllerView).navigate(action)
                    }
                }
            }
            lastClickedMarkerTitle = marker.title
            println("[debug] marker title ${marker.title}")
            println(System.currentTimeMillis())
        }
        return false
    }
}
