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
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    markers: List<RenCarMapMarker> = emptyList(),
    onMarkerClick: ((String) -> Unit)? = null,
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
                
                val markerIdMap = mutableMapOf<org.maplibre.android.annotations.Marker, String>()
                
                markers.forEach { marker ->
                    val addedMarker = mapboxMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(marker.latitude, marker.longitude))
                            .title(marker.title)
                            .snippet(marker.snippet)
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
                    
                    // Animate camera to user location if it changed recently
                    // (For simplicity in this component, we just set the camera)
                    mapboxMap.animateCamera(
                        org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                            LatLng(userLatitude, userLongitude), zoom
                        ),
                        1000
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
