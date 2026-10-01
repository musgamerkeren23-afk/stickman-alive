package com.example.stickmanalive

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.sin

enum class StickmanExpression { NORMAL, KAGET_YOUTUBE }

class StickmanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var data = StickmanData()
        set(value) {
            field = value
            invalidate()
        }
        
    var currentExpression = StickmanExpression.NORMAL
    var speechBubbleText = ""

    private var posX = 300f
    private var posY = 500f
    private var speedX = 3f
    private var walkCycle = 0f
    private var expressionTimer = 0
    private var isInPipMode = false

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 35f
        style = Paint.Style.FILL
    }

    init {
        post(object : Runnable {
            override fun run() {
                updateLogic()
                invalidate()
                postDelayed(this, 16)
            }
        })
    }

    fun triggerReaction(expression: StickmanExpression, text: String) {
        currentExpression = expression
        speechBubbleText = text
        expressionTimer = 180
    }

    fun setIsInPipMode(inPip: Boolean) {
        isInPipMode = inPip
        if (isInPipMode) {
            posX = 150f
            posY = 250f 
            speedX = 1.5f
        } else {
            posX = 300f
            posY = 500f
            speedX = 3f
        }
        invalidate()
    }

    private fun updateLogic() {
        if (expressionTimer > 0) {
            expressionTimer--
            if (expressionTimer == 0) {
                currentExpression = StickmanExpression.NORMAL
                speechBubbleText = ""
            }
        }

        if (currentExpression == StickmanExpression.KAGET_YOUTUBE) {
            walkCycle += 0.3f
            posY = (if (isInPipMode) 250f else 500f) + sin(walkCycle * 2) * 15f
            return 
        }

        posX += speedX
        val maxW = if (width > 0) width.toFloat() else 500f
        if (posX > maxW - 100 || posX < 100) speedX = -speedX
        walkCycle += 0.1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = data.skinColor
        paint.strokeWidth = data.headRadius * 0.2f
        val direction = if (speedX > 0) 1 else -1

        if (speechBubbleText.isNotEmpty()) {
            drawSpeechBubble(canvas, posX, posY - data.headRadius - 60f, speechBubbleText)
        }

        paint.style = Paint.Style.STROKE
        canvas.drawCircle(posX, posY - data.headRadius, data.headRadius, paint)

        val eyeOffset = data.headRadius * 0.3f
        if (currentExpression == StickmanExpression.KAGET_YOUTUBE) {
            paint.style = Paint.Style.STROKE
            canvas.drawCircle(posX - eyeOffset, posY - data.headRadius - 5, 8f, paint)
            canvas.drawCircle(posX + eyeOffset, posY - data.headRadius - 5, 8f, paint)
            canvas.drawCircle(posX, posY - data.headRadius + 20f, 10f, paint)
        } else {
            paint.style = Paint.Style.FILL
            canvas.drawCircle(posX - eyeOffset, posY - data.headRadius, 5f, paint)
            canvas.drawCircle(posX + eyeOffset, posY - data.headRadius, 5f, paint)
            paint.style = Paint.Style.STROKE
            canvas.drawLine(posX - 10f, posY - data.headRadius + 15f, posX + 10f, posY - data.headRadius + 15f, paint)
        }

        paint.style = Paint.Style.STROKE
        val torsoBottomY = posY + data.torsoLength
        canvas.drawLine(posX, posY, posX, torsoBottomY, paint)

        if (currentExpression == StickmanExpression.KAGET_YOUTUBE) {
            canvas.drawLine(posX, posY + 20f, posX - 40f, posY - 20f, paint)
            canvas.drawLine(posX, posY + 20f, posX + 40f, posY - 20f, paint)
        } else {
            val armSwing = sin(walkCycle) * data.armLength
            canvas.drawLine(posX, posY + 20f, posX + (armSwing * direction), posY + 20f + data.armLength, paint)
            canvas.drawLine(posX, posY + 20f, posX - (armSwing * direction), posY + 20f + data.armLength, paint)
        }

        val legSwing = sin(walkCycle) * data.legLength
        canvas.drawLine(posX, torsoBottomY, posX + legSwing, torsoBottomY + data.legLength, paint)
        canvas.drawLine(posX, torsoBottomY, posX - legSwing, torsoBottomY + data.legLength, paint)
    }

    private fun drawSpeechBubble(canvas: Canvas, x: Float, y: Float, text: String) {
        val rectPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        
        val textWidth = textPaint.measureText(text)
        val padding = 20f
        val rect = RectF(x - (textWidth/2) - padding, y - 60f, x + (textWidth/2) + padding, y)
        
        canvas.drawRoundRect(rect, 15f, 15f, rectPaint)
        canvas.drawRoundRect(rect, 15f, 15f, borderPaint)
        canvas.drawText(text, x - (textWidth/2), y - 18f, textPaint)
    }
}
