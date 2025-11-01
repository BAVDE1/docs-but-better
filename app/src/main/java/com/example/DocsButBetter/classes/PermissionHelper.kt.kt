package com.example.DocsButBetter.classes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.DocsButBetter.MainActivity
import com.example.DocsButBetter.ui.theme.*

const val ACCESS_INTERNET = Manifest.permission.INTERNET
const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
const val ACCESS_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION

val PERMS_LIST = arrayListOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)

class PermissionHelper {
  private var initialized: Boolean = false
  private var activity: MainActivity? = null
  private var context: Context? = null

  private var permRequestLock: Boolean = false

  /** SINGLE */
  private var requestPermCallback: ((Boolean) -> Unit)? = null
  private var requestPermLauncher: ActivityResultLauncher<String>? = null

  /** MULTIPLE */
  private var requestPermsCallback: ((Map<String, Boolean>) -> Unit)? = null
  private var requestPermsLauncher: ActivityResultLauncher<Array<String>>? = null

  fun initialise(activity: MainActivity) {
    if (initialized) return
    this.activity = activity
    this.context = activity.applicationContext

    setupLaunchers()
    initialized = true
  }

  private fun setupLaunchers() {
    requestPermLauncher = activity!!.registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { grantedResult: Boolean ->
      requestPermCallback?.let { it(grantedResult) }
      requestPermCallback = null
      permRequestLock = false
    }

    requestPermsLauncher = activity!!.registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedResults: Map<String, Boolean> ->
      for (result in grantedResults) {
      }
      requestPermsCallback?.let { it(grantedResults) }
      requestPermsCallback = null
      permRequestLock = false
    }
  }

  fun isPermissionGranted(perm: String): Boolean {
    return ActivityCompat.checkSelfPermission(context!!, perm) == PackageManager.PERMISSION_GRANTED
  }

  fun arePermissionsGranted(perms: Array<String>): Boolean {
    for (perm in perms) {
      if (!isPermissionGranted(perm)) return false
    }
    return true
  }

  fun requestPermission(perm: String, callback: ((Boolean) -> Unit)? = null) {
    if (permRequestLock) return
    permRequestLock = true
    requestPermCallback = callback
    requestPermLauncher!!.launch(perm)
  }

  fun requestPermissions(perms: Array<String>, callback: ((Map<String, Boolean>) -> Unit)? = null) {
    if (permRequestLock) return
    permRequestLock = true
    requestPermsCallback = callback
    requestPermsLauncher!!.launch(perms)
  }
}

@Composable
fun PermissionPage(activity: MainActivity, innerPadding: PaddingValues) {
  val cornerShape: Shape = RoundedCornerShape(5.dp)
  val scrollState: ScrollState = rememberScrollState()

  var permsListObserved: ArrayList<String> by remember { mutableStateOf(PERMS_LIST) }

  fun refreshList() {
    permsListObserved = arrayListOf()
    Handler(Looper.getMainLooper()).postDelayed({ permsListObserved = PERMS_LIST }, 50)
  }

  Box(Modifier.fillMaxSize().padding(innerPadding).background(LIGHT_GREY_007)) {
    Box(Modifier.fillMaxSize().padding(10.dp, 10.dp, 10.dp, 60.dp)) {
      Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
        PressElement { activity.closePermissionsMenu() }.Unit {
          Column(
            Modifier.size(25.dp).clip(cornerShape).background(DARK_GREY_003).border(3.dp, DARK_GREY_001, cornerShape)
          ) {
            Text(text = "X", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
          }
        }
      }
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          Text("permissions", color = BLACK, fontWeight = FontWeight.Bold, fontSize = TextUnit(24f, TextUnitType.Sp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
          Box(Modifier.fillMaxWidth().height(5.dp).background(DARK_GREY_003))
        }
        Row(Modifier.clip(cornerShape).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          PressElement {
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.packageName)))
          }.Unit {
            Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
              Text("change permissions", color = BLACK, modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp))
            }
          }
          PressElement { refreshList() }.Unit {
            Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
              Text("refresh", color = BLACK, modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp))
            }
          }
        }
        Row(
          Modifier.clip(cornerShape).fillMaxSize().verticalScroll(scrollState).background(LIGHT_GREY_006)
            .border(3.dp, DARK_GREY_004, cornerShape)
        ) {
          Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (perm in permsListObserved) {
              val granted = activity.permHelper.isPermissionGranted(perm)
              Row(Modifier.clip(cornerShape).fillMaxWidth().background(LIGHT_GREY_008).padding(5.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                  Text(perm.split('.').last(), color = BLACK, fontWeight = FontWeight.Bold)
                  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Box(Modifier.fillMaxWidth().height(5.dp).background(LIGHT_GREY_006))
                  }
                  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                      if (granted) "GRANTED" else "DENIED",
                      color = if (granted) GREEN else RED,
                      fontWeight = FontWeight.Bold
                    )
                    if (!granted) {
                      PressElement {
                        activity.permHelper.requestPermission(perm) { refreshList() }
                      }.Unit {
                        Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
                          Text("request", color = BLACK, modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp))
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}