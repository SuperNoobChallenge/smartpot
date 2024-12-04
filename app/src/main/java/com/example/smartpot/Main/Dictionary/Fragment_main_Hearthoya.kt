// 파일: com/example/smartpot/Main/Dictionary/Fragment_main_Hearthoya.kt
package com.example.smartpot.Main.Dictionary

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.smartpot.Main.SharedViewModel
import com.example.smartpot.Main.Fragment_Main_page.NowData
import com.example.smartpot.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class Fragment_main_Hearthoya : Fragment(), UpdatableFragment {

    private var pageNumber: Int = 1
    private var deviceId: String? = null

    private var temperatureTextView: TextView? = null
    private var humidityTextView: TextView? = null
    private var moistureTextView: TextView? = null
    private var batteryImageView: ImageView? =null
    private var syncButton: Button? =null

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
        syncButton = rootView.findViewById(R.id.Hearthoya_is_sync)
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
        // 프래그먼트가 첨부된 상태인지 확인
        if (!isAdded) {
            Log.w("Fragment_main_Hearthoya", "Fragment is not attached. Skipping UI update.")
            return
        }

        // 온도, 습도, 수분 텍스트 업데이트
        temperatureTextView?.text = "${nowData.temperature?.roundToInt()} ℃"
        humidityTextView?.text = "${nowData.humidity?.roundToInt()} %"
        moistureTextView?.text = (nowData.soilMoistureADC?.let {
            ((4095 - it.toFloat()) / 4095.0f) * 100
        } ?: 0f).roundToInt().toString() + " %"

        // 배터리 이미지 업데이트
        when (nowData.batteryPercentageRound ?: 0) {
            in 75..100 -> batteryImageView?.setImageResource(R.drawable.icon_bat_max)
            in 50..74 -> batteryImageView?.setImageResource(R.drawable.icon_bat_hi)
            in 25..49 -> batteryImageView?.setImageResource(R.drawable.icon_bat_mid)
            else -> batteryImageView?.setImageResource(R.drawable.icon_bat_low)
        }

        // currentTime과 현재 시스템 시간 비교
        nowData.currentTime?.let { timeString ->
            val dateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC") // 필요에 따라 타임존 설정
            try {
                val dataTime: Date = dateFormat.parse(timeString) ?: throw Exception("Parsed Date is null")
                val currentTime: Date = Date() // 현재 시스템 시간

                // 밀리초 단위 차이 계산
                val diffInMillis: Long = currentTime.time - dataTime.time

                // 분 단위로 변환
                val diffInMinutes: Long = diffInMillis / (1000 * 60)

                if (diffInMinutes >= 30) {
                    // 30분 이상 차이가 날 경우 수행할 작업
                    syncButton?.setTextColor(Color.parseColor("#FF4500"))
                    syncButton?.setBackgroundResource(R.drawable.costom_button_main_3)
                    syncButton?.text = "기기연동 X"
                } else {
                    // 데이터가 최신일 경우 텍스트 색상을 기본 색상으로 설정
                    syncButton?.setTextColor(Color.parseColor("#54B22D"))
                    syncButton?.setBackgroundResource(R.drawable.costom_button_main_2)
                    syncButton?.text = "기기연동 O"
                }
            } catch (e: Exception) {
                Log.e("TimeComparison", "Error parsing currentTime: $timeString", e)
                // 파싱 에러 처리 (예: 사용자에게 알림)
                Toast.makeText(requireContext(), "시간 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
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
