package com.example.DocsButBetter.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.example.DocsButBetter.ui.theme.DARK_GREY_001
import com.example.DocsButBetter.ui.theme.DARK_GREY_003
import com.example.DocsButBetter.ui.theme.WHITE


class PressElement(
  private val onRelease: (PointerInputChange) -> Unit,
) {
  @Composable
  fun Unit(
    modifier: Modifier = Modifier,
    inner: (@Composable () -> Unit)
  ) {
    Box(Modifier.then(Modifier.pointerInput(onRelease) {
      awaitEachGesture {
        awaitFirstDown().also { it.consume() }
        val up = waitForUpOrCancellation()
        if (up != null) {
          up.consume()
          onRelease(up)
        }
      }
    }).then(modifier)) { inner() }
  }
}


class ToggleElement(
  private val onToggle: (Boolean) -> Unit,
  default: Boolean = false
) {
  private val pressElem: PressElement = PressElement(onRelease = { toggle() })
  private var toggled: MutableLiveData<Boolean> = MutableLiveData(default)

  /** silent: should this fire `onToggle` function */
  fun toggle(value: Boolean? = null, silent: Boolean = false) {
    if (value != null) toggled.value = value
    else toggled.value = !toggled.value!!
    if (!silent) onToggle(toggled.value!!)
  }

  @Composable
  fun Unit(
    modifier: Modifier = Modifier,
    inner: (@Composable (Boolean) -> Unit),
  ) {
    pressElem.Unit(modifier = modifier) {
      var toggledObserved by remember { mutableStateOf(false) }  // default value doesn't even matter here
      toggled.observeForever { v: Boolean -> toggledObserved = v }
      inner(toggledObserved)
    }
  }
}

class DragElement(
  private val onDrag: (PointerEvent) -> Unit,
  private val onDown: ((PointerEvent) -> Unit)? = null,
  private val onRelease: ((PointerEvent) -> Unit)? = null,
) {
  @Composable
  fun Unit(
    modifier: Modifier = Modifier,
    inner: (@Composable () -> Unit)
  ) {
    Box(Modifier.then(Modifier.pointerInput(PointerEventType.Press, PointerEventType.Move, PointerEventType.Release) {
      awaitPointerEventScope {
        while (true) {
          val event = awaitPointerEvent()

          // event.changes.first().position
          when (event.type) {
            PointerEventType.Press -> onDown?.let { it(event) }
            PointerEventType.Release -> onRelease?.let { it(event) }
            PointerEventType.Move -> onDrag(event)
          }
        }
      }
    }).then(modifier)) { inner() }
  }
}


@Composable
fun toggleElementDefaultInner(toggled: Boolean) {
  val cornerShape: Shape = RoundedCornerShape(5.dp)
  Column(
    Modifier.size(25.dp).clip(cornerShape).background(DARK_GREY_003).border(3.dp, DARK_GREY_001, cornerShape),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (toggled) {
      Box(Modifier.clip(cornerShape).size(10.dp).background(WHITE))
    }
  }
}


/**
 * OUTDATED lol probably avoid using this anyway
 */
//@Composable
//fun InteractionPress(onPress: (PointerInputChange) -> Unit, modifierOptions: Modifier? = null) {
//  BuildInteractionElem(Modifier.pointerInput(onPress) {
//    awaitEachGesture {
//      val down = awaitFirstDown()
//      down.consume()
//      onPress(down)
//    }
//  }, modifierOptions)
//}