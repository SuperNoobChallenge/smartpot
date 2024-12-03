// 파일: com/example/smartpot/Main/Dictionary/Fragment_main_Hearthoya.kt
package com.example.smartpot.Main.Dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smartpot.Main.SharedViewModel
import com.example.smartpot.Main.Fragment_Main_page.NowData
import com.example.smartpot.R
import kotlin.math.roundToInt

class Fragment_main_Hearthoya : Fragment(), UpdatableFragment {

    private var pageNumber: Int = 1
    private var deviceId: String? = null

    private var temperatureTextView: TextView? = null
    private var humidityTextView: TextView? = null
    private var moistureTextView: TextView? = null
    private var batteryImageView: ImageView? =null

    // SharedViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
            deviceId = it.getString(ARG_DEVICE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_hearthoya, container, false)
        temperatureTextView = rootView.findViewById(R.id.TextView_Hearthoya_temperature)
        humidityTextView = rootView.findViewById(R.id.TextView_Hearthoya_humidity)
        moistureTextView = rootView.findViewById(R.id.TextView_Hearthoya_moisture)
        batteryImageView = rootView.findViewById(R.id.ImageView_Hearthoya_battery)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel 초기화
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // ViewModel 관찰
        deviceId?.let { id ->
            sharedViewModel.nowDataMap.observe(viewLifecycleOwner) { nowDataMap ->
                val nowData = nowDataMap[id]
                if (nowData != null) {
                    updateUI(nowData)
                }
            }
        }

        // 초기 UI 설정
        temperatureTextView?.text = "00 ℃"
        humidityTextView?.text = "00 %"
        moistureTextView?.text = "00 %"
        batteryImageView?.setImageResource(R.drawable.icon_bat_low)
        // 버튼 리스너 설정
        view.findViewById<Button>(R.id.harthoya_grow)?.setOnClickListener {
            val customDialogFragment = Fragment_CustomDialog_hearhoya()
            customDialogFragment.show(parentFragmentManager, "custom_dialog")
        }
    }

    override fun updateUI(nowData: NowData) {
        temperatureTextView?.text = "${nowData.temperature?.roundToInt()} ℃"
        humidityTextView?.text = "${nowData.humidity?.roundToInt()} %"
        moistureTextView?.text = (nowData.soilMoistureADC?.let {
            ((4095-it.toFloat()) / 4095.0f) * 100
        }
            ?: 0f).roundToInt().toString()+" %" // avgHumidity를 사용합니다.
        if(nowData.batteryPercentageRound ?: 0 >= 75){
            batteryImageView?.setImageResource(R.drawable.icon_bat_max)
        }else if(nowData.batteryPercentageRound ?: 0 >= 50){
            batteryImageView?.setImageResource(R.drawable.icon_bat_hi)
        }else if(nowData.batteryPercentageRound ?: 0 >= 25){
            batteryImageView?.setImageResource(R.drawable.icon_bat_mid)
        }else{
            batteryImageView?.setImageResource(R.drawable.icon_bat_low)
        }
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"
        private const val ARG_DEVICE_ID = "device_id"

        fun newInstance(pageNumber: Int, deviceId: String): Fragment_main_Hearthoya {
            val fragment = Fragment_main_Hearthoya()
            val args = Bundle()
            args.putInt(ARG_PAGE_NUMBER, pageNumber)
            args.putString(ARG_DEVICE_ID, deviceId)
            fragment.arguments = args
            return fragment
        }
    }
}
