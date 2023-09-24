package com.example.runningrhino

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context!!))

        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        val map = view.findViewById<MapView>(R.id.mapview)
        map.setUseDataConnection(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val map = view.findViewById<MapView>(R.id.mapview)
        val mapController: IMapController = map.controller

        activity?.runOnUiThread {
            mapController.zoomTo(16, 1)

            val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
            mLocationOverlay.enableMyLocation()
            map.overlays.add(mLocationOverlay)
            mapController.setCenter(mLocationOverlay.myLocation)
        }
    }

}
