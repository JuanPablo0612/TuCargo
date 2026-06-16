package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.juanpablo0612.tucargo.BuildKonfig
import com.juanpablo0612.tucargo.core.location.DriverLocation
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectZero
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.darwin.NSObject
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.map_your_location
// NOTE ON THIS WHOLE FILE: written without an Xcode/Mac toolchain available, so it could
// not be compiled here. The import path below uses "TuCargo" as the group segment because
// composeApp/build.gradle.kts does NOT set an explicit `group` (verified via
// `./gradlew :composeApp:properties`, which prints `group: TuCargo` — Gradle's default,
// matching `rootProject.name` as-is). Do NOT add an explicit `group = ...` to that build
// file to "tidy up" this namespace: doing so changes Compose Multiplatform's generated
// resources package away from `tucargo.composeapp.generated.resources`, which every
// existing screen (TripDetailScreen, TripHistoryScreen, etc.) already imports — confirmed
// by reproducing the resulting "Unresolved reference 'tucargo'" build failure locally.
// Kotlin's SwiftPM-import feature derives the `swiftPMImport.<group>.<module>.<ClassName>`
// namespace for the "GoogleMaps" product declared in the `swiftPMDependencies` block; the
// exact casing/segmentation for a non-dotted group like "TuCargo" is still a best-effort
// guess and must be confirmed once `:composeApp:integrateLinkagePackage` has run on a Mac.
// Named-argument labels on the GoogleMaps ObjC calls below (e.g. `target =`,
// `cameraUpdate =`, `path =`) are also a best-effort match to the SDK's Objective-C
// selectors and need the same on-Mac confirmation.
import swiftPMImport.TuCargo.composeApp.GMSCameraPosition
import swiftPMImport.TuCargo.composeApp.GMSCameraUpdate
import swiftPMImport.TuCargo.composeApp.GMSCoordinateBounds
import swiftPMImport.TuCargo.composeApp.GMSMapView
import swiftPMImport.TuCargo.composeApp.GMSMapViewDelegateProtocol
import swiftPMImport.TuCargo.composeApp.GMSMarker
import swiftPMImport.TuCargo.composeApp.GMSMutablePath
import swiftPMImport.TuCargo.composeApp.GMSServices

private const val ANIMATION_DURATION_MS = 1500
private const val BOUNDS_PADDING_PX = 120.0

// GMSServices.provideAPIKey(_:) must be called exactly once per process, before the
// first GMSMapView is created.
private var mapsApiKeyProvided = false

private fun ensureMapsApiKeyProvided() {
    if (!mapsApiKeyProvided) {
        GMSServices.provideAPIKey(BuildKonfig.GOOGLE_MAPS_IOS_API_KEY)
        mapsApiKeyProvided = true
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapComponent(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    onMapClick: ((latitude: Double, longitude: Double) -> Unit)?,
    driverLocation: DriverLocation?,
    originLatLng: Pair<Double, Double>?,
    destinationLatLng: Pair<Double, Double>?,
) {
    // Animatable lat/lng for smooth driver marker movement (mirrors the Android actual).
    val animLat = remember { Animatable(driverLocation?.lat?.toFloat() ?: latitude.toFloat()) }
    val animLng = remember { Animatable(driverLocation?.lng?.toFloat() ?: longitude.toFloat()) }

    LaunchedEffect(driverLocation) {
        val loc = driverLocation ?: return@LaunchedEffect
        launch { animLat.animateTo(loc.lat.toFloat(), tween(ANIMATION_DURATION_MS)) }
        animLng.animateTo(loc.lng.toFloat(), tween(ANIMATION_DURATION_MS))
    }

    var hasSetInitialBounds by remember { mutableStateOf(false) }
    val yourLocationTitle = stringResource(Res.string.map_your_location)

    // Markers are created once and mutated imperatively from `update`, since GMSMapView
    // (unlike Compose's Android GoogleMap) has no declarative marker API.
    val markers = remember { MapMarkers() }

    UIKitView(
        factory = {
            ensureMapsApiKeyProvided()
            val camera = GMSCameraPosition.cameraWithLatitude(
                latitude = latitude,
                longitude = longitude,
                zoom = zoom,
            )
            val mapView = GMSMapView(frame = CGRectZero.readValue(), camera = camera)
            mapView.settings.zoomGestures = true
            mapView.delegate = object : NSObject(), GMSMapViewDelegateProtocol {
                override fun mapView(mapView: GMSMapView, didTapAtCoordinate: CValue<CLLocationCoordinate2D>) {
                    // `latitude`/`longitude` here resolve to the tapped coordinate's
                    // fields via the useContents receiver, not this function's params.
                    didTapAtCoordinate.useContents { onMapClick?.invoke(latitude, longitude) }
                }
            }
            mapView
        },
        modifier = modifier,
        update = { mapView ->
            val driverLat = animLat.value.toDouble()
            val driverLng = animLng.value.toDouble()

            if (driverLocation != null) {
                markers.staticMarker?.map = null
                markers.staticMarker = null

                val driverMarker = markers.driver ?: GMSMarker().apply {
                    title = "Conductor"
                    map = mapView
                }.also { markers.driver = it }
                driverMarker.position = CLLocationCoordinate2DMake(driverLat, driverLng)

                originLatLng?.let { (lat, lng) ->
                    val marker = markers.origin ?: GMSMarker().apply {
                        title = "Origen"
                        map = mapView
                    }.also { markers.origin = it }
                    marker.position = CLLocationCoordinate2DMake(lat, lng)
                }
                destinationLatLng?.let { (lat, lng) ->
                    val marker = markers.destination ?: GMSMarker().apply {
                        title = "Destino"
                        map = mapView
                    }.also { markers.destination = it }
                    marker.position = CLLocationCoordinate2DMake(lat, lng)
                }

                if (!hasSetInitialBounds && originLatLng != null && destinationLatLng != null) {
                    val path = GMSMutablePath().apply {
                        addCoordinate(CLLocationCoordinate2DMake(driverLat, driverLng))
                        addCoordinate(CLLocationCoordinate2DMake(originLatLng.first, originLatLng.second))
                        addCoordinate(CLLocationCoordinate2DMake(destinationLatLng.first, destinationLatLng.second))
                    }
                    val bounds = GMSCoordinateBounds(path = path)
                    mapView.animateWithCameraUpdate(
                        cameraUpdate = GMSCameraUpdate.fitBounds(bounds = bounds, withPadding = BOUNDS_PADDING_PX)
                    )
                    hasSetInitialBounds = true
                }
            } else {
                markers.driver?.map = null
                markers.driver = null
                markers.origin?.map = null
                markers.origin = null
                markers.destination?.map = null
                markers.destination = null

                val marker = markers.staticMarker ?: GMSMarker().apply {
                    title = yourLocationTitle
                    map = mapView
                }.also { markers.staticMarker = it }
                marker.position = CLLocationCoordinate2DMake(latitude, longitude)

                if (!hasSetInitialBounds) {
                    mapView.animateWithCameraUpdate(
                        cameraUpdate = GMSCameraUpdate.setTarget(
                            target = CLLocationCoordinate2DMake(latitude, longitude),
                            zoom = zoom,
                        )
                    )
                    hasSetInitialBounds = true
                }
            }
        },
    )
}

private class MapMarkers {
    var driver: GMSMarker? = null
    var staticMarker: GMSMarker? = null
    var origin: GMSMarker? = null
    var destination: GMSMarker? = null
}
