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

            // Draw mirrored points for these landmarks
            indices.forEach { index ->
                val landmark = landmarks[index]
                canvas.drawPoint(
                    (1 - landmark.x()) * imageWidth * scaleFactor, // Mirroring the x-coordinate
                    landmark.y() * imageHeight * scaleFactor,
                    pointPaint
                )
            }


            // Draw connections and angles
            connections.forEach { (start, end) ->
                val startLandmark = landmarks[start]
                val endLandmark = landmarks[end]
                val startX = (1 - startLandmark.x()) * imageWidth * scaleFactor
                val startY = startLandmark.y() * imageHeight * scaleFactor
                val endX = (1 - endLandmark.x()) * imageWidth * scaleFactor
                val endY = endLandmark.y() * imageHeight * scaleFactor

                // Drawing the line
                canvas.drawLine(startX, startY, endX, endY, linePaint)

                // Calculating and displaying angle
                val angle = calculateAngle(startX, startY, endX, endY)
                val midX = (startX + endX) / 2
                val midY = (startY + endY) / 2
                canvas.drawText("${angle.toInt()}°", midX, midY, textPaint)
            }
        }
    }

    // Function to calculate the angle between two points
    private fun calculateAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val angle = atan2(y2 - y1, x2 - x1) * (180 / PI)
        return if (angle < 0) angle + 360 else angle
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

//    companion object {
//        private const val LANDMARK_STROKE_WIDTH = 12F
//    }
}




//    private fun initPaints() {
//        linePaint.color =
//            ContextCompat.getColor(context!!, R.color.mp_color_primary)
//        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
//        linePaint.style = Paint.Style.STROKE
//
//        pointPaint.color = Color.YELLOW
//        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
//        pointPaint.style = Paint.Style.FILL
//    }

//    override fun draw(canvas: Canvas) {
//        super.draw(canvas)
//        results?.let { poseLandmarkerResult ->
//            for(landmark in poseLandmarkerResult.landmarks()) {
//                for(normalizedLandmark in landmark) {
//                    canvas.drawPoint(
//                        normalizedLandmark.x() * imageWidth * scaleFactor,
//                        normalizedLandmark.y() * imageHeight * scaleFactor,
//                        pointPaint
//                    )
//                }
//
//                PoseLandmarker.POSE_LANDMARKS.forEach {
//                    canvas.drawLine(
//                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
//                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
//                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
//                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
//                        linePaint)
//                }
//            }
//        }
//    }

//    override fun draw(canvas: Canvas) {
//        super.draw(canvas)
//        results?.let { poseLandmarkerResult ->
//            val landmarks = poseLandmarkerResult.landmarks().first()
//
//            // Define the indices for shoulders, hips, and knees
//            val indices = listOf(11, 12, 23, 24, 25, 26)
//
//            // Draw points for these landmarks
//            indices.forEach { index ->
//                val landmark = landmarks[index]
//                canvas.drawPoint(
//                    landmark.x() * imageWidth * scaleFactor,
//                    landmark.y() * imageHeight * scaleFactor,
//                    pointPaint
//                )
//            }
//
//            // Define connections between points
//            val connections = listOf(
//                Pair(11, 12), // shoulder
//                Pair(23, 24), // hip
//                Pair(25, 26) // knee
//            )
//
//            // Draw lines for these connections
//            connections.forEach { (start, end) ->
//                val startLandmark = landmarks[start]
//                val endLandmark = landmarks[end]
//                canvas.drawLine(
//                    startLandmark.x() * imageWidth * scaleFactor,
//                    startLandmark.y() * imageHeight * scaleFactor,
//                    endLandmark.x() * imageWidth * scaleFactor,
//                    endLandmark.y() * imageHeight * scaleFactor,
//                    linePaint
//                )
//            }
//        }
//    }
