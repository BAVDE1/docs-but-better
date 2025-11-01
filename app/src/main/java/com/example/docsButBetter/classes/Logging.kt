package com.example.docsButBetter.classes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.example.docsButBetter.*
import com.example.docsButBetter.ui.theme.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.floor

val Logger: LoggerSingleton = LoggerSingleton()

const val OPEN_DEFAULT = false
const val SHOW_MORE_DEFAULT = false
const val SHOW_DEBUG_DEFAULT = true
const val SNAP_TO_BTM_DEFAULT = true
const val HEIGHT_DEFAULT = 400f
const val MAX_VISIBLE_LOGS = 200

enum class LogLevel {
  DEBUG, INFO, WARN, DANGER
}

class LoggerSingleton {
  private var initialized: Boolean = false
  private var activity: MainActivity? = null

  // live values
  private val isOpen: MutableLiveData<Boolean> = MutableLiveData(OPEN_DEFAULT)
  private val logs: MutableLiveData<List<Log>> = MutableLiveData(listOf())
  private val logOverflow: MutableLiveData<Int> = MutableLiveData(0)
  private val height: MutableLiveData<Dp> = MutableLiveData(Dp(HEIGHT_DEFAULT))
  private val showMore: MutableLiveData<Boolean> = MutableLiveData(SHOW_MORE_DEFAULT)
  private val showDebug: MutableLiveData<Boolean> = MutableLiveData(SHOW_DEBUG_DEFAULT)
  private val snapToBtm: MutableLiveData<Boolean> = MutableLiveData(SNAP_TO_BTM_DEFAULT)

  // events
  val onVisibilityChanged: EventHandler = EventHandler()

  // interactable
  private val showMoreToggle =
    ToggleElement({ toggled: Boolean -> showMore.value = toggled }, default = SHOW_MORE_DEFAULT)
  private val showDebugToggle =
    ToggleElement({ toggled: Boolean -> showDebug.value = toggled }, default = SHOW_DEBUG_DEFAULT)

  // lock (from toggling itself off again) while waiting for auto scroll to finish
  private var snapToBtmLocked = SNAP_TO_BTM_DEFAULT
  private val snapToBtmToggle = ToggleElement({ toggled: Boolean ->
    if (toggled) snapToBtmLocked = true
    snapToBtm.value = toggled
  }, default = SNAP_TO_BTM_DEFAULT)

  private val heightDrag = DragElement({ e: PointerEvent ->
    val change: PointerInputChange = e.changes.first()
    val delta = (change.previousPosition.y - change.position.y) * .5f  // TODO: this is incorrect & wonky
    var newHeight = height.value!!.value + delta
    newHeight = 200f.coerceAtLeast(800f.coerceAtMost(newHeight))
    height.value = Dp(newHeight)
  })

  private val randomLogBtn = PressElement({
    info(
      listOf(
        "a log",
        "something",
        "another thing",
        "omba",
        "eeeeee"
      ).random()
    )
  })

  fun initialise(activity: MainActivity) {
    if (initialized) return
    this.activity = activity
    initialized = true
    debug("Logger initialized (activity: $activity)")
  }

  fun isInitialized(): Boolean {
    return initialized
  }

  fun pushLog(level: LogLevel, msg: String) {
    val newLog = Log(logLevel = level, msg)
    var newList: MutableList<Log> = mutableListOf(*logs.value!!.toTypedArray(), newLog)
    val diff = newList.size - MAX_VISIBLE_LOGS
    if (diff > 0) {
      logOverflow.value = logOverflow.value!! + diff
      newList = newList.subList(diff, diff + MAX_VISIBLE_LOGS)
      newList[0] = Log(LogLevel.DEBUG, "+ ${logOverflow.value} overflowed")
    }
    logs.value = newList
  }

  fun debug(msg: Any) {
    pushLog(LogLevel.DEBUG, msg.toString())
  }

  fun info(msg: Any) {
    pushLog(LogLevel.INFO, msg.toString())
  }

  fun warn(msg: Any) {
    pushLog(LogLevel.WARN, msg.toString())
  }

  fun danger(msg: Any) {
    pushLog(LogLevel.DANGER, msg.toString())
  }

  fun toggleVisibility(value: Boolean) {
    isOpen.value = value
    onVisibilityChanged.fire(isOpen.value!!)
  }

  @Composable
  fun Unit(pv: PaddingValues) {
    val cornerShape: Shape = RoundedCornerShape(5.dp)
    val scrollState: ScrollState = rememberScrollState()

    var isOpenObserved: Boolean by remember { mutableStateOf(OPEN_DEFAULT) }
    isOpen.observeForever { v: Boolean -> isOpenObserved = v }

    var logsObserved: List<Log> by remember { mutableStateOf(listOf()) }
    logs.observeForever { v: List<Log> -> logsObserved = v }

    var logOverflowObserved: Int by remember { mutableStateOf(0) }
    logOverflow.observeForever { v: Int -> logOverflowObserved = v }

    var heightObserved: Dp by remember { mutableStateOf(Dp(HEIGHT_DEFAULT)) }
    height.observeForever { v: Dp -> heightObserved = v }

    var showMoreObserved: Boolean by remember { mutableStateOf(SHOW_MORE_DEFAULT) }
    showMore.observeForever { v: Boolean -> showMoreObserved = v }

    var showDebugObserved: Boolean by remember { mutableStateOf(SHOW_DEBUG_DEFAULT) }
    showDebug.observeForever { v: Boolean -> showDebugObserved = v }

    var snapToBtmObserved: Boolean by remember { mutableStateOf(SNAP_TO_BTM_DEFAULT) }
    snapToBtm.observeForever { v: Boolean -> snapToBtmObserved = v }

    // auto scroll (asynchronous)
    if (snapToBtm.value!!) LaunchedEffect(logsObserved.size, showMoreObserved, showDebugObserved) {
      scrollState.scrollTo(scrollState.maxValue)
      snapToBtmLocked = false  // unlock!
    }

    // manual scroll listener (conditional order is important)
    if (snapToBtmObserved && scrollState.value < scrollState.maxValue && !snapToBtmLocked) {
      snapToBtmToggle.toggle(value = false)
    }

    val showMoreUnit: @Composable () -> Unit = {
      showMoreToggle.Unit { toggled: Boolean ->
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          Column(
            Modifier.size(25.dp).clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(if (toggled) "v" else ">", color = BLACK)
          }
        }
      }
    }

