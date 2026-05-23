package com.example.uni_lift.core.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    pickupLat: Double = 10.2897,
    pickupLng: Double = 123.8628,
    dropoffLat: Double? = null,
    dropoffLng: Double? = null,
    driverLat: Double? = null,
    driverLng: Double? = null
) {
    val context = LocalContext.current
    val mapView = remember { createMapView(context) }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()
            val pickupPoint = GeoPoint(pickupLat, pickupLng)

            // Pickup marker (default blue)
            val pickupMarker = Marker(map).apply {
                position = pickupPoint
                title = "Pickup"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            map.overlays.add(pickupMarker)

            // Dropoff marker (default orange/red)
            if (dropoffLat != null && dropoffLng != null) {
                val dropoffMarker = Marker(map).apply {
                    position = GeoPoint(dropoffLat, dropoffLng)
                    title = "Destination"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(dropoffMarker)

                // Auto-fit to show both pickup and dropoff
                val points = mutableListOf(pickupPoint, GeoPoint(dropoffLat, dropoffLng))
                driverLat?.let { dlat -> driverLng?.let { dlng -> points.add(GeoPoint(dlat, dlng)) } }
                val bounds = BoundingBox.fromGeoPoints(points)
                map.zoomToBoundingBox(bounds.increaseByScale(1.4f), false)
            } else {
                map.controller.setCenter(pickupPoint)
                map.controller.setZoom(16.0)
            }

            // Driver marker (shown on top of others)
            if (driverLat != null && driverLng != null) {
                val driverMarker = Marker(map).apply {
                    position = GeoPoint(driverLat, driverLng)
                    title = "Driver"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(driverMarker)
            }

            map.invalidate()
        }
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidBasePath = context.getExternalFilesDir(null) ?: context.cacheDir
        osmdroidTileCache = java.io.File(osmdroidBasePath, "osm_tiles")
    }
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(16.0)
        controller.setCenter(GeoPoint(10.2897, 123.8628))
    }
}
