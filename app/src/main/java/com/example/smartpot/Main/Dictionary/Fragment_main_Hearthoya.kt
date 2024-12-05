// 파일: com/example/smartpot/Main/Dictionary/Fragment_main_Hearthoya.kt
package com.example.smartpot.Main.Dictionary

import android.app.AlertDialog
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class Fragment_main_Hearthoya : Fragment(), UpdatableFragment {

    private var pageNumber: Int = 1
    private var deviceId: String? = null

    private var email:String? = null

    private var temperatureTextView: TextView? = null
    private var humidityTextView: TextView? = null
    private var moistureTextView: TextView? = null
    private var batteryImageView: ImageView? =null
    private var syncButton: Button? =null

    private var detailTextView: TextView?=null
    private var titleTextView: TextView?=null
    private var titleImg: ImageView?=null

    // SharedViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
            deviceId = it.getString(ARG_DEVICE_ID)
            email = it.getString(ARG_EMAIL)
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
        detailTextView = rootView.findViewById(R.id.TextView_Hearthoya_Detail)
        titleTextView = rootView.findViewById(R.id.TextView_Hearthoya_Title)
        titleImg = rootView.findViewById(R.id.TextView_Hearthoya_TitleImg)
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
        batteryImageView?.setImageResource(R.drawable.icon_bat_empty)
        // 버튼 리스너 설정
        view.findViewById<Button>(R.id.harthoya_grow)?.setOnClickListener {
            val customDialogFragment = Fragment_CustomDialog_hearhoya()
            customDialogFragment.show(parentFragmentManager, "custom_dialog")
        }

        // 루트 뷰에 롱 클릭 리스너 추가
        view.setOnLongClickListener {
            // 롱 클릭 시 수행할 작업
            showAddButtonDialog()
            true // 이벤트 소비를 나타내기 위해 true 반환
        }
    }

    fun updateUserNameAtIndex(username: String, index: Int, newName: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(username) // 'username'이 사용자 식별자로 사용된다고 가정

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentNames = snapshot.get("name") as? MutableList<String>

            if (currentNames != null) {
                if (index in 0 until currentNames.size) {
                    currentNames[index] = newName
                    transaction.set(userRef, mapOf("name" to currentNames), SetOptions.merge())
                    Log.d("Firestore", "Name at index $index updated to $newName for user $username")
                } else {
                    throw Exception("Index $index is out of bounds for the 'name' array.")
                }
            } else {
                throw Exception("'name' field does not exist or is not a list.")
            }
        }.addOnSuccessListener {
            // 업데이트 성공 처리
//            Toast.makeText(context, "이름이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            // 오류 처리
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                // 문서가 존재하지 않는 경우 새로 생성
                val newUserData = mapOf(
                    "name" to listOf(newName) // 'name' 필드를 새 리스트로 설정
                )
                userRef.set(newUserData)
                    .addOnSuccessListener {
                        Log.d("Firestore", "새 사용자 생성 및 이름 추가 성공")
//                        Toast.makeText(context, "새 사용자 생성 및 이름이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { ex ->
                        Log.e("Firestore", "사용자 생성 오류: ${ex.message}")
//                        Toast.makeText(context, "사용자 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.e("Firestore", "사용자 데이터 업데이트 오류: ${e.message}")
//                Toast.makeText(context, "사용자 데이터 업데이트 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun changeToHearthoya(){
        titleTextView?.text = "하트호야"
        detailTextView?.text = "통통한 하트모양의 잎이 사랑스러운"
        titleImg?.setImageResource(R.drawable.icon_main_harthoya)
        updateUserNameAtIndex(email.toString(),pageNumber-1,"하트호야")
    }

    private fun changeToStucky(){
        titleTextView?.text = "스투키"
        detailTextView?.text = "간결하고 강렬한 스타일로 독보적인 모습을 자랑하는 신선한"
        titleImg?.setImageResource(R.drawable.icon_main_stucky)
        updateUserNameAtIndex(email.toString(),pageNumber-1,"스투키")
    }
    private fun changeToCactus(){
        titleTextView?.text = "선인장"
        detailTextView?.text = "작은 공간도 화사하게 만드는 친구"
        titleImg?.setImageResource(R.drawable.icon_main_cactus)
        updateUserNameAtIndex(email.toString(),pageNumber-1,"선인장")
    }
    private fun changeToFishbone(){
        titleTextView?.text = "피쉬본"
        detailTextView?.text = "바다의 미묘한 아름다움을 담은 독특하고 세련된 물고기 무늬의 플랜트"
        titleImg?.setImageResource(R.drawable.icon_main_fishbone)
        updateUserNameAtIndex(email.toString(),pageNumber-1,"피쉬본")
    }
    private fun changeToHaunted_house(){
        titleTextView?.text = "괴마옥"
        detailTextView?.text = "신비로운 아름다움과 독특한 형태로 눈길을 사로잡는"
        titleImg?.setImageResource(R.drawable.icon_main_haunted_house)
        updateUserNameAtIndex(email.toString(),pageNumber-1,"괴마옥")
    }

    private fun showAddButtonDialog() {
        val items = arrayOf("하트호야", "스투키", "선인장", "피쉬본", "괴마옥")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("식물을 선택해주세요")

        builder.setItems(items) { _, which ->
            // 다이얼로그에서 항목을 선택했을 때의 처리를 여기에 추가
            when (which) {
                0 -> changeToHearthoya()
                1 -> changeToStucky()
                2 -> changeToCactus()
                3 -> changeToFishbone()
                4 -> changeToHaunted_house()
            }
        }

        builder.create().show()
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
            in 10..24 -> batteryImageView?.setImageResource(R.drawable.icon_bat_low)
            else -> batteryImageView?.setImageResource(R.drawable.icon_bat_empty)
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
        private const val ARG_EMAIL = "device_name"

        fun newInstance(pageNumber: Int, deviceId: String, email: String): Fragment_main_Hearthoya {
            val fragment = Fragment_main_Hearthoya()
            val args = Bundle()
            args.putInt(ARG_PAGE_NUMBER, pageNumber)
            args.putString(ARG_DEVICE_ID, deviceId)
            args.putString(ARG_EMAIL, email)
            fragment.arguments = args
            return fragment
        }
    }
}
