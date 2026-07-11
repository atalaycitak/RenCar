package com.example.rencar_pair.presentation.ui.components

import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

val DEFAULT_CENTER: LatLng = LatLng(41.0082, 28.9784)

private val ME_MARKER_COLOR = Color.parseColor("#4285F4")

data class RenCarMapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val text: String,
    val colorHex: String,
    val title: String = "",
    val snippet: String = ""
)

class RencarMapController internal constructor() {
    internal var map: MapLibreMap? = null

    fun animateTo(target: LatLng, zoom: Double = 13.0) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }
}

@Composable
fun rememberRencarMapController(): RencarMapController = remember { RencarMapController() }

@Composable
fun RenCarMap(
    myLocation: LatLng?,
    modifier: Modifier = Modifier,
    initialCenter: LatLng = DEFAULT_CENTER,
    initialZoom: Double = 13.0,
    controller: RencarMapController? = null,
    markers: List<RenCarMapMarker> = emptyList(),
    onMarkerClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(null) }
    }

    var mapAndStyle by remember { mutableStateOf<Pair<MapLibreMap, Style>?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(Unit) {
        mapView.getMapAsync { map ->
            controller?.map = map
            map.cameraPosition = CameraPosition.Builder().target(initialCenter).zoom(initialZoom).build()
            map.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) { loaded ->
                loaded.addSource(GeoJsonSource("me"))
                loaded.addLayer(
                    CircleLayer("me-halo-layer", "me").withProperties(
                        PropertyFactory.circleColor(ME_MARKER_COLOR),
                        PropertyFactory.circleRadius(20f),
                        PropertyFactory.circleOpacity(0.2f),
                        PropertyFactory.circleBlur(0.4f)
                    )
                )
                loaded.addLayer(
                    CircleLayer("me-layer", "me").withProperties(
                        PropertyFactory.circleColor(ME_MARKER_COLOR),
                        PropertyFactory.circleRadius(9f),
                        PropertyFactory.circleStrokeColor(Color.WHITE),
                        PropertyFactory.circleStrokeWidth(3f)
                    )
                )
                mapAndStyle = map to loaded
            }
        }
    }

    // Update me marker when location changes
    LaunchedEffect(mapAndStyle, myLocation) {
        val (_, style) = mapAndStyle ?: return@LaunchedEffect
        updateMe(style, myLocation)
    }

    // Zoom once to the user when map is loaded and location is found
    var hasZoomedToUser by remember { mutableStateOf(false) }
    LaunchedEffect(mapAndStyle, myLocation) {
        if (hasZoomedToUser) return@LaunchedEffect
        val (map, _) = mapAndStyle ?: return@LaunchedEffect
        val location = myLocation ?: return@LaunchedEffect

        hasZoomedToUser = true
        Log.d("MAP", "First zoom -> lat: ${location.latitude}, lng: ${location.longitude}, zoom: 13.0")
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13.0))
    }

    // Update car markers
    LaunchedEffect(mapAndStyle, markers) {
        val (map, _) = mapAndStyle ?: return@LaunchedEffect
        map.clear()
        
        val markerIdMap = mutableMapOf<Marker, String>()
        
        val iconFactory = org.maplibre.android.annotations.IconFactory.getInstance(context)

        markers.forEach { marker ->
            val bitmap = createPriceMarkerBitmap(context, marker.text, marker.colorHex)
            val icon = iconFactory.fromBitmap(bitmap)
            val addedMarker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(marker.latitude, marker.longitude))
                    .title(marker.title)
                    .snippet(marker.snippet)
                    .icon(icon)
            )
            markerIdMap[addedMarker] = marker.id
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

    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun updateMe(style: Style, myLocation: LatLng?) {
    val source = style.getSourceAs<GeoJsonSource>("me") ?: return
    if (myLocation == null) {
        source.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
    } else {
        source.setGeoJson(Point.fromLngLat(myLocation.longitude, myLocation.latitude))
    }
}

private fun createPriceMarkerBitmap(context: android.content.Context, text: String, colorHex: String): android.graphics.Bitmap {
    val scale = context.resources.displayMetrics.density
    
    // Convert dp to px
    val paddingHorizontal = 8f * scale
    val paddingVertical = 6f * scale
    val textSize = 13f * scale
    val cornerRadius = 13f * scale
    val triangleHeight = 6f * scale
    val triangleWidth = 10f * scale
    val shadowRadius = 6f * scale
    val shadowDy = 4f * scale

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    paint.textSize = textSize
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    
    val textWidth = paint.measureText(text)
    
    // Box dimensions
    val boxWidth = textWidth + (paddingHorizontal * 2)
    val boxHeight = textSize + (paddingVertical * 2)

    // Total bitmap dimensions (box + triangle + shadow padding)
    val bitmapWidth = (boxWidth + shadowRadius * 2).toInt()
    val bitmapHeight = (boxHeight + triangleHeight + shadowRadius * 2 + shadowDy).toInt()

    val bitmap = android.graphics.Bitmap.createBitmap(bitmapWidth, bitmapHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Calculate drawing offsets to center within the shadow padding
    val dx = shadowRadius
    val dy = shadowRadius

    // Draw shadow
    paint.color = android.graphics.Color.TRANSPARENT
    val shadowColor = android.graphics.Color.parseColor(colorHex)
    paint.setShadowLayer(shadowRadius, 0f, shadowDy, shadowColor and 0x7FFFFFFF) // 50% alpha shadow

    // Path for the badge (rounded rect + downward triangle)
    val path = android.graphics.Path()
    val rectF = android.graphics.RectF(dx, dy, dx + boxWidth, dy + boxHeight)
    path.addRoundRect(rectF, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
    
    // Triangle at the bottom center
    val centerX = dx + (boxWidth / 2f)
    val triangleTopY = dy + boxHeight - 1f // Slight overlap to avoid gaps
    
    path.moveTo(centerX - (triangleWidth / 2f), triangleTopY)
    path.lineTo(centerX + (triangleWidth / 2f), triangleTopY)
    path.lineTo(centerX, triangleTopY + triangleHeight)
    path.close()

    // Draw solid color path
    paint.color = android.graphics.Color.parseColor(colorHex)
    canvas.drawPath(path, paint)
    
    // Clear shadow for text
    paint.clearShadowLayer()

    // Draw Text
    paint.color = android.graphics.Color.WHITE
    val fontMetrics = paint.fontMetrics
    val textBaselineY = dy + paddingVertical - fontMetrics.ascent
    canvas.drawText(text, dx + paddingHorizontal, textBaselineY, paint)

    return bitmap
}
