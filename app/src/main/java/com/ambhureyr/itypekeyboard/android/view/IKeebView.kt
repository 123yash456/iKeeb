package com.ambhureyr.itypekeyboard.android.view

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ambhureyr.itypekeyboard.engine.model.KeyModel
import com.ambhureyr.itypekeyboard.engine.model.KeyboardLayout
import com.ambhureyr.itypekeyboard.engine.rendering.RenderContext
import com.ambhureyr.itypekeyboard.engine.rendering.RenderEngine

class IKeebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnKeyActionListener {
        fun onKey(primaryCode: Int)
        fun onLongPressKey(primaryCode: Int) {}
    }

    val keyboardLayout = KeyboardLayout()
    private val renderEngine = RenderEngine()

    enum class ShiftState {
        OFF,
        ON,      // Single shift for next character
        CAPS_LOCK // Persistent caps lock
    }

    var shiftState: ShiftState = ShiftState.OFF
        set(value) {
            field = value
            invalidate()
        }

    var listener: OnKeyActionListener? = null
    private var pressedKey: KeyModel? = null

    // Double click detection for Shift
    private var lastShiftPressTime: Long = 0L

    // Long press & repeat timer for backspace
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPressTriggered = false
    private val longPressRunnable = Runnable {
        pressedKey?.let { key ->
            if (key.isRepeatable) {
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
        keyboardLayout.measure(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val renderContext = RenderContext(
            canvas = canvas,
            layout = keyboardLayout,
            width = width.toFloat(),
            height = height.toFloat(),
            isShifted = shiftState != ShiftState.OFF,
            pressedKey = pressedKey
        )
        renderEngine.render(renderContext)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressedKey = keyboardLayout.findKeyAt(x, y)
                isLongPressTriggered = false
                
                pressedKey?.let { key ->
                    if (key.isRepeatable) {
                        handler.postDelayed(longPressRunnable, 350L)
                    }
                }
                
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val currentKey = keyboardLayout.findKeyAt(x, y)
                if (currentKey != pressedKey) {
                    pressedKey = currentKey
                    handler.removeCallbacks(longPressRunnable)
                    handler.removeCallbacks(repeatRunnable)
                    isLongPressTriggered = false
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(repeatRunnable)

                val releasedKey = keyboardLayout.findKeyAt(x, y)
                if (releasedKey != null && releasedKey == pressedKey && !isLongPressTriggered) {
                    if (releasedKey.primaryCode == KeyboardLayout.KEYCODE_SHIFT) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastShiftPressTime < 350L) {
                            // Double tap -> Caps Lock
                            shiftState = if (shiftState == ShiftState.CAPS_LOCK) ShiftState.OFF else ShiftState.CAPS_LOCK
                        } else {
                            // Single tap
                            shiftState = when (shiftState) {
                                ShiftState.OFF -> ShiftState.ON
                                ShiftState.ON -> ShiftState.OFF
                                ShiftState.CAPS_LOCK -> ShiftState.OFF
                            }
                        }
                        lastShiftPressTime = currentTime
                    } else {
                        listener?.onKey(releasedKey.primaryCode)
                    }
                    performClick()
                }
                pressedKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(repeatRunnable)
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
