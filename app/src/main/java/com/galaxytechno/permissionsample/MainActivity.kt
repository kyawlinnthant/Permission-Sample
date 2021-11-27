package com.galaxytechno.permissionsample

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {


    private lateinit var layout: View

    companion object {
        private const val CAMERA_REQUEST = 1
        private const val GALLERY_REQUEST = 2
        private const val CONTACT_REQUEST = 3
        private const val FINE_LOCATION_REQUEST = 4
        private const val BACKGROUND_LOCATION_REQUEST = 5

        private const val PERMISSION_CAMERA = Manifest.permission.CAMERA
        private const val PERMISSION_READ = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val PERMISSION_CONTACT = Manifest.permission.READ_CONTACTS
        private const val PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

        @RequiresApi(Build.VERSION_CODES.Q)
        private const val PERMISSION_BACKGROUND_LOCATION =
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Toast.makeText(
                    this@MainActivity,
                    "Updated Location: $location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val cameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            it?.let {
                if (it.resultCode == Activity.RESULT_OK) {

                    //todo some ui in this
                }

            }
        }

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.let {
                if (it.resultCode == Activity.RESULT_OK) {
                    //todo some ui
                }

            }
        }

    private val contactResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.let {
                if (it.resultCode == Activity.RESULT_OK) {
                    //todo ui
                }
            }
        }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        initLocation()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun init() {
        layout = findViewById(R.id.layout)
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnContact = findViewById<Button>(R.id.btnContact)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val btnLocation = findViewById<Button>(R.id.btnLocation)

        val imgCamera = findViewById<ImageView>(R.id.imgCamera)
        val imgGallery = findViewById<ImageView>(R.id.imgGallery)
        val tvCamera = findViewById<TextView>(R.id.tvCamera)
        val tvGallery = findViewById<TextView>(R.id.tvGallery)
        val tvContact = findViewById<TextView>(R.id.tvContact)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)


        btnCamera.setOnClickListener {
            checkCameraPermission()
        }
        btnGallery.setOnClickListener {
            checkGalleryPermission()
        }
        btnContact.setOnClickListener {
            checkContactPermission()
        }
        btnLocation.setOnClickListener {
            checkLocationPermission()
        }

    }

    private fun initLocation() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //below M, auto granted
            PackageManager.PERMISSION_GRANTED
        }

        val grantedCamera = ActivityCompat.checkSelfPermission(this, PERMISSION_CAMERA)

        if (grantedCamera == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(layout, "Camera is granted", Snackbar.LENGTH_INDEFINITE).show()
            //todo start go Intent to Camera
        } else {
            requestCameraPermission()
        }
    }

    private fun checkGalleryPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            PackageManager.PERMISSION_GRANTED
        }
        val galleryReadPermission =
            ActivityCompat.checkSelfPermission(this, PERMISSION_READ)

        if (galleryReadPermission == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(layout, "Gallery is granted", Snackbar.LENGTH_INDEFINITE).show()
            //todo start go Intent to Gallery
        } else requestGalleryPermission()
    }

    private fun checkContactPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) PackageManager.PERMISSION_GRANTED
        val contactPermission = ActivityCompat.checkSelfPermission(this, PERMISSION_CONTACT)
        if (contactPermission == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(layout, "Contact is granted", Snackbar.LENGTH_INDEFINITE).show()
            //todo start go Intent to Contact
        } else requestContactPermission()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) PackageManager.PERMISSION_GRANTED
        val accessLocationPermission =
            ActivityCompat.checkSelfPermission(this, PERMISSION_FINE_LOCATION)
        if (accessLocationPermission == PackageManager.PERMISSION_GRANTED) {
            //todo : From android 10 (API 30) background location must be requested separately.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Snackbar.make(layout, "Fine Location is granted", Snackbar.LENGTH_INDEFINITE)
                    .show()
                //todo start access the location
                getUpdateLocation()
            } else checkBackgroundLocation()

        } else requestFineLocationPermission()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkBackgroundLocation() {
        val backgroundPermission =
            ActivityCompat.checkSelfPermission(this, PERMISSION_BACKGROUND_LOCATION)
        if (backgroundPermission == PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(layout, "Background Location is granted", Snackbar.LENGTH_INDEFINITE)
                .show()
            //todo start trace the location
            getUpdateLocation()
        } else requestBackgroundLocationPermission()
    }

    private fun requestCameraPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_CAMERA)) {
            Snackbar
                .make(
                    layout,
                    "Camera Permission is required to capture the pictures!!",
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(android.R.string.ok) {
                    requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
                }.show()


        } else {
            Snackbar
                .make(
                    layout,
                    "Permission is not available, Fuck Off don't use this Feature!!!",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        }
    }

    private fun requestGalleryPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                PERMISSION_READ
            )
        ) {
            Snackbar
                .make(
                    layout,
                    "Gallery Permission is required to pick up the pictures!!",
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(android.R.string.ok) {
                    requestPermissionsCompat(arrayOf(PERMISSION_READ), GALLERY_REQUEST)
                }.show()
        } else {

            Snackbar
                .make(
                    layout,
                    "Gallery Permission is not available, Fuck Off don't use this Feature!!!",
                    Snackbar.LENGTH_INDEFINITE
                ).show()

            requestPermissionsCompat(arrayOf(PERMISSION_READ), GALLERY_REQUEST)

        }
    }

    private fun requestContactPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, PERMISSION_CONTACT
            )
        ) {
            Snackbar
                .make(
                    layout,
                    "Contact Permission is required to pick up phone contacts!!",
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(android.R.string.ok) {
                    requestPermissionsCompat(arrayOf(PERMISSION_CONTACT), CONTACT_REQUEST)
                }.show()
        } else {
            Snackbar
                .make(
                    layout,
                    "Contact Permission is not available, Fuck Off don't use this Feature!!!",
                    Snackbar.LENGTH_INDEFINITE
                ).show()

            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(PERMISSION_CONTACT), CONTACT_REQUEST)
        }
    }

    private fun requestFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_FINE_LOCATION)) {
            Snackbar
                .make(
                    layout,
                    "Fine Location Permission is required !!!",
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(android.R.string.ok) {
                    requestPermissionsCompat(
                        arrayOf(PERMISSION_FINE_LOCATION),
                        FINE_LOCATION_REQUEST
                    )
                }.show()
        } else {
            Snackbar
                .make(
                    layout,
                    "Fine Location Permission is not available, Fuck Off don't use this Feature!!!",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            requestPermissionsCompat(arrayOf(PERMISSION_FINE_LOCATION), FINE_LOCATION_REQUEST)
        }
    }

    private fun requestBackgroundLocationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    PERMISSION_BACKGROUND_LOCATION
                )
            ) {
                Snackbar
                    .make(
                        layout,
                        "Background Location Permission is required!!",
                        Snackbar.LENGTH_INDEFINITE
                    )
                    .setAction(android.R.string.ok) {
                        requestPermissionsCompat(
                            arrayOf(PERMISSION_BACKGROUND_LOCATION),
                            BACKGROUND_LOCATION_REQUEST
                        )
                    }.show()
            } else {
                Snackbar
                    .make(
                        layout,
                        "Background Location Permission is not available, Fuck Off don't use this Feature!!!",
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                requestPermissionsCompat(
                    arrayOf(PERMISSION_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_REQUEST
                )
            }
        }
    }


    private fun requestPermissionsCompat(ary: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, ary, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(
                        layout,
                        "Camera permission has been granted, Now you can use CAMERA!!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    //todo start capture with camera
                }
            }
            GALLERY_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(
                        layout,
                        "Gallery permission has been granted, Now you can use GALLERY!!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    //todo start pickup from gallery
                }
            }
            CONTACT_REQUEST -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(
                        layout,
                        "Contact permission has been granted, Now you can use CONTACT!!!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    //todo start check contact
                }
            }
            FINE_LOCATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(
                        layout,
                        "Fine Location permission has been granted, Now you can use FINE LOCATION!!!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    //todo start check fine location
                }
            }
            BACKGROUND_LOCATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(
                        layout,
                        "Background Location permission has been granted, Now you can use BACKGROUND LOCATION!!!",
                        Snackbar.LENGTH_LONG
                    ).show()
                    //todo start check background location
                }
            }
            else -> {

            }
        }
    }

    private fun getUpdateLocation() {

    }

}