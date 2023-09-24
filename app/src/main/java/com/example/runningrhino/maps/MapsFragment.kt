package com.example.runningrhino.maps

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.example.runningrhino.BuildConfig
import com.example.runningrhino.R
import com.example.runningrhino.tracking.TrackingViewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapsFragment : Fragment() {

    private lateinit var mMapView: MapView
    private lateinit var mMapController: IMapController

    private val sharedViewModel: TrackingViewModel by activityViewModels()
    private var lastLine: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context!!))

        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        mMapView = view.findViewById(R.id.mapview)
        mMapView.setUseDataConnection(true)
        mMapView.setTileSource(TileSourceFactory.MAPNIK)
        mMapView.setMultiTouchControls(true)
        mMapController = mMapView.controller

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMapController.zoomTo(18, 1)

        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mMapView)
        mLocationOverlay.enableMyLocation()
        mMapView.overlays.add(mLocationOverlay)
        mMapController.setCenter(GeoPoint(50.0, 10.0))
        mMapController.animateTo(GeoPoint(50.0, 10.0))

        sharedViewModel.lineList.observe(viewLifecycleOwner) {
            Log.d("GPS", "lineListObserve")
            val length = sharedViewModel.getLineListLen()
            if (length > 0 && length > lastLine) {
                val list = sharedViewModel.getLineList()
                for (i in lastLine until length) {
                    drawPolyLine(list[i])
                }
                lastLine = length
            }
        }

        //drawTestPolyline()

    }

    private fun drawPolyLine(line: Polyline) {
        Log.d("GPS", "drawPolyLine")
        mMapView.overlays.add(line)
        line.outlinePaint.set(Paint(Color.CYAN))
        line.outlinePaint.strokeWidth = 5F
    }

    private fun drawTestPolyline() {

        //TODO:
        val geoPoints =
            arrayListOf(GeoPoint(50.0, 10.0), GeoPoint(51.0, 10.0), GeoPoint(52.0, 10.0))
        val line = Polyline()

        line.setPoints(geoPoints)
        mMapView.overlays.add(line)
        line.outlinePaint.set(Paint(Color.CYAN))
        line.outlinePaint.strokeWidth = 5F
    }

}
