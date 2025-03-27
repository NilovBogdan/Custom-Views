package ru.netology.statsview.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)
    private var animator: Animator? = null
    private var progress = 0F

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        var startFrom = -90F

        val text = data.sum() / data.maxOrNull()!! * 100F / data.count()

        canvas.drawText(
            "%.2f%%".format(text),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
        if (text == 100F) {
            paint.color = colors[0]
            canvas.drawArc(oval, startFrom + rotation, 1F, false, paint)
        }


        val max = data.sum() / data.maxOrNull()!! / data.count() * 360F
        val progressAngle = progress * 360F



        if (progressAngle > max) {
            for ((index, datum) in data.withIndex()) {
                val angle = (datum / data.maxOrNull()!!.times(data.count())) * 360F
                paint.color = colors.getOrNull(index) ?: randomColor()
                canvas.drawArc(oval, startFrom, angle , false, paint)
                startFrom += angle
            }
            return
        }

        var filled = 0F

        for ((index, datum) in data.withIndex()) {
            val angle = (datum / data.maxOrNull()!!.times(data.count())) * 360F
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, progressAngle - filled, false, paint)
            startFrom += angle
            filled += angle
            if (filled > progressAngle) return
        }

//        for ((index, datum) in data.withIndex()) {
//            val angle = (datum / data.maxOrNull()!!.times(data.count())) * 360F
//            paint.color = colors.getOrNull(index) ?: randomColor()
//            canvas.drawArc(oval, startFrom, angle, false, paint)
//            startFrom += angle
//        }


    }

    private fun update() {
        animator?.let {
            it.removeAllListeners()
            it.cancel()
        }

        progress = 0F

        animator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 5_000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }


private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}