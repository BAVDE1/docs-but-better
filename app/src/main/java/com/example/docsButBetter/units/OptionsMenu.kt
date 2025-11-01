package com.example.docsButBetter.units

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.example.docsButBetter.MainActivity
import com.example.docsButBetter.classes.PressElement
import com.example.docsButBetter.ui.theme.BLACK
import com.example.docsButBetter.ui.theme.DARK_GREY_003
import com.example.docsButBetter.ui.theme.LIGHT_GREY_007
import com.example.docsButBetter.ui.theme.LIGHT_GREY_009

@Composable
fun OptionsMenuUnit(
  activity: MainActivity,
  innerPadding: PaddingValues,
  isOptionsOpen: MutableLiveData<Boolean>,
  optionsOpenDefault: Boolean
) {
  val cornerShape: Shape = RoundedCornerShape(5.dp)

  var isOptionsOpenObserved: Boolean by remember { mutableStateOf(optionsOpenDefault) }
  isOptionsOpen.observeForever { v: Boolean -> isOptionsOpenObserved = v }

  Box(Modifier.fillMaxSize().padding(innerPadding)) {
    Box(
      modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isOptionsOpenObserved) {
          Row(
            Modifier.clip(cornerShape).width(180.dp).background(LIGHT_GREY_007)
              .border(3.dp, DARK_GREY_003, cornerShape)
          ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
              PressElement { }.Unit {
                Row(
                  Modifier.clip(cornerShape).fillMaxWidth().background(LIGHT_GREY_009).padding(5.dp),
                  horizontalArrangement = Arrangement.Center
                ) {
                  Text("settings (N/I)", color = BLACK)
                }
              }
            }
          }
        }
        PressElement { _: PointerInputChange -> isOptionsOpen.value = !isOptionsOpen.value!! }.Unit {
          Row(
            Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)
              .width(40.dp), horizontalArrangement = Arrangement.Center
          ) {
            Text(
              if (isOptionsOpenObserved) "X" else "...",
              color = BLACK,
              modifier = Modifier.padding(10.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
