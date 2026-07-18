package com.example.rencar_pair.presentation.ui.components

import android.graphics.Color
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
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.LineString
import org.maplibre.android.style.layers.LineLayer

val DEFAULT_CENTER: LatLng = LatLng(41.0082, 28.9784)

private val ME_MARKER_COLOR = Color.parseColor("#4285F4")

data class RenCarMapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val text: String,
    val colorHex: String,
    val title: String = "",
    val snippet: String = "",
    val selected: Boolean = false
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
    routePoints: List<LatLng> = emptyList(),
    vehicleLocation: LatLng? = null,
    onMarkerClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapLibre.getInstance(context)
        val options = MapLibreMapOptions()
            .textureMode(true)
            .translucentTextureSurface(false)
        options.renderSurfaceOnTop(false)
        MapView(context, options).apply { onCreate(null) }
    }

    var mapAndStyle by remember { mutableStateOf<Pair<MapLibreMap, Style>?>(null) }

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
                
                // Route layer
                loaded.addSource(GeoJsonSource("route"))
                loaded.addLayerBelow(
                    LineLayer("route-layer", "route").withProperties(
                        PropertyFactory.lineColor(Color.parseColor("#1976D2")),
                        PropertyFactory.lineWidth(4.0f),
                        PropertyFactory.lineOpacity(0.82f),
                        PropertyFactory.lineDasharray(arrayOf(1.4f, 1.2f)),
                        PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND)
                    ), "me-halo-layer"
                )

                // Vehicle layer
                loaded.addSource(GeoJsonSource("vehicle"))
                loaded.addLayer(
                    CircleLayer("vehicle-layer", "vehicle").withProperties(
                        PropertyFactory.circleColor(Color.parseColor("#1976D2")),
                        PropertyFactory.circleRadius(10f),
                        PropertyFactory.circleStrokeColor(Color.WHITE),
                        PropertyFactory.circleStrokeWidth(3f)
                    )
                )
                
                mapAndStyle = map to loaded
            }
        }
    }

    LaunchedEffect(mapAndStyle, initialCenter, initialZoom) {
        val (map, _) = mapAndStyle ?: return@LaunchedEffect
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(initialCenter, initialZoom))
    }

    LaunchedEffect(mapAndStyle, myLocation) {
        val (_, style) = mapAndStyle ?: return@LaunchedEffect
        updateMe(style, myLocation)
    }

    var hasZoomedToUser by remember { mutableStateOf(false) }
    LaunchedEffect(mapAndStyle, myLocation) {
        if (hasZoomedToUser) return@LaunchedEffect
        val (map, _) = mapAndStyle ?: return@LaunchedEffect
        val location = myLocation ?: return@LaunchedEffect

        hasZoomedToUser = true
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13.0))
    }

    LaunchedEffect(mapAndStyle, routePoints) {
        val (_, style) = mapAndStyle ?: return@LaunchedEffect
        val source = style.getSourceAs<GeoJsonSource>("route") ?: return@LaunchedEffect
        if (routePoints.isEmpty()) {
            source.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        } else {
            val points = routePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
            source.setGeoJson(LineString.fromLngLats(points))
        }
    }

    var hasZoomedToVehicle by remember { mutableStateOf(false) }
    LaunchedEffect(mapAndStyle, vehicleLocation) {
        val (map, style) = mapAndStyle ?: return@LaunchedEffect
        val source = style.getSourceAs<GeoJsonSource>("vehicle") ?: return@LaunchedEffect
        if (vehicleLocation == null) {
            source.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        } else {
            source.setGeoJson(Point.fromLngLat(vehicleLocation.longitude, vehicleLocation.latitude))
            if (!hasZoomedToVehicle) {
                hasZoomedToVehicle = true
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(vehicleLocation, 15.0))
            }
        }
    }

    LaunchedEffect(mapAndStyle, markers) {
        val (map, _) = mapAndStyle ?: return@LaunchedEffect
        map.clear()

        val markerIdMap = mutableMapOf<Marker, String>()
        val iconFactory = org.maplibre.android.annotations.IconFactory.getInstance(context)

        markers.forEach { marker ->
            val bitmap = createPriceMarkerBitmap(
                context = context,
                text = marker.text,
                colorHex = marker.colorHex,
                selected = marker.selected
            )
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

private fun createPriceMarkerBitmap(
    context: android.content.Context,
    text: String,
    colorHex: String,
    selected: Boolean
): android.graphics.Bitmap {
    val scale = context.resources.displayMetrics.density

    val paddingHorizontal = if (selected) 11f * scale else 8f * scale
    val paddingVertical = if (selected) 8f * scale else 6f * scale
    val textSize = if (selected) 14f * scale else 13f * scale
    val cornerRadius = if (selected) 16f * scale else 13f * scale
    val triangleHeight = if (selected) 8f * scale else 6f * scale
    val triangleWidth = if (selected) 13f * scale else 10f * scale
    val shadowRadius = if (selected) 9f * scale else 6f * scale
    val shadowDy = if (selected) 5f * scale else 4f * scale
    val strokeWidth = if (selected) 3f * scale else 0f

    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    paint.textSize = textSize
    paint.typeface = android.graphics.Typeface.create(
        android.graphics.Typeface.DEFAULT,
        android.graphics.Typeface.BOLD
    )

    val textWidth = paint.measureText(text)
    val boxWidth = textWidth + (paddingHorizontal * 2)
    val boxHeight = textSize + (paddingVertical * 2)
    val bitmapWidth = (boxWidth + shadowRadius * 2 + strokeWidth * 2).toInt()
    val bitmapHeight = (boxHeight + triangleHeight + shadowRadius * 2 + shadowDy + strokeWidth * 2).toInt()

    val bitmap = android.graphics.Bitmap.createBitmap(
        bitmapWidth,
        bitmapHeight,
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)

    val dx = shadowRadius + strokeWidth
    val dy = shadowRadius + strokeWidth

    paint.color = android.graphics.Color.TRANSPARENT
    val shadowColor = android.graphics.Color.parseColor(colorHex)
    paint.setShadowLayer(shadowRadius, 0f, shadowDy, shadowColor and 0x7FFFFFFF)

    val path = android.graphics.Path()
    val rectF = android.graphics.RectF(dx, dy, dx + boxWidth, dy + boxHeight)
    path.addRoundRect(rectF, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)

    val centerX = dx + (boxWidth / 2f)
    val triangleTopY = dy + boxHeight - 1f

    path.moveTo(centerX - (triangleWidth / 2f), triangleTopY)
    path.lineTo(centerX + (triangleWidth / 2f), triangleTopY)
    path.lineTo(centerX, triangleTopY + triangleHeight)
    path.close()

    paint.color = android.graphics.Color.parseColor(colorHex)
    paint.style = android.graphics.Paint.Style.FILL
    canvas.drawPath(path, paint)
    paint.clearShadowLayer()

    if (selected) {
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = android.graphics.Color.WHITE
        canvas.drawPath(path, paint)
    }

    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    val fontMetrics = paint.fontMetrics
    val textBaselineY = dy + paddingVertical - fontMetrics.ascent
    canvas.drawText(text, dx + paddingHorizontal, textBaselineY, paint)

    return bitmap
}
