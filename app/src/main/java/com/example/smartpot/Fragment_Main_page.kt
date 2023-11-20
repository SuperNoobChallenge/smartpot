package com.example.smartpot

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
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

class Fragment_Main_page: Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var addButton: Button
    private lateinit var addText: TextView
    private lateinit var addImage: ImageView
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var chart: LineChart
    private val fragments = ArrayList<Fragment>()

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

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
        viewPager.adapter?.notifyItemInserted(0)
        viewPager.setCurrentItem(1, true)

        addButton.setOnClickListener {
            showAddButtonDialog()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
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
        initChart()
        setChartData()

        return view
    }
    private fun showAddButtonDialog() {
        val items = arrayOf("Heart Hoya", "Stucky", "Cactus", "Fish Bone", "Devil Orchid")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose a plant")

        builder.setItems(items) { _, which ->
            // 다이얼로그에서 항목을 선택했을 때의 처리를 여기에 추가
            when (which) {
                0 -> addNewPage() // Heart Hoya 선택 시
                // 다른 항목에 대한 처리 추가
            }
        }

        builder.create().show()
    }

    private fun addLimitLine() {
        val limitLine = LimitLine(50f, "Limit") // 가로 점선의 위치 (50%)와 라벨 설정
        limitLine.lineColor = Color.RED // 점선 색상 설정
        limitLine.lineWidth = 2f // 점선 두께 설정

        val leftAxis: YAxis = chart.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.addLimitLine(limitLine)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
    }

    private fun updateIndicators(currentPosition: Int) {
        indicatorLayout.removeAllViews()
        for (i in fragments.indices) {
            val indicator = View(context)
            val indicatorSize = resources.getDimensionPixelSize(R.dimen.indicator_size)
            val indicatorMargin = resources.getDimensionPixelSize(R.dimen.indicator_margin)
            val params = LinearLayout.LayoutParams(indicatorSize, indicatorSize)
            params.setMargins(indicatorMargin, 0, indicatorMargin, 0)
            indicator.layoutParams = params
            indicator.setBackgroundResource(
                if (i == currentPosition) R.drawable.icon_main_selected_indicator
                else R.drawable.icon_main_unselected_indiactor
            )
            indicatorLayout.addView(indicator)
        }
    }


    fun addNewPage() {
        val currentPosition = viewPager.currentItem

        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }

        fragments.add(Fragment_Blank.newInstance(fragments.size + 1))
        updateIndicators(0)
        addNewPage2()
    }
    fun addNewPage2() {
        val currentPosition = viewPager.currentItem

        fragments.add(Fragment_Blank2.newInstance(fragments.size + 1))
        viewPager.adapter?.notifyItemInserted(currentPosition + 1)
        viewPager.setCurrentItem(0, true)
        updateIndicators(0)
    }
    private fun initChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisLineWidth = 3f

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val currentDate = getCurrentDate()
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_MONTH, value.toInt() - 6) // -6을 추가하여 역순으로 날짜를 계산
                val formattedDate = SimpleDateFormat("MM/dd", Locale.getDefault()).format(calendar.time)
                return formattedDate
            }
        }

        xAxis.setCenterAxisLabels(false)

        chart.axisRight.isEnabled = false
        chart.axisLeft.isEnabled = false
        val xLabels = chart.xAxis

        xLabels.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val currentDate = getCurrentDate()
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_MONTH, value.toInt() - 6)
                val formattedDate = SimpleDateFormat("MM/dd", Locale.getDefault()).format(calendar.time)
                val dayOfWeek = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)
                return "$formattedDate"
            }
        }

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
        dataSet.setFormSize(0f) // 아이콘 크기를 0으로 설정
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
