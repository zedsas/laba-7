package com.example.a7laba

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private const val POSTOMAT_LOCATION_LAT = 55.354993
        private const val POSTOMAT_LOCATION_LON = 86.085805
        private const val DEFAULT_ZOOM = 15.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("c01811d7-3f02-40f6-aa5a-6b50d39df857")
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupButtons()

        checkLocationPermissionWithRationale()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnZoomToKemerovo).apply {
            text = "Почтомат"
            setOnClickListener {
                moveCameraToLocation(
                    Point(POSTOMAT_LOCATION_LAT, POSTOMAT_LOCATION_LON),
                    DEFAULT_ZOOM
                )
            }
        }

        findViewById<Button>(R.id.btnZoomToCurrentLocation).setOnClickListener {
            checkLocationPermissionWithRationale()
        }
    }

    private fun moveCameraToLocation(point: Point, zoom: Float) {
        val cameraPosition = CameraPosition(point, zoom, 0.0f, 0.0f)
        mapView.map.move(cameraPosition)
    }

    private fun checkLocationPermissionWithRationale() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionExplanation()
            }

            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun showPermissionExplanation() {
        Toast.makeText(
            this,
            "Приложение запрашивает доступ к вашему местоположению для отображения на карте. Разрешение можно изменить в настройках устройства.",
            Toast.LENGTH_LONG
        ).show()

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        moveCameraToLocation(
                            Point(location.latitude, location.longitude),
                            DEFAULT_ZOOM
                        )
                    } else {
                        showLocationError("Не удалось определить ваше местоположение")
                    }
                }
                .addOnFailureListener { e ->
                    showLocationError("Ошибка получения местоположения: ${e.localizedMessage}")
                }
        } else {
            showLocationError("Нет разрешения на доступ к местоположению")
        }
    }

    private fun showLocationError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Без разрешения на доступ к местоположению функция недоступна",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}