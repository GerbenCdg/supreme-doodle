package fr.gerben.supreme_doodle

import android.graphics.Color
import android.graphics.PointF

typealias CanvasStrokes = ArrayDeque<CanvasStroke>

class DoodleDataManager(
    private val undoCommand: Command<CanvasStroke>,
    private val redoCommand: Command<CanvasStroke>,
) {

    private val data: CanvasData = CanvasData()
    private val redoStrokes = CanvasStrokes()


    fun addStroke(stroke: CanvasStroke) {
        data.strokes.add(stroke)
    }

    fun undo() {
        if (data.strokes.isEmpty())
            return

        val stroke = data.strokes.removeLast()
        redoStrokes.add(stroke)

        undoCommand(stroke)
    }

    fun redo() {
        if (redoStrokes.isEmpty())
            return

        val stroke = redoStrokes.removeLast()
        addStroke(stroke)

        redoCommand(stroke)
    }


}

data class CanvasPaint(
    val brushWidth: Float,
    val color: Color,
)

data class CanvasData(
    val strokes: CanvasStrokes = CanvasStrokes(),
)

data class CanvasStroke(
    val points: List<PointF>,
    val canvasPaint: CanvasPaint,
)

typealias Command<T> = (T) -> Unit