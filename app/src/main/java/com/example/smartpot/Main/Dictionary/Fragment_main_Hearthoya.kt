package com.example.smartpot.Main.Dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smartpot.R
import com.example.smartpot.Main.Fragment_Main_page.NowData
import kotlin.math.roundToInt

class Fragment_main_Hearthoya : Fragment(), UpdatableFragment {

    private var pageNumber: Int = 1
    private var temperatureTextView: TextView? = null
    private var humidityTextView: TextView? = null
    private var moistureTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_hearthoya, container, false)
        temperatureTextView = rootView.findViewById(R.id.TextView_Hearthoya_temperature)
        humidityTextView = rootView.findViewById(R.id.TextView_Hearthoya_humidity)
        moistureTextView = rootView.findViewById(R.id.TextView_Hearthoya_moisture)
        return rootView
    }

    override fun updateUI(nowData: NowData) {
        temperatureTextView?.text = "${nowData.temperature?.roundToInt()} ℃"
        humidityTextView?.text = "${nowData.humidity?.roundToInt()} %"
        moistureTextView?.text = "${nowData.soilMoistureADC} %" // Assuming 'moisture' exists in NowData
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"

        fun newInstance(pageNumber: Int): Fragment_main_Hearthoya {
            val fragment = Fragment_main_Hearthoya()
            val args = Bundle()
            args.putInt(ARG_PAGE_NUMBER, pageNumber)
            fragment.arguments = args
            return fragment
        }
    }
}