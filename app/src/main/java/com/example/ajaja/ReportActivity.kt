package com.example.ajaja

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var chartWeek: LineChart
    private lateinit var tvSelectedMonth: TextView
    private lateinit var layoutSelectMonth: LinearLayout

    private lateinit var tabMonth: TextView
    private lateinit var tabAll: TextView

    private val startYear = 2025
    private val startMonth = 11 // 가입 월

    private var isAllMode = false   // ← 전체 탭인지 여부 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        chartWeek = findViewById(R.id.chartWeek)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        layoutSelectMonth = findViewById(R.id.layoutSelectMonth)

        tabMonth = findViewById(R.id.tabMonth)
        tabAll = findViewById(R.id.tabAll)

        // 🔵 월 탭 클릭
        tabMonth.setOnClickListener {
            isAllMode = false
            selectTab(tabMonth)

            tvSelectedMonth.text = "2025년 12월"
            setupChartForMonth()
            updateChartByMonth(2025, 12)

            chartWeek.visibility = View.VISIBLE
            layoutSelectMonth.visibility = View.VISIBLE
        }

        // 🔵 전체 탭 클릭
        tabAll.setOnClickListener {
            isAllMode = true
            selectTab(tabAll)

            tvSelectedMonth.text = "2025년"
            setupChartForYear()
            updateChartByYear(2025)

            chartWeek.visibility = View.VISIBLE
            layoutSelectMonth.visibility = View.VISIBLE
        }

        // 기본 초기 탭 = 월
        isAllMode = false
        selectTab(tabMonth)
        setupChartForMonth()
        updateChartByMonth(2025, 12)

        layoutSelectMonth.setOnClickListener {
            openPicker()
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_dark)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    // 🔵 탭 선택 UI
    private fun selectTab(selected: TextView) {
        val tabs = listOf(tabMonth, tabAll)
        tabs.forEach {
            if (it == selected) {
                it.setBackgroundResource(R.drawable.tab_selected)
                it.setTextColor(Color.WHITE)
            } else {
                it.setBackgroundResource(R.drawable.tab_unselected)
                it.setTextColor(Color.BLACK)
            }
        }
    }

    // ---------------------------------------------------------
    // 📌 월 모드 (기존 그래프)
    // ---------------------------------------------------------
    private fun setupChartForMonth() {
        setupCommonChart()

        val xAxis = chartWeek.xAxis
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 4f
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("1주차", "2주차", "3주차", "4주차", "5주차")
        )
    }

    private fun updateChartByMonth(year: Int, month: Int) {
        val weekCounts = IntArray(5) { 0 }

        val files = filesDir.listFiles { file ->
            file.name.startsWith("homework_") && file.name.endsWith(".jpg")
        } ?: return

        for (file in files) {
            val date = Date(file.lastModified())
            val cal = Calendar.getInstance().apply { time = date }

            if (cal.get(Calendar.YEAR) == year && (cal.get(Calendar.MONTH) + 1) == month) {
                val week = cal.get(Calendar.WEEK_OF_MONTH)
                if (week in 1..5) weekCounts[week - 1] += 1
            }
        }

        drawLineChart(weekCounts.toList(), "주차별 과제 제출량")
    }

    // ---------------------------------------------------------
    // 📌 전체(연도) 모드 - 1월~12월 그래프
    // ---------------------------------------------------------
    private fun setupChartForYear() {
        setupCommonChart()

        val xAxis = chartWeek.xAxis
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 11f
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("1월", "2월", "3월", "4월", "5월", "6월",
                "7월", "8월", "9월", "10월", "11월", "12월")
        )
    }

    private fun updateChartByYear(year: Int) {
        val monthCounts = IntArray(12) { 0 }

        val files = filesDir.listFiles { file ->
            file.name.startsWith("homework_") && file.name.endsWith(".jpg")
        } ?: return

        for (file in files) {
            val date = Date(file.lastModified())
            val cal = Calendar.getInstance().apply { time = date }

            if (cal.get(Calendar.YEAR) == year) {
                val monthIndex = cal.get(Calendar.MONTH) // 0~11
                monthCounts[monthIndex] += 1
            }
        }

        drawLineChart(monthCounts.toList(), "월별 과제 제출량")
    }

    // ---------------------------------------------------------
    // 공통 그래프 설정
    // ---------------------------------------------------------
    private fun setupCommonChart() {
        val legend = chartWeek.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 14f
        legend.formSize = 14f
        legend.yOffset = 10f
        legend.textColor = Color.BLACK
        legend.typeface = android.graphics.Typeface.DEFAULT_BOLD

        chartWeek.axisRight.isEnabled = false
        chartWeek.description.isEnabled = false

        chartWeek.setScaleEnabled(false)
        chartWeek.setPinchZoom(false)
        chartWeek.isDoubleTapToZoomEnabled = false

        chartWeek.axisLeft.textColor = Color.BLACK
    }

    // ---------------------------------------------------------
    // 공통: 데이터 그려주는 함수
    // ---------------------------------------------------------
    private fun drawLineChart(values: List<Int>, label: String) {

        val entries = ArrayList<Entry>()
        for (i in values.indices) {
            entries.add(Entry(i.toFloat(), values[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = Color.parseColor("#00AFFF")
            setCircleColor(Color.parseColor("#00AFFF"))
            lineWidth = 3f
            circleRadius = 5f
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val maxValue = values.maxOrNull() ?: 0
        val yMax = if (maxValue == 0) 1 else maxValue + 1

        val yAxis = chartWeek.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = yMax.toFloat()
        yAxis.setLabelCount(yMax + 1, true)

        chartWeek.data = LineData(dataSet)
        chartWeek.invalidate()
    }

    // ---------------------------------------------------------
    // 📌 월 / 연도 선택 BottomSheet
    // ---------------------------------------------------------
    private fun openPicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_month_picker, null)
        dialog.setContentView(view)

        val listYear = view.findViewById<ListView>(R.id.listYear)
        val listMonth = view.findViewById<ListView>(R.id.listMonth)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (startYear..currentYear).toList().reversed()

        listYear.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, years)

        if (isAllMode) {
            // 📌 전체 모드: 연도만 선택 가능
            listMonth.visibility = View.GONE

            listYear.setOnItemClickListener { _, _, pos, _ ->
                val year = years[pos]
                tvSelectedMonth.text = "${year}년"
                updateChartByYear(year)
                dialog.dismiss()
            }

        } else {
            // 📌 월 모드
            listMonth.visibility = View.VISIBLE

            var selectedYear = years.first()

            fun loadMonths(year: Int) {
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val months =
                    if (year == startYear)
                        (startMonth..12).toList()
                    else if (year == currentYear)
                        (1..currentMonth).toList()
                    else
                        (1..12).toList()

                listMonth.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, months)

                listMonth.setOnItemClickListener { _, _, pos, _ ->
                    val month = months[pos]
                    tvSelectedMonth.text = "${selectedYear}년 ${month}월"
                    updateChartByMonth(selectedYear, month)
                    dialog.dismiss()
                }
            }

            loadMonths(selectedYear)

            listYear.setOnItemClickListener { _, _, pos, _ ->
                selectedYear = years[pos]
                loadMonths(selectedYear)
            }
        }

        dialog.show()
    }
}
