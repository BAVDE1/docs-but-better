package com.example.docsButBetter.classes

class EventHandler {
  private val allCallbacks: HashMap<Int, (Any?) -> Unit> = HashMap()
  private var idIncrement: Int = 0

  fun registerCallback(callback: (Any?) -> Unit): Int {
    val id: Int = idIncrement++
    allCallbacks[id] = callback
    return id
  }

  fun removeCallback(id: Int) {
    if (!allCallbacks.containsKey(id)) {
      Logger.danger("'$id' does not exist in registered callbacks. Ignoring")
      return
    }

    allCallbacks.remove(id)
  }

  fun fire() {
    for (callback in allCallbacks) {
      callback.value(null)
    }
  }

  fun fire(arg: Any) {
    for (callback in allCallbacks) {
      callback.value(arg)
    }
  }

  /** takes a custom function to call for each callback. This allows multiple, custom parameters to be passed to each callback */
  fun fire(call: ((Any?) -> Unit) -> Unit) {
    for (callback in allCallbacks) {
      call(callback.value)
    }
  }
}