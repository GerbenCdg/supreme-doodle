package fr.gerben.supreme_doodle

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColor
import fr.gerben.supreme_doodle.ui.theme.AppColors
import kotlin.math.abs

class DoodleCanvas(context: Context) : View(context) {

    companion object {
        private const val STROKE_WIDTH = 12f
    }

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val backgroundColor = AppColors.CanvasBackground
    private val drawColor = AppColors.Black

    val data = DoodleDataManager(::undo, ::redo)

    // Set up the paint with which to draw.
    private val paint = Paint().apply {
        color = drawColor.toArgb()
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    private val path = Path()
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop / 4


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (::extraBitmap.isInitialized)
            extraBitmap.recycle()

        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)

        extraCanvas.drawColor(backgroundColor.toArgb())

        // Calculations should be performed here
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {

        val motionTouchEvent = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart(motionTouchEvent)
            MotionEvent.ACTION_MOVE -> touchMove(motionTouchEvent)
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    private lateinit var current: PointF
    private lateinit var currentStrokePoints: MutableList<PointF>

    private fun touchStart(motionTouchEvent: PointF) {
        currentStrokePoints = mutableListOf()
        currentStrokePoints.add(motionTouchEvent.copy())

        path.reset()
        path.moveTo(motionTouchEvent)
        current = motionTouchEvent
    }

    private fun touchMove(motionTouchEvent: PointF, shouldInvalidate: Boolean = true) {
        val dx = abs(motionTouchEvent.x - current.x)
        val dy = abs(motionTouchEvent.y - current.y)

        if (dx >= touchTolerance || dy >= touchTolerance) {
            currentStrokePoints.add(motionTouchEvent.copy())

            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(current.x,
                current.y,
                (motionTouchEvent.x + current.x) / 2,
                (motionTouchEvent.y + current.y) / 2)

            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint)
            current = motionTouchEvent
        }

        if (shouldInvalidate) {
            invalidate()
        }
    }


    private fun touchUp(shouldAddStroke: Boolean = true) {

        if (shouldAddStroke) {
            val stroke = CanvasStroke(
                points = currentStrokePoints,
                canvasPaint = CanvasPaint(paint.strokeWidth, paint.color.toColor()))

            data.addStroke(stroke)
        }

        path.reset()
    }

    private fun undo(stroke: CanvasStroke) {
        val undoStroke = stroke.copy(
            canvasPaint = stroke.canvasPaint.copy(
                color = backgroundColor.toArgb().toColor())
        )
        draw(undoStroke)
    }

    private fun redo(stroke: CanvasStroke) {
        draw(stroke)
    }

    private fun draw(stroke: CanvasStroke) {
        val oldCanvasPaint = paint.getCanvasPaint()
        paint.setCanvasPaint(stroke.canvasPaint)

        touchStart(stroke.points.first())
        for (point in stroke.points.drop(1)) {
            touchMove(point, shouldInvalidate = false)
        }
        touchUp(shouldAddStroke = false)
        invalidate()

        paint.setCanvasPaint(oldCanvasPaint)
    }


    fun Path.moveTo(pointF: PointF) {
        moveTo(pointF.x, pointF.y)
    }

    fun PointF.copy() = PointF(this.x, this.y)

    fun Paint.getCanvasPaint(): CanvasPaint {
        return CanvasPaint(
            brushWidth = strokeWidth,
            color = color.toColor()
        )
    }

    fun Paint.setCanvasPaint(canvasPaint: CanvasPaint) {
        color = canvasPaint.color.toArgb()
        strokeWidth = canvasPaint.brushWidth
    }


    fun Paint.copy() = Paint(paint)
}