@file:Suppress("DEPRECATION")

package com.example.rencar_pair.presentation.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMapOptions

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
    styleUrl: String = "https://demotiles.maplibre.org/style.json",
    latitude: Double = 41.0082,
    longitude: Double = 28.9784,
    zoom: Double = 12.0,
    markers: List<RenCarMapMarker> = emptyList(),
    onMapCreated: ((org.maplibre.android.maps.MapLibreMap) -> Unit)? = null
) {
    val context = LocalContext.current

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
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    DisposableEffect(Unit) {
        mapView.getMapAsync { map ->
            map.setStyle(styleUrl) {
                onMapCreated?.invoke(map)
            }
        }
        onDispose {
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.getMapAsync { mapboxMap ->
                mapboxMap.clear()
                markers.forEach { marker ->
                    mapboxMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.latitude, marker.longitude))
                            .title(marker.title)
                            .snippet(marker.snippet)
                    )
                }
            }
        }
    )
}
