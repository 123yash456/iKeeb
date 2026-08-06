package com.ambhureyr.itypekeyboard.android.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ambhureyr.itypekeyboard.engine.dictionary.Dictionary
import com.ambhureyr.itypekeyboard.engine.dictionary.SpatialKeyMap
import com.ambhureyr.itypekeyboard.engine.dictionary.UserDictionary
import com.ambhureyr.itypekeyboard.engine.model.KeyModel
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import com.ambhureyr.itypekeyboard.engine.rendering.GestureTrailRenderer
import com.ambhureyr.itypekeyboard.engine.rendering.RenderContext
import com.ambhureyr.itypekeyboard.engine.rendering.RenderEngine
import com.ambhureyr.itypekeyboard.engine.slide.SlideDictionaryDecoder
import kotlin.math.hypot

class IKeebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnKeyActionListener {
        fun onKey(primaryCode: Int)
        fun onLongPressKey(primaryCode: Int) {}
        fun onSlideWordCommitted(word: String) {}
        // Fired for keys that carry their own text (e.g. a kaomoji in the
        // text-emoji panel) instead of a single char-code, like [onKey] does.
        fun onTextKey(text: String) {}
    }

    val keyboardLayout = KeyboardLayout()
    private val renderEngine = RenderEngine()

    // Shared with KeyboardService's Autocorrector so both features learn from
    // and score against the exact same personal vocabulary and real key positions.
    val userDictionary = UserDictionary(context)
    val spatialKeyMap = SpatialKeyMap(keyboardLayout)
    private val slideDecoder = SlideDictionaryDecoder(Dictionary.get(context), userDictionary, spatialKeyMap)
    private val gesturePath = Path()
    private val traversedKeys = mutableListOf<KeyModel>()

    private var isSliding = false
    private var startX = 0f
    private var startY = 0f

    enum class ShiftState {
        OFF,
        ON,
        CAPS_LOCK
    }

    var shiftState: ShiftState = ShiftState.OFF
        set(value) {
            field = value
            invalidate()
        }

    var listener: OnKeyActionListener? = null
    private var pressedKey: KeyModel? = null

    private var lastShiftPressTime: Long = 0L

    private val handler = Handler(Looper.getMainLooper())
    private var isLongPressTriggered = false
    private val longPressRunnable = Runnable {
        pressedKey?.let { key ->
            if (key.isRepeatable && !isSliding) {
                isLongPressTriggered = true
                triggerRepeat(key)
            }
        }
    }

    private val repeatRunnable = object : Runnable {
        override fun run() {
            pressedKey?.let { key ->
                if (key.isRepeatable) {
                    listener?.onLongPressKey(key.primaryCode)
                    handler.postDelayed(this, 70L)
                }
            }
        }
    }

    private fun addTraversedKey(key: KeyModel?) {
        if (key != null && traversedKeys.lastOrNull() != key) {
            traversedKeys.add(key)
        }
    }

    private fun triggerRepeat(key: KeyModel) {
        listener?.onLongPressKey(key.primaryCode)
        handler.postDelayed(repeatRunnable, 100L)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetHeightDp = 320f
        val targetHeightPx = (targetHeightDp * resources.displayMetrics.density).toInt()
        val heightSpec = MeasureSpec.makeMeasureSpec(targetHeightPx, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        keyboardLayout.measure(w.toFloat(), h.toFloat(), context = context)
        spatialKeyMap.refresh()
    }

    /**
     * Re-syncs [spatialKeyMap] with the layout's current measured bounds.
     * Safe to call any time, e.g. after switching back from the symbol
     * keyboard -- see [SpatialKeyMap.refresh] for why it's a no-op otherwise.
     */
    fun refreshSpatialMap() {
        spatialKeyMap.refresh()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        renderEngine.renderers.forEach { renderer ->
            if (renderer is GestureTrailRenderer) {
                renderer.currentPath = if (isSliding) gesturePath else null
            }
        }

        val renderContext = RenderContext(
            context = context,
            canvas = canvas,
            layout = keyboardLayout,
            width = width.toFloat(),
            height = height.toFloat(),
            isShifted = shiftState != ShiftState.OFF,
            pressedKey = if (isSliding) null else pressedKey
        )
        renderEngine.render(renderContext)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                pressedKey = keyboardLayout.findKeyAt(x, y)
                isLongPressTriggered = false
                isSliding = false
                gesturePath.reset()
                gesturePath.moveTo(x, y)
                traversedKeys.clear()
                pressedKey?.let { traversedKeys.add(it) }

                pressedKey?.let { key ->
                    if (key.isRepeatable) {
                        handler.postDelayed(longPressRunnable, 350L)
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val slideEnabled = context.getSharedPreferences("ikeeb_settings", Context.MODE_PRIVATE).getBoolean("slide_typing_enabled", true)
                if (!slideEnabled) {
                    return super.onTouchEvent(event)
                }

                val distance = hypot((x - startX).toDouble(), (y - startY).toDouble()).toFloat()
                val displacementThreshold = 30f * resources.displayMetrics.density

                if (!isSliding && distance > displacementThreshold) {
                    isSliding = true
                    pressedKey = null
                    handler.removeCallbacks(longPressRunnable)
                    handler.removeCallbacks(repeatRunnable)
                }

                if (isSliding) {
                    // Android batches move events; on a fast swipe the finger can cross a
                    // whole key between two ACTION_MOVE callbacks. Replay the historical
                    // points Android already recorded so we don't miss keys in between.
                    val historySize = event.historySize
                    for (i in 0 until historySize) {
                        val hx = event.getHistoricalX(i)
                        val hy = event.getHistoricalY(i)
                        gesturePath.lineTo(hx, hy)
                        addTraversedKey(keyboardLayout.findKeyAt(hx, hy))
                    }
                    gesturePath.lineTo(x, y)
                    addTraversedKey(keyboardLayout.findKeyAt(x, y))
                } else {
                    val currentKey = keyboardLayout.findKeyAt(x, y)
                    if (currentKey != pressedKey) {
                        pressedKey = currentKey
                        handler.removeCallbacks(longPressRunnable)
                        handler.removeCallbacks(repeatRunnable)
                        isLongPressTriggered = false
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(repeatRunnable)

                if (isSliding) {
                    val decodedWord = slideDecoder.decode(traversedKeys)
                    if (!decodedWord.isNullOrEmpty()) {
                        listener?.onSlideWordCommitted(decodedWord)
                    }
                    isSliding = false
                    gesturePath.reset()
                    traversedKeys.clear()
                } else {
                    val releasedKey = keyboardLayout.findKeyAt(x, y)
                    if (releasedKey != null && releasedKey == pressedKey && !isLongPressTriggered) {
                        if (releasedKey.primaryCode == KeyboardLayout.KEYCODE_SHIFT) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastShiftPressTime < 350L) {
                                shiftState = if (shiftState == ShiftState.CAPS_LOCK) ShiftState.OFF else ShiftState.CAPS_LOCK
                            } else {
                                shiftState = when (shiftState) {
                                    ShiftState.OFF -> ShiftState.ON
                                    ShiftState.ON -> ShiftState.OFF
                                    ShiftState.CAPS_LOCK -> ShiftState.OFF
                                }
                            }
                            lastShiftPressTime = currentTime
                            listener?.onKey(releasedKey.primaryCode)
                        } else if (releasedKey.insertText != null) {
                            listener?.onTextKey(releasedKey.insertText)
                        } else {
                            listener?.onKey(releasedKey.primaryCode)
                        }
                        performClick()
                    }
                }
                pressedKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(repeatRunnable)
                isSliding = false
                gesturePath.reset()
                traversedKeys.clear()
                pressedKey = null
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
