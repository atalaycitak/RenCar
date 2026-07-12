@file:Suppress("DEPRECATION")

package com.example.rencar_pair.presentation.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMapOptions

data class RenCarMapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String,
    val markerColor: Int = DEFAULT_MARKER_COLOR,
    val selected: Boolean = false
)

@Composable
fun RenCarMap(
    modifier: Modifier = Modifier,
    styleUrl: String = "https://demotiles.maplibre.org/style.json",
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
    val iconFactory = remember(context) { IconFactory.getInstance(context) }

    DisposableEffect(Unit) {
        MapLibre.getInstance(context)
        onDispose { }
    }

    val mapView = remember {
        val options = MapLibreMapOptions.createFromAttributes(context, null).apply {
            camera(
                org.maplibre.android.camera.CameraPosition.Builder()
                    .target(LatLng(latitude, longitude))
                    .zoom(zoom)
                    .build()
            )
        }
        MapView(context, options).apply {
            onCreate(null)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    DisposableEffect(mapView, styleUrl) {
        mapView.getMapAsync { map ->
            map.setStyle(styleUrl) {
                mapLibreMap = map
                onMapCreated?.invoke(map)
            }
        }
        onDispose { }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var destroyed = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> {
                    destroyed = true
                    mapView.onDestroy()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onStart()
        }
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!destroyed) {
                mapView.onDestroy()
            }
        }
    }

    LaunchedEffect(mapLibreMap, latitude, longitude, zoom) {
        mapLibreMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom),
            CAMERA_ANIMATION_MS
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.getMapAsync { mapboxMap ->
                mapboxMap.clear()
                
                val markerIdMap = mutableMapOf<org.maplibre.android.annotations.Marker, String>()
                
                val iconCache = mutableMapOf<String, Icon>()

                markers.forEach { marker ->
                    val iconKey = "${marker.markerColor}-${marker.selected}"
                    val icon = iconCache.getOrPut(iconKey) {
                        iconFactory.fromBitmap(
                            createVehicleMarkerBitmap(
                                color = marker.markerColor,
                                selected = marker.selected
                            )
                        )
                    }
                    val addedMarker = mapboxMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.latitude, marker.longitude))
                            .title(marker.title)
                            .snippet(marker.snippet)
                            .icon(icon)
                    )
                    markerIdMap[addedMarker] = marker.id
                }

                // If user location is available, add a special marker
                if (userLatitude != null && userLongitude != null) {
                    mapboxMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(userLatitude, userLongitude))
                            .title("Sizin Konumunuz")
                    )
                }

                mapboxMap.setOnMarkerClickListener { clickedMarker ->
                    val id = markerIdMap[clickedMarker]
                    if (id != null) {
                        onMarkerClick?.invoke(id)
                        true // Prevent default InfoWindow
                    } else {
                        // User location marker clicked, allow default behavior (InfoWindow)
                        false
                    }
                }
            }
        }
    )
}

private const val CAMERA_ANIMATION_MS = 750
private const val DEFAULT_MARKER_COLOR = 0xFF0066CC.toInt()

private fun createVehicleMarkerBitmap(
    color: Int,
    selected: Boolean
): Bitmap {
    val size = if (selected) 72 else 56
    val circleRadius = if (selected) 24f else 18f
    val center = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = 0x33000000
    canvas.drawCircle(center, center + 5f, circleRadius + 5f, paint)

    paint.color = color
    val pinPath = Path().apply {
        moveTo(center, size - 8f)
        cubicTo(center - 18f, center + 16f, center - circleRadius, center, center, center)
        cubicTo(center + circleRadius, center, center + 18f, center + 16f, center, size - 8f)
        close()
    }
    canvas.drawPath(pinPath, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = if (selected) 6f else 4f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, circleRadius, paint)

    paint.style = Paint.Style.FILL
    paint.color = color
    canvas.drawCircle(center, center, circleRadius - paint.strokeWidth, paint)

    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = 5f
    paint.style = Paint.Style.STROKE
    paint.strokeCap = Paint.Cap.ROUND
    val carTop = center - 5f
    canvas.drawLine(center - 8f, carTop, center + 8f, carTop, paint)
    canvas.drawLine(center - 12f, center + 5f, center + 12f, center + 5f, paint)
    canvas.drawPoint(center - 8f, center + 10f, paint)
    canvas.drawPoint(center + 8f, center + 10f, paint)

    return bitmap
}
