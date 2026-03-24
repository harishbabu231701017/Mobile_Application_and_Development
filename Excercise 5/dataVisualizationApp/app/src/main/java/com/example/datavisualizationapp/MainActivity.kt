package com.example.datavisualizationapp

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup

class MainActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var editTextAmount: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var chartContainer: LinearLayout
    private lateinit var barChartContainer: View
    private lateinit var lineChartContainer: FrameLayout
    private lateinit var pieChartContainer: FrameLayout
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    private val dayValues = mutableMapOf<String, Float>()
    private val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    private val chartColors = intArrayOf(
        Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"), Color.parseColor("#96CEB4"),
        Color.parseColor("#FFEEAD"), Color.parseColor("#D4A5A5"),
        Color.parseColor("#9DE0AD")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinner = findViewById(R.id.spinner)
        editTextAmount = findViewById(R.id.editTextAmount)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        chartContainer = findViewById(R.id.chartContainer)
        barChartContainer = findViewById(R.id.barChartContainer)
        lineChartContainer = findViewById(R.id.lineChartContainer)
        pieChartContainer = findViewById(R.id.pieChartContainer)
        toggleGroup = findViewById(R.id.chartToggleGroup)

        days.forEach { dayValues[it] = 0f }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        buttonSubmit.setOnClickListener {
            val selectedDay = spinner.selectedItem.toString()
            val amountStr = editTextAmount.text.toString()

            if (amountStr.isNotEmpty()) {
                dayValues[selectedDay] = amountStr.toFloat()
                updateCurrentChart()
                editTextAmount.text.clear()
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                barChartContainer.visibility = if (checkedId == R.id.btnBar) View.VISIBLE else View.GONE
                lineChartContainer.visibility = if (checkedId == R.id.btnLine) View.VISIBLE else View.GONE
                pieChartContainer.visibility = if (checkedId == R.id.btnPie) View.VISIBLE else View.GONE
                updateCurrentChart()
            }
        }

        updateCurrentChart()
    }

    private fun updateCurrentChart() {
        when (toggleGroup.checkedButtonId) {
            R.id.btnBar -> updateBarChart()
            R.id.btnLine -> updateLineChart()
            R.id.btnPie -> updatePieChart()
        }
    }

    private fun updateBarChart() {
        chartContainer.removeAllViews()
        val maxAmount = dayValues.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val maxHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280f, resources.displayMetrics)
        val scaleFactor = maxHeightPx / maxAmount

        for ((index, day) in days.withIndex()) {
            val amount = dayValues[day] ?: 0f
            val dayLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt(),
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply { setMargins(8, 0, 8, 0) }
            }

            if (amount > 0) {
                dayLayout.addView(TextView(this).apply {
                    text = amount.toInt().toString()
                    gravity = Gravity.CENTER
                    textSize = 10f
                    setTextColor(Color.GRAY)
                })
            }

            dayLayout.addView(View(this).apply {
                val barHeight = (amount * scaleFactor).toInt().coerceAtLeast(4)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeight)
                val barDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(chartColors[index % chartColors.size])
                    cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
                }
                background = barDrawable
            })

            dayLayout.addView(TextView(this).apply {
                text = day.take(3)
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 0)
                textSize = 12f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            })
            chartContainer.addView(dayLayout)
        }
    }

    private fun updateLineChart() {
        lineChartContainer.removeAllViews()
        lineChartContainer.addView(LineChartView(this, dayValues, days, chartColors[0]))
    }

    private fun updatePieChart() {
        pieChartContainer.removeAllViews()
        pieChartContainer.addView(PieChartView(this, dayValues, days, chartColors))
    }

    class LineChartView(context: Context, val data: Map<String, Float>, val labels: Array<String>, val lineColor: Int) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 8f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lineColor }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }

        override fun onDraw(canvas: Canvas) {
            val max = data.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val padding = 100f
            val w = width.toFloat() - 2 * padding
            val h = height.toFloat() - 2 * padding
            val stepX = if (labels.size > 1) w / (labels.size - 1) else 0f
            
            val path = Path()
            for (i in labels.indices) {
                val x = padding + i * stepX
                val y = height - padding - (data[labels[i]] ?: 0f) / max * h
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                canvas.drawCircle(x, y, 12f, pointPaint)
                canvas.drawText(labels[i].take(3), x, height - 20f, textPaint)
            }
            paint.color = lineColor
            canvas.drawPath(path, paint)
        }
    }

    class PieChartView(context: Context, val data: Map<String, Float>, val labels: Array<String>, val chartColors: IntArray) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val rect = RectF()

        override fun onDraw(canvas: Canvas) {
            val total = data.values.sum().coerceAtLeast(1f)
            val padding = 100f
            rect.set(padding, padding, width.toFloat() - padding, height.toFloat() - padding)
            
            var startAngle = 0f
            for (i in labels.indices) {
                val sweep = (data[labels[i]] ?: 0f) / total * 360f
                if (sweep > 0) {
                    paint.color = chartColors[i % chartColors.size]
                    canvas.drawArc(rect, startAngle, sweep, true, paint)
                }
                startAngle += sweep
            }
        }
    }
}