package com.example.smartpot

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs


class Fragment_Main_page: Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var addButton: Button
    private lateinit var addText: TextView
    private lateinit var addImage: ImageView
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var chart: LineChart
    private val fragments = ArrayList<Fragment>()
    // 페이지 이동에 따라 크기가 줄어들고 왼쪽으로 이동하는 효과를 주는 PageTransformer 클래스
    class CardsPagerTransformerShift(
        private val baseElevation: Int,
        private val raisingElevation: Int,
        private val smallerScale: Float,
        private val startOffset: Float
    ) : ViewPager2.PageTransformer {

        // transformPage 메서드는 각 페이지에 대한 변환을 처리합니다.
        override fun transformPage(page: View, position: Float) {
            // position 값의 절댓값을 계산
            val absPosition = Math.abs(position - startOffset)

            // absPosition이 1보다 크거나 같으면 페이지가 완전히 사라진 상태이므로 초기 설정으로 되돌립니다.
            if (absPosition >= 1) {
                page.elevation = baseElevation.toFloat()
                page.scaleY = smallerScale
            } else {
                // 페이지가 변환 중인 상태

                // 페이지의 고도를 계산하여 설정
                page.elevation = ((1 - absPosition) * raisingElevation + baseElevation).toFloat()

                // 페이지의 세로 크기를 계산하여 설정
                page.scaleY = (smallerScale - 1) * absPosition + 1
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.page_main, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        addButton = view.findViewById(R.id.addButton)
        addText = view.findViewById(R.id.addText)
        addImage = view.findViewById(R.id.addImage)
        indicatorLayout = view.findViewById(R.id.indicatorLayout)

        chart = view.findViewById(R.id.plant_water_chart)

        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        viewPager.adapter?.notifyItemInserted(0)
        viewPager.setCurrentItem(1, true)

        addButton.setOnClickListener {
            showAddButtonDialog()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                if (position == fragments.size - 1) {
                    addButton.visibility = View.VISIBLE
                    addText.visibility = View.VISIBLE
                    addImage.visibility = View.VISIBLE
                }else{
                    addButton.visibility = View.GONE
                    addText.visibility = View.GONE
                    addImage.visibility = View.GONE
                }
            }
        })

        addLimitLine()
        setChartData()
        chart.invalidate()
        initChart()

        val viewPagerPadding = resources.getDimensionPixelSize(R.dimen.view_pager_padding)
        val screen = requireActivity().windowManager.defaultDisplay.width
        val startOffset = viewPagerPadding.toFloat() / (screen - 2 * viewPagerPadding)
        viewPager.setPageTransformer(CardsPagerTransformerShift(0, 50, 0.75f, startOffset))

        return view
    }
    private fun showAddButtonDialog() {
        val items = arrayOf("하트호야", "스투키", "선인장", "피쉬본", "괴마옥")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("식물을 선택해주세요")

        builder.setItems(items) { _, which ->
            // 다이얼로그에서 항목을 선택했을 때의 처리를 여기에 추가
            when (which) {
                0 -> addNewPage_Hearthoya()
                1 -> addNewPage_Stucky()
                2 -> addNewPage_Cactus()
                3 -> addNewPage_Fishbone()
                4 -> addNewPage_Haunted_house()
            }
        }

        builder.create().show()
    }
    private fun addLimitLine() {
        // 가로선을 추가할 위치와 라벨 설정
        val limitLine = LimitLine(50f, "50%")


        // 가로선의 스타일 및 속성 설정
        val colors = "#B0E19C"
        limitLine.lineColor = Color.parseColor(colors)
        limitLine.lineWidth = 1f

        limitLine.enableDashedLine(20f, 20f, 0f)
        limitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        // 가로선을 왼쪽 Y 축에 추가
        val yAxisLeft: YAxis = chart.axisLeft
        yAxisLeft.addLimitLine(limitLine)
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 100f
    }

    private fun updateIndicators(currentPosition: Int) {
        // Do not remove existing views, just update the existing ones and add new ones

        // Check if the number of indicators matches the number of fragments
        val existingIndicatorCount = indicatorLayout.childCount

        for (i in fragments.indices) {
            val indicator: View

            if (i < existingIndicatorCount) {
                // If the indicator already exists, reuse it
                indicator = indicatorLayout.getChildAt(i)
            } else {
                // If the indicator does not exist, create a new one
                indicator = View(context)
                val indicatorSize = resources.getDimensionPixelSize(R.dimen.indicator_size)
                val indicatorMargin = resources.getDimensionPixelSize(R.dimen.indicator_margin)
                val params = LinearLayout.LayoutParams(indicatorSize, indicatorSize)
                params.setMargins(indicatorMargin, 0, indicatorMargin, 0)
                indicator.layoutParams = params
                indicatorLayout.addView(indicator)
            }

            // Update the background resource based on the current position
            indicator.setBackgroundResource(
                if (i == currentPosition) R.drawable.icon_main_selected_indicator
                else R.drawable.icon_main_unselected_indiactor
            )
        }
    }



    fun addNewPage_Hearthoya() {
        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }

        fragments.add(Fragment_main_Hearthoya.newInstance(fragments.size + 1))
        addNewPage2()
    }
    fun addNewPage_Stucky() {
        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }

        fragments.add(Fragment_main_Stucky.newInstance(fragments.size + 1))
        addNewPage2()
    }
    fun addNewPage_Cactus() {
        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }

        fragments.add(Fragment_main_Cactus.newInstance(fragments.size + 1))
        addNewPage2()
    }
    fun addNewPage_Fishbone() {
        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }

        fragments.add(Fragment_main_Fishbone.newInstance(fragments.size + 1))
        addNewPage2()
    }
    fun addNewPage_Haunted_house() {
        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }

        fragments.add(Fragment_main_Haunted_house.newInstance(fragments.size + 1))
        addNewPage2()
    }

    fun addNewPage2() {
        val currentPosition = viewPager.currentItem

        fragments.add(Fragment_Blank2.newInstance(fragments.size + 1))
        viewPager.adapter?.notifyItemInserted(currentPosition + 1)
        viewPager.setCurrentItem(0, true)
    }
    private fun initChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisLineWidth = 2f

        xAxis.setCenterAxisLabels(false)

        chart.axisRight.isEnabled = false
        chart.axisLeft.isEnabled = false
        val xLabels = chart.xAxis

        xLabels.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val currentDate = getCurrentDate()
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_MONTH, value.toInt() - 7)
                val formattedDate = SimpleDateFormat("MM/dd", Locale.getDefault()).format(calendar.time)
                val dayOfWeek = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)
                return "$formattedDate"
            }
        }

        // Y 축 설정
        val yAxisLeft: YAxis = chart.axisLeft
        yAxisLeft.isEnabled = true // 왼쪽 Y 축 활성화
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 100f
        yAxisLeft.setDrawLabels(false)
        yAxisLeft.setDrawGridLines(false)

        val yAxisRight: YAxis = chart.axisRight
        yAxisRight.isEnabled = false // 오른쪽 Y 축 비활성화
        addLimitLine()
        setChartData()
        chart.invalidate()
    }

    private fun getCurrentDate(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }

    private fun setChartData() {
        val entries = mutableListOf<Entry>()

        // 1~7일 동안의 1~7의 값을 가지는 데이터를 entries에 추가
        entries.add(Entry(1.toFloat(), 10.toFloat()))
        entries.add(Entry(2.toFloat(), 20.toFloat()))
        entries.add(Entry(3.toFloat(), 30.toFloat()))
        entries.add(Entry(4.toFloat(), 40.toFloat()))
        entries.add(Entry(5.toFloat(), 50.toFloat()))
        entries.add(Entry(6.toFloat(), 60.toFloat()))
        entries.add(Entry(7.toFloat(), 70.toFloat()))

        val colorString = "#54B22D"
        val color = Color.parseColor(colorString)
        val dataSet = LineDataSet(entries, null)

        dataSet.color = color
        dataSet.setFormSize(10f) // 아이콘 크기를 0으로 설정

        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}