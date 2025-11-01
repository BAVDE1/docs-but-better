package com.example.DocsButBetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.MutableLiveData
import com.example.DocsButBetter.classes.GnssHelper
import com.example.DocsButBetter.classes.Logger
import com.example.DocsButBetter.classes.PermissionHelper
import com.example.DocsButBetter.classes.PermissionPage
import com.example.DocsButBetter.ui.theme.DocsButBetterTheme
import com.example.DocsButBetter.units.OptionsMenuUnit

const val OPTIONS_OPEN_DEFAULT = false
const val PERMISSION_OPEN_DEFAULT = true

enum class Menu {
  LOGGING, OPTIONS, PERMISSIONS
}

class MainActivity : ComponentActivity() {
  private val isOptionsOpen: MutableLiveData<Boolean> = MutableLiveData(OPTIONS_OPEN_DEFAULT)
  private val isPermissionsOpen: MutableLiveData<Boolean> = MutableLiveData(PERMISSION_OPEN_DEFAULT)

//  private val menuLookups: HashMap<Menu, MutableLiveData<Boolean>> = hashMapOf(pairs = Pair<Menu, Logger.>)

  val permHelper: PermissionHelper = PermissionHelper()
  val gns: GnssHelper = GnssHelper()

  private fun initialise() {
    Logger.initialise(this)

    permHelper.initialise(this)
    gns.initialise(this, permHelper)

    Logger.onVisibilityChanged.registerCallback { isOptionsOpen.value = false }

    enableEdgeToEdge()
  }

  fun openMenu(menu: Menu) {

  }

  fun closeMenu(menu: Menu) {

  }

  fun openPermissionsMenu() {
    isOptionsOpen.value = false
    isPermissionsOpen.value = true
  }

  fun closePermissionsMenu() {
    isPermissionsOpen.value = false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initialise()

    setContent {
      var isPermissionsOpenObserved: Boolean by remember { mutableStateOf(PERMISSION_OPEN_DEFAULT) }
      isPermissionsOpen.observeForever { v: Boolean -> isPermissionsOpenObserved = v }

      DocsButBetterTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          OptionsMenuUnit(this, innerPadding, isOptionsOpen, OPTIONS_OPEN_DEFAULT)

          if (isPermissionsOpenObserved) {
            PermissionPage(this, innerPadding)
          }

          Logger.Unit(innerPadding)
        }
      }
    }
  }
}
