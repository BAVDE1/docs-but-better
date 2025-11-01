package com.example.DocsButBetter.classes

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.example.DocsButBetter.MainActivity

/**
 Global Navigation Satellite System
 */
class GnssHelper {
  private var activity: MainActivity? = null
  private var context: Context? = null
  private var permHelper: PermissionHelper? = null

  private var lm: LocationManager? = null
  private var listener: GnssListener = GnssListener()

  fun initialise(activity: MainActivity, permHelper: PermissionHelper) {
    this.activity = activity
    this.context = activity.applicationContext
    this.permHelper = permHelper
    setupLocationService()
  }

  private fun setupLocationService() {
    if (permHelper!!.arePermissionsGranted(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION))) {
      lm = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
      setupLocationUpdates()
    }
  }

  private fun requestCurrentLocation() {
  }

  @SuppressLint("MissingPermission")  // should already be checked
  private fun setupLocationUpdates() {
    if (isLocationEnabled()) {
      lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000.toLong(), 1.toFloat(), listener)
    }
  }

  private fun isLocationEnabled(): Boolean {
    if (lm == null) return false
    return lm!!.isLocationEnabled
  }

  private fun hasLocationPerm(): Boolean {
    return activity!!.permHelper.arePermissionsGranted(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
  }

  private fun hasBackgroundLocationPerm(): Boolean {
    return activity!!.permHelper.arePermissionsGranted(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION))
  }

  // todo: move
//  private fun hasLocationPermission(): Boolean {
//    val coarsePerms = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION)
//    val finePerms = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
//    val bgPerms = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//    return (finePerms == PackageManager.PERMISSION_GRANTED &&
//            coarsePerms == PackageManager.PERMISSION_GRANTED &&
//            bgPerms == PackageManager.PERMISSION_GRANTED)
//  }

  // todo: move
//  private fun requestLocationPermission() {
//    PermissionHelper.requestPermissions(
//      arrayOf(
//        Manifest.permission.ACCESS_COARSE_LOCATION,
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_BACKGROUND_LOCATION
//      ), { permsGranted: Map<String, Boolean> ->
//        requestLocationPermissionCallback(permsGranted)
//      }
//    )
//  }

//  private fun requestLocationPermissionCallback(permsGranted: Map<String, Boolean>) {
//    println(permsGranted)
//  }
}

class GnssListener : LocationListener {
  override fun onLocationChanged(p0: Location) {
    println("Not yet implemented")
  }
}
