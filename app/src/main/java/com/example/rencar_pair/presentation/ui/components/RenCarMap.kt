@file:Suppress("DEPRECATION")

package com.example.rencar_pair.presentation.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

data class RenCarMapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String
)

@Composable
fun RenCarMap(
    modifier: Modifier = Modifier,
    styleJson: String = RenCarMapDefaults.OsmRasterStyleJson,
    latitude: Double = 41.0082,
    longitude: Double = 28.9784,
    zoom: Double = 12.0,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    markers: List<RenCarMapMarker> = emptyList(),
    onMarkerClick: ((String) -> Unit)? = null,
    onMapCreated: ((org.maplibre.android.maps.MapLibreMap) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var lastCameraTarget by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val mapView = remember {
        MapLibre.getInstance(context)
        val options = MapLibreMapOptions.createFromAttributes(context, null).apply {
            camera(
                CameraPosition.Builder()
                    .target(LatLng(latitude, longitude))
                    .zoom(zoom)
                    .build()
            )
        }
        MapView(context, options).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            onCreate(null)
        }
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        var destroyed = false

        fun destroyMapView() {
            if (!destroyed) {
                mapView.onDestroy()
                destroyed = true
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> destroyMapView()
                else -> Unit
            }
        }

        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }

        onDispose {
            lifecycle.removeObserver(observer)
            destroyMapView()
        }
    }

    DisposableEffect(mapView, styleJson) {
        mapView.getMapAsync { map ->
            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                style.ensureUserLocationLayer()
                mapLibreMap = map
                onMapCreated?.invoke(map)
            }
        }
        onDispose { mapLibreMap = null }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = {
            mapLibreMap?.let { map ->
                map.clear()

                val markerIdMap = mutableMapOf<org.maplibre.android.annotations.Marker, String>()

                markers.forEach { marker ->
                    val addedMarker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.latitude, marker.longitude))
                            .title(marker.title)
                            .snippet(marker.snippet)
                    )
                    markerIdMap[addedMarker] = marker.id
                }

                map.getStyle { style ->
                    style.ensureUserLocationLayer()
                    style.updateUserLocation(userLatitude, userLongitude)
                }

                val cameraLatLng = if (userLatitude != null && userLongitude != null) {
                    LatLng(userLatitude, userLongitude)
                } else {
                    LatLng(latitude, longitude)
                }
                val cameraTarget = cameraLatLng.latitude to cameraLatLng.longitude
                if (lastCameraTarget != cameraTarget) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            cameraLatLng,
                            zoom
                        ),
                        CameraAnimationDurationMs
                    )
                    lastCameraTarget = cameraTarget
                }

                map.setOnMarkerClickListener { clickedMarker ->
                    val id = markerIdMap[clickedMarker]
                    if (id != null) {
                        onMarkerClick?.invoke(id)
                        true
                    } else {
                        false
                    }
                }
            }
        }
    )
}

object RenCarMapDefaults {
    const val DefaultLatitude: Double = 41.0082
    const val DefaultLongitude: Double = 28.9784

    const val OsmRasterStyleJson: String =
        """{"version":8,"sources":{"osm":{"type":"raster","tiles":["https://a.tile.openstreetmap.org/{z}/{x}/{y}.png","https://b.tile.openstreetmap.org/{z}/{x}/{y}.png","https://c.tile.openstreetmap.org/{z}/{x}/{y}.png"],"tileSize":256,"attribution":"OpenStreetMap contributors"}},"layers":[{"id":"osm-tiles","type":"raster","source":"osm","minzoom":0,"maxzoom":19}]}"""
}

private const val UserLocationSourceId = "rencar-user-location-source"
private const val UserLocationLayerId = "rencar-user-location-layer"
private const val CameraAnimationDurationMs = 700

private fun Style.ensureUserLocationLayer() {
    if (getSource(UserLocationSourceId) == null) {
        addSource(GeoJsonSource(UserLocationSourceId, emptyUserLocationFeatureCollection()))
    }
    if (getLayer(UserLocationLayerId) == null) {
        addLayer(
            CircleLayer(UserLocationLayerId, UserLocationSourceId).withProperties(
                circleColor("#1E88E5"),
                circleRadius(8f),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(3f)
            )
        )
    }
}

private fun Style.updateUserLocation(latitude: Double?, longitude: Double?) {
    val source = getSource(UserLocationSourceId) as? GeoJsonSource ?: return
    val featureCollection = if (latitude != null && longitude != null) {
        FeatureCollection.fromFeature(
            Feature.fromGeometry(Point.fromLngLat(longitude, latitude))
        )
    } else {
        emptyUserLocationFeatureCollection()
    }
    source.setGeoJson(featureCollection)
}

private fun emptyUserLocationFeatureCollection(): FeatureCollection {
    return FeatureCollection.fromFeatures(emptyArray<Feature>())
}
