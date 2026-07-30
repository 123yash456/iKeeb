package com.ambhureyr.itypekeyboard.android.view

import android.content.Context
import android.graphics.Canvas
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
    }

    val keyboardLayout = KeyboardLayout()
    private val renderEngine = RenderEngine()

    var isShifted: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var listener: OnKeyActionListener? = null
    private var pressedKey: KeyModel? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Increase keyboard height to 320dp to accommodate 5 rows (Numbers + QWERTY)
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
            isShifted = isShifted,
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
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val currentKey = keyboardLayout.findKeyAt(x, y)
                if (currentKey != pressedKey) {
                    pressedKey = currentKey
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                val releasedKey = keyboardLayout.findKeyAt(x, y)
                if (releasedKey != null && releasedKey == pressedKey) {
                    listener?.onKey(releasedKey.primaryCode)
                    performClick()
                }
                pressedKey = null
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
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
