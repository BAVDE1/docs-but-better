package com.example.docsButBetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.docsButBetter.classes.Logger
import com.example.docsButBetter.ui.theme.DocsButBetterTheme

//const val OPTIONS_OPEN_DEFAULT = false

enum class Menu {
  LOGGING, OPTIONS
}

class MainActivity : ComponentActivity() {
//  private val isOptionsOpen: MutableLiveData<Boolean> = MutableLiveData(OPTIONS_OPEN_DEFAULT)
//  private val menuLookups: HashMap<Menu, MutableLiveData<Boolean>> = hashMapOf(pairs = Pair<Menu, Logger.>)

  private fun initialise() {
    Logger.initialise(this)

    enableEdgeToEdge()
  }

  fun openMenu(menu: Menu) {

  }

  fun closeMenu(menu: Menu) {

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initialise()

    setContent {
      DocsButBetterTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//          OptionsMenuUnit(this, innerPadding, isOptionsOpen, OPTIONS_OPEN_DEFAULT)

          Logger.Unit(innerPadding)
        }
      }
    }
  }
}
