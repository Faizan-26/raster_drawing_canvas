package com.example.raster_drawing_canvas

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.util.Stack
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random

enum class BrushType {
    NORMAL, RAINBOW
}

data class PathProperties(
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 10f,
    var alpha: Int = 255,
    var blendMode: PorterDuff.Mode = PorterDuff.Mode.SRC_OVER,
    var style: Paint.Style = Paint.Style.STROKE,
    var strokeCap: Paint.Cap = Paint.Cap.ROUND,
    var strokeJoin: Paint.Join = Paint.Join.ROUND,
    var isEraserMode: Boolean = false
)

class DrawPath(val path: Path = Path(), val properties: PathProperties)

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Drawing bitmap and canvas
    private lateinit var bitmap: Bitmap
    private lateinit var canvasBitmap: Canvas

    private val pathStack = Stack<DrawPath>()
    private val redoStack = Stack<DrawPath>()
    // currentPath is used only for undo/redo (it won't be drawn)
    private var currentPath: DrawPath? = null

    private var currentProperties = PathProperties()
    private val drawPaint = Paint().apply {
        isAntiAlias = true
        isDither = true // Dithering affects how colors with higher precision are down-sampled.
    }

    var brushType: BrushType = BrushType.NORMAL
        set(value) {
            field = value
            if (value == BrushType.RAINBOW) {
                rainbowHue = 0f
            }
        }
    private var rainbowHue: Float = 0f

    // Last drawing coordinates (in bitmap space)
    private var lastX = 0f
    private var lastY = 0f

    // --- Transformation-related members ---
    // The matrix applied when drawing to transform (translate/scale/rotate) the entire canvas.
    private var displayMatrix = Matrix()
    // When a gesture begins we save the current matrix here.
    private val savedMatrix = Matrix()

    // Modes: one finger for drawing, two (or more) for transformation.
    private var mode = NONE

    // For transformation gestures (when two or more fingers are used)
    // 'initialMidPoint' is recorded at the moment the two-finger gesture starts.
    private var initialMidPoint = PointF()
    private var oldDist = 1f
    private var oldRotation = 0f

    private var canvasBackgroundColor: Int = Color.WHITE

    companion object {
        private const val NONE = 0
        private const val DRAG = 1    // (Not used here since one finger is for drawing.)
        private const val ZOOM = 2

        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 10f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!::bitmap.isInitialized || bitmap.width != w || bitmap.height != h) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvasBitmap = Canvas(bitmap)
            canvasBitmap.drawColor(canvasBackgroundColor)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Apply the transformation (translation/scale/rotation) to the entire drawing.
        canvas.save()
        canvas.concat(displayMatrix)
        // Draw only the bitmap (which is updated in real time)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        // Do not draw currentPath overlay while drawing.
        canvas.restore()
    }

    // Extension function to apply drawing properties to the paint.
    private fun Paint.applyProperties(properties: PathProperties) {
        color = properties.color
        strokeWidth = properties.strokeWidth
        alpha = properties.alpha
        style = properties.style
        strokeCap = properties.strokeCap
        strokeJoin = properties.strokeJoin
        xfermode = if (properties.isEraserMode)
            PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        else
            PorterDuffXfermode(properties.blendMode)
    }

    /**
     * Distinguish between one-finger (drawing) and two-finger (transformation) touches.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (event.pointerCount > 1) {
            // Two or more fingers → transformation gesture.
            handleTransform(event)
        } else {
            handleDrawing(event)
        }
    }

    /**
     * For drawing we convert the touch coordinates from screen space into the bitmap’s space
     * (using the inverse of displayMatrix) so that drawing is consistent regardless of zoom/rotation.
     * In this version, drawing is directly committed to the bitmap.
     */
    private fun handleDrawing(event: MotionEvent): Boolean {
        // Invert the display matrix to map screen points back to bitmap coordinates.
        val inverse = Matrix()
        displayMatrix.invert(inverse)
        val pts = floatArrayOf(event.x, event.y)
        inverse.mapPoints(pts)
        val x = pts[0]
        val y = pts[1]

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                redoStack.clear()
                lastX = x
                lastY = y
                // Update the paint with the current properties.
                drawPaint.applyProperties(currentProperties)
                // Start a new path for undo purposes.
                currentPath = DrawPath(Path().apply { moveTo(x, y) }, currentProperties.copy())
                // Optionally, draw a starting point.
                canvasBitmap.drawPoint(x, y, drawPaint)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (brushType == BrushType.RAINBOW) {
                    val hsv = floatArrayOf(rainbowHue, 1f, 1f)
                    drawPaint.color = Color.HSVToColor(hsv)
                    rainbowHue = (rainbowHue + 5f) % 360f
                } else {
                    // For non-rainbow brushes, update the paint properties.
                    drawPaint.applyProperties(currentProperties)
                }
                // Draw the line segment directly on the bitmap.
                canvasBitmap.drawLine(lastX, lastY, x, y, drawPaint)
                // Record the segment for undo purposes.
                currentPath?.path?.lineTo(x, y)
                lastX = x
                lastY = y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (brushType == BrushType.RAINBOW) {
                    val hsv = floatArrayOf(rainbowHue, 1f, 1f)
                    drawPaint.color = Color.HSVToColor(hsv)
                    rainbowHue = (rainbowHue + 5f) % 360f
                } else {
                    drawPaint.applyProperties(currentProperties)
                }
                // Draw the final segment.
                canvasBitmap.drawLine(lastX, lastY, x, y, drawPaint)
                currentPath?.path?.lineTo(x, y)
                // Commit the stroke for undo purposes.
                currentPath?.let { finalPath ->
                    pathStack.push(finalPath)
                    currentPath = null
                }
                invalidate()
            }
        }
        return true
    }

    /**
     * For transformation gestures we handle:
     * - Pinch: to compute the scale (with clamping).
     * - Rotation: to compute the angle change.
     * - Swipe with two fingers: to compute translation.
     */
    private fun handleTransform(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
                savedMatrix.set(displayMatrix)
                oldDist = spacing(event)
                oldRotation = rotation(event)
                initialMidPoint = calculateMidPoint(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == ZOOM && event.pointerCount >= 2) {
                    val newMid = calculateMidPoint(event)
                    val dx = newMid.x - initialMidPoint.x
                    val dy = newMid.y - initialMidPoint.y

                    val newDist = spacing(event)
                    var scaleFactor = newDist / oldDist

                    // Clamp overall scale factor based on current scale from savedMatrix.
                    val values = FloatArray(9)
                    savedMatrix.getValues(values)
                    val baseScale = values[Matrix.MSCALE_X]
                    var newOverallScale = baseScale * scaleFactor
                    newOverallScale = newOverallScale.coerceIn(MIN_SCALE, MAX_SCALE)
                    scaleFactor = newOverallScale / baseScale

                    val newRotation = rotation(event)
                    val rotationDelta = newRotation - oldRotation

                    displayMatrix.set(savedMatrix)
                    // Apply translation (swipe) based on movement of the midpoint.
                    displayMatrix.postTranslate(dx, dy)
                    // Apply scaling and rotation about the initial midpoint.
                    displayMatrix.postScale(scaleFactor, scaleFactor, initialMidPoint.x, initialMidPoint.y)
                    displayMatrix.postRotate(rotationDelta, initialMidPoint.x, initialMidPoint.y)
                    invalidate()
                }
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                mode = NONE
            }
        }
        return true
    }

    // Helper: compute the distance between the first two fingers.
    private fun spacing(event: MotionEvent): Float {
        return if (event.pointerCount >= 2) {
            val dx = event.getX(0) - event.getX(1)
            val dy = event.getY(0) - event.getY(1)
            sqrt(dx * dx + dy * dy)
        } else {
            0f
        }
    }

    // Helper: compute the angle (in degrees) between the first two fingers.
    private fun rotation(event: MotionEvent): Float {
        return if (event.pointerCount >= 2) {
            val deltaX = event.getX(0) - event.getX(1)
            val deltaY = event.getY(0) - event.getY(1)
            Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
        } else {
            0f
        }
    }

    // Helper: compute the midpoint between the first two fingers.
    private fun calculateMidPoint(event: MotionEvent): PointF {
        val x = (event.getX(0) + event.getX(1)) / 2
        val y = (event.getY(0) + event.getY(1)) / 2
        return PointF(x, y)
    }

    fun undo() {
        if (pathStack.isNotEmpty()) {
            redoStack.push(pathStack.pop())
            redrawCanvas()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            pathStack.push(redoStack.pop())
            redrawCanvas()
        }
    }

    private fun redrawCanvas() {
        // Clear the bitmap and re-draw all paths from the stack.
        canvasBitmap.drawColor(canvasBackgroundColor)
        for (drawPath in pathStack) {
            drawPaint.applyProperties(drawPath.properties)
            canvasBitmap.drawPath(drawPath.path, drawPaint)
        }
        invalidate()
    }

    fun setRandomColor() {
        if (brushType == BrushType.NORMAL) {
            drawPaint.color = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        }
    }

    fun clearCanvas() {
        pathStack.clear()
        redoStack.clear()
        canvasBitmap.drawColor(canvasBackgroundColor)
        invalidate()
    }

    fun setBrushThickness(thickness: Float) {
        currentProperties.strokeWidth = thickness
    }

    fun setBrushColor(color: Int) {
        currentProperties.color = color
    }

    fun updateBrushProperties(properties: Map<String, Any>) {
        currentProperties = currentProperties.copy().apply {
            properties["color"]?.let { color = (it as? Long)?.toInt() ?: (it as? Int) ?: Color.BLACK }
            properties["strokeWidth"]?.let { strokeWidth = (it as Number).toFloat() }
            properties["alpha"]?.let { alpha = (it as Number).toInt() }
            properties["blendMode"]?.let { blendMode = PorterDuff.Mode.valueOf(it as String) }
            properties["style"]?.let { style = Paint.Style.valueOf(it as String) }
            properties["strokeCap"]?.let { strokeCap = Paint.Cap.valueOf(it as String) }
            properties["strokeJoin"]?.let { strokeJoin = Paint.Join.valueOf(it as String) }
            properties["isEraserMode"]?.let { isEraserMode = it as Boolean }
        }
    }

    // Reset the transformation matrix.
    fun resetTransform() {
        displayMatrix.reset()
        invalidate()
    }

    fun setCanvasColor(color: Int) {
        canvasBackgroundColor = color
        redrawCanvas()
    }

    fun saveAsImage(filePath: String, format: String, quality: Int): Boolean {
        try {
            val file = File(filePath)
            FileOutputStream(file).use { out ->
                val compressFormat = when(format.toLowerCase()) {
                    "png" -> Bitmap.CompressFormat.PNG
                    "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
                    "webp" -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Bitmap.CompressFormat.WEBP_LOSSLESS
                        } else {
                            Bitmap.CompressFormat.WEBP
                        }
                    }
                    "heif" -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP // Using WEBP as fallback since HEIF is not universally available
                        } else {
                            Bitmap.CompressFormat.PNG // Fallback for older Android versions
                        }
                    }
                    else -> Bitmap.CompressFormat.PNG
                }
                bitmap.compress(compressFormat, quality, out)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
