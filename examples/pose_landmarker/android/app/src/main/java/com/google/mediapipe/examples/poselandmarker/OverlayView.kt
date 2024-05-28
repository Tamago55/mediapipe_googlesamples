package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.*

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        textPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.apply {
            color = Color.GREEN
            strokeWidth = 10f
            style = Paint.Style.STROKE
        }

        pointPaint.apply {
            color = Color.GREEN
            strokeWidth = 30f
            style = Paint.Style.FILL
        }

        textPaint.apply {
            color = Color.WHITE
            textSize = 80f
            style = Paint.Style.FILL
        }
    }

    private fun drawDottedLine(canvas: Canvas, startX: Float, startY: Float, endX: Float, color: Int, thickness: Float, angle: Double) {
        val spaceLength = 5f
        val dotLength = 15f
        val totalLength = abs(endX - startX)
        val numDots = (totalLength / (dotLength + spaceLength)).toInt()

        pointPaint.apply {
            this.color = Color.YELLOW
            strokeWidth = 30f
            style = Paint.Style.FILL
        }
        val radius = 20f
        if (angle < -2) {
            canvas.drawCircle(endX, startY, radius, pointPaint)
        } else if (angle > 2){
            canvas.drawCircle(startX, startY, radius, pointPaint)
        }

        for (i in 0 until numDots) {
            val dotStartX = startX + i * (dotLength + spaceLength)
            val dotStartY = startY
            val dotEndX = dotStartX + dotLength
            val dotEndY = startY

            linePaint.apply {
                style = Paint.Style.STROKE
                strokeWidth = thickness
                this.color = color
            }

            canvas.drawLine(dotStartX, dotStartY, dotEndX, dotEndY, linePaint)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            val landmarks = poseLandmarkerResult.landmarks().first()

            // Define the indices for shoulders, hips, and knees
            val indices = listOf(11, 12, 23, 24, 25, 26)

            // Define connections between points, considering the mirroring
            val connections = listOf(
                Pair(11, 12), // shoulder connection
                Pair(23, 24), // hip connection
                Pair(25, 26)  // knee connection
            )

            // Prepare to store point colors based on connection analysis
            val pointColors = mutableMapOf<Int, Int>()

            // Process each connection to determine colors
            connections.forEach { (start, end) ->
                val startLandmark = landmarks[start]
                val endLandmark = landmarks[end]
                val startX = (1 - startLandmark.x()) * imageWidth * scaleFactor
                val startY = startLandmark.y() * imageHeight * scaleFactor
                val endX = (1 - endLandmark.x()) * imageWidth * scaleFactor
                val endY = endLandmark.y() * imageHeight * scaleFactor

                // Calculating the angle
                val angle = calculateAngle(startX, startY, endX, endY)
                val midX = (startX + endX) / 2
                val midY = (startY + endY) / 2 - 100

//                // Decide color based on angle deviation
                if (angle < -2) {
                    drawDottedLine(canvas, startX, startY, endX, Color.YELLOW, 10f, angle)
                    linePaint.color = Color.RED
                    pointColors[start] = Color.RED
                    pointColors[end] = Color.RED
                } else if (angle > 2){
                    drawDottedLine(canvas, startX, endY, endX, Color.YELLOW, 10f, angle)
                    linePaint.color = Color.RED
                    pointColors[start] = Color.RED
                    pointColors[end] = Color.RED
                } else {
                    // Below threshold
                    linePaint.color = Color.GREEN
                    pointColors[start] = Color.GREEN
                    pointColors[end] = Color.GREEN
                }

                // Draw the line
                canvas.drawLine(startX, startY, endX, endY, linePaint)
                canvas.drawText(String.format("%.2f°", abs(angle)), midX, midY, textPaint)
            }

            // Draw mirrored points for landmarks using determined colors
            indices.forEach { index ->
                val landmark = landmarks[index]
                pointPaint.color = pointColors.getOrElse(index) { Color.GREEN } // Default to green if not set
                canvas.drawPoint(
                    (1 - landmark.x()) * imageWidth * scaleFactor,
                    landmark.y() * imageHeight * scaleFactor,
                    pointPaint
                )
            }
        }
    }


    // Function to calculate the angle between two points
    private fun calculateAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        var angle = atan2(y2 - y1, x2 - x1) * (180 / PI)
        angle = (angle + 360) % 360
        if (angle > 180) {
            angle -= 360
        }
        return angle
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

}