    val snapToBtnUnit: @Composable () -> Unit = {
      snapToBtmToggle.Unit { toggled: Boolean ->
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          toggleElementDefaultInner(toggled = toggled)
          Text("snap to btm", color = BLACK)
        }
      }
    }

    val showDebugUnit: @Composable () -> Unit = {
      showDebugToggle.Unit { toggled: Boolean ->
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
          toggleElementDefaultInner(toggled = toggled)
          Text("show debug", color = BLACK)
        }
      }
    }

    val randomLogUnit: @Composable () -> Unit = {
      randomLogBtn.Unit {
        Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
          Text("rand log", color = BLACK, modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp))
        }
      }
    }

    val closeBtnUnit: @Composable () -> Unit = {
      PressElement { toggleVisibility(false) }.Unit {
        Box(Modifier.size(25.dp).clip(cornerShape).background(DARK_GREY_003).border(3.dp, DARK_GREY_001, cornerShape)) {
          Text(text = "X", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
        }
      }
    }

    val clearLogsUnit: @Composable () -> Unit = {
      PressElement {
        logs.value = listOf()
        logOverflow.value = 0
      }.Unit {
        Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
          Text("clear logs", color = BLACK, modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp))
        }
      }
    }

    val sliceLogsUnit: @Composable () -> Unit = {
      PressElement {
        logs.value = logs.value!!.subList(floor(logs.value!!.size * .5).toInt(), logs.value!!.size)
        logOverflow.value = 0
      }.Unit {
        Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
          Text("slice logs", color = BLACK, modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp))
        }
      }
    }

    Box(Modifier.fillMaxSize().padding(pv)) {
      if (isOpenObserved) {
        Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(heightObserved + 2.dp)) {
          Box(Modifier.fillMaxWidth().height(2.dp).background(DARK_GREY_001))
        }
        Box(Modifier.align(Alignment.BottomStart).background(LIGHT_GREY_007)
            .fillMaxWidth().height(heightObserved).padding(top = 5.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        ) {
          Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            heightDrag.Unit {
              Row(Modifier.fillMaxWidth().padding(bottom = 5.dp), horizontalArrangement = Arrangement.Center) {
                Box(Modifier.clip(cornerShape).fillMaxWidth(.8f).height(10.dp).background(DARK_GREY_001))
              }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              showMoreUnit()
              snapToBtnUnit()
              closeBtnUnit()
            }
            if (showMoreObserved) {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Box(Modifier.fillMaxWidth().height(5.dp).background(GREY))
              }
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                showDebugUnit()
                randomLogUnit()
              }
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("logs: ${logsObserved.size} (+${logOverflowObserved})", color = BLACK)
                sliceLogsUnit()
                clearLogsUnit()
              }
            }
            Row(
              Modifier.fillMaxWidth().verticalScroll(scrollState).border(3.dp, DARK_GREY_003, cornerShape)
                .clip(cornerShape).background(DARK_GREEN).weight(1f, true).padding(10.dp),
              verticalAlignment = Alignment.Bottom
            ) {
              Column(Modifier.width(IntrinsicSize.Max), verticalArrangement = Arrangement.Bottom) {
                for (log: Log in logsObserved) {
                  if (showDebugObserved || log.logLevel != LogLevel.DEBUG) {
                    Text(
                      "[${log.getFormattedTime()}] ${log.msg}",
                      fontFamily = FontFamily.Monospace,
                      color = log.getColor(),
                      style = TextStyle(textIndent = TextIndent(0.sp, 20.sp))
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        Box(
          modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp).alpha(.5f)
        ) {
          PressElement { toggleVisibility(true) }.Unit {
            Row(Modifier.clip(cornerShape).background(LIGHT_GREY_007).border(3.dp, DARK_GREY_003, cornerShape)) {
              Text(
                "${logsObserved.size + logOverflowObserved}",
                color = BLACK,
                modifier = Modifier.padding(10.dp, 5.dp),
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

class Log(val logLevel: LogLevel, val msg: String) {
  private var epochTime: Long = Instant.now().epochSecond

  fun getFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    val netDate = Date(epochTime * 1000)
    return sdf.format(netDate)
  }

  fun getColor(): Color {
    return when (logLevel) {
      LogLevel.DEBUG -> GREY
      LogLevel.INFO -> WHITE
      LogLevel.WARN -> YELLOW
      LogLevel.DANGER -> RED
    }
  }
}
