package com.example.smartpot.Main
import android.app.Activity.RESULT_OK
import kotlin.math.roundToInt
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.smartpot.Fragment_Blank2
import com.example.smartpot.Main.Dictionary.Fragment_CustomDialog_hearhoya
import com.example.smartpot.Main.Dictionary.Fragment_main_Cactus
import com.example.smartpot.Main.Dictionary.Fragment_main_Fishbone
import com.example.smartpot.Main.Dictionary.Fragment_main_Haunted_house
import com.example.smartpot.Main.Dictionary.Fragment_main_Hearthoya
import com.example.smartpot.Main.Dictionary.Fragment_main_Stucky
import com.example.smartpot.R
import com.example.smartpot.hardtest
import com.example.smartpot.login.Kakao_login_Activity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.smartpot.Main.Dictionary.UpdatableFragment


class Fragment_Main_page: Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var addButton: Button
    private lateinit var addText: TextView
    private lateinit var addImage: ImageView
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var  yesterdayMoisture: TextView
    private lateinit var  yesterdayTemperature: TextView
    private lateinit var  currentMoisture: TextView
    private lateinit var  currentTemperature: TextView
    private lateinit var chart: LineChart
    private val fragments = ArrayList<Fragment>()
    private lateinit var db: FirebaseFirestore

    // ViewModel 선언
    private lateinit var sharedViewModel: SharedViewModel

    var userDevices: ArrayList<String> = arrayListOf() // 유저 화분 번호 저장
    var currentPosition : Int = 0 // 현재 페이지 index 저장

    var kakaoemail:String = "" //기기 추가할 때 유저 메일이 필요한데 코루틴 처리하기 귀찮아서 임시 변수 하나 구현
    // 기기 추가를 완료했을 때 리프레시를 위한 ActivityResultLauncher 초기화
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var gestureDetector: GestureDetectorCompat

    // 파이어베이스 nowdata 저장용 클레스
    data class NowData(
        val batteryPercentage: Double? = null,
        val batteryPercentageRound: Int? = null,
        val batteryVoltage: Double? = null,
        val currentTime: String? = null,
        val heatIndex: Double? = null,
        val humidity: Double? = null,
        val name: String? = null,
        val soilMoistureADC: Int? = null,
        val soilStatus: String? = null,
        val soilVoltage: Double? = null,
        val temperature: Double? = null,
        val timestamp: Long? = null
    )

    data class UserData(
        val devices: List<String>? = null,
        val name: List<String>? = null
    )

    data class HistoricalData(
        val date: String,
        val avgHumidity: Double?,
        val avgMoistureADC: Double?,
        val avgTemperature: Double?
    )

    object DataHolder {
        var devicesCurrentData: ArrayList<NowData> = ArrayList()
        var historicalDataMap: MutableMap<String, List<HistoricalData>> = mutableMapOf()
        var userDevices: ArrayList<String> = arrayListOf()
        var userDevicesName: ArrayList<String> = arrayListOf()
    }


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
    private lateinit var sharedPreferences: SharedPreferences
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
        yesterdayMoisture = view.findViewById(R.id.yesterday_moisture)
        yesterdayTemperature = view.findViewById(R.id.yesterday_temperature)
        currentMoisture = view.findViewById(R.id.current_moisture)
        currentTemperature = view.findViewById(R.id.current_temperature)
        chart = view.findViewById(R.id.plant_water_chart)

        // ViewModel 초기화
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        viewPager.adapter = pagerAdapter

        viewPager.adapter?.notifyItemInserted(0)
        viewPager.setCurrentItem(1, true)

        addButton.setOnClickListener {
            showAddButtonDialog()
        }


        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance()

        // 이전 기록을 저장하기 위한 변수 DataHolder 초기화
        DataHolder.devicesCurrentData.clear()
        DataHolder.historicalDataMap.clear()
        DataHolder.userDevices.clear()
        DataHolder.userDevicesName.clear()


        val lastPageIndex = sharedPreferences.getInt("lastPageIndex", 0)
        viewPager.setCurrentItem(lastPageIndex, false)

        // GestureDetector 초기화
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                handleLongPress()
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true // 다른 제스처를 인식하기 위해 true 반환
            }
        })

        // ViewPager2 내부의 RecyclerView에 OnItemTouchListener 설정
        val recyclerView = viewPager.getChildAt(0) as? RecyclerView
        recyclerView?.let {
            it.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    gestureDetector.onTouchEvent(e)
                    return false // ViewPager2가 다른 터치 이벤트를 처리할 수 있도록 false 반환
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                    // No action needed
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                    // No action needed
                }
            })
        }


        // 코루틴을 사용하여 데이터 로딩
        lifecycleScope.launch {
            // async-await를 사용하여 순차적 실행 보장
            val userResult = async {
                suspendCoroutine<String> { continuation ->
                    UserApiClient.instance.me { user: User?, error ->
                        if (user != null) {
                            val email = user.kakaoAccount?.email ?: ""
                            kakaoemail = email
                            continuation.resume(email)
                        } else {
                            continuation.resume("")
                        }
                    }
                }
            }

            val username = userResult.await()
            kakaoemail = username

            // username을 사용하여 사용자 데이터 가져오기
            val userData = fetchUserData(username)

            // 나머지 코드는 동일
            if (userData != null) {
                DataHolder.userDevices.addAll(userData.devices ?: emptyList())
                DataHolder.userDevicesName.addAll(userData.name ?: emptyList())

                for ((deviceId, devicename) in DataHolder.userDevices.zip(DataHolder.userDevicesName)) {
                    val nowData = fetchNowData(deviceId)
                    if (nowData != null) {
                        DataHolder.devicesCurrentData.add(nowData)

                        val historicalDataList = fetchHistoricalData(deviceId)
                        DataHolder.historicalDataMap[deviceId] = historicalDataList

                        // "하트호야", "스투키", "선인장", "피쉬본", "괴마옥"
                        withContext(Dispatchers.Main) {
                            addNewPage(devicename, deviceId, kakaoemail)
                        }

                        updateIndicators(0)
                        setNowDataListener(deviceId, currentPosition)
                        setHistoricalDataListener(deviceId, currentPosition)


                        // ViewModel 업데이트
                        sharedViewModel.updateNowData(deviceId, nowData)
                    }
                }
            }else{
                saveUserDataToFirestore(username,"")
            }
        }

        // 파이어베이스 유저 기기 이름 내역에 하트호야 추가
        fun saveUserDataToFirestore(username: String) {
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(kakaoemail)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentNames = snapshot.get("name") as? MutableList<String> ?: mutableListOf()
                currentNames.add("하트호야") // 중복을 허용하여 추가
                transaction.set(userRef, mapOf("name" to currentNames), com.google.firebase.firestore.SetOptions.merge())
            }.addOnSuccessListener {
                // 저장 성공 처리
                Log.d("Firestore", "Device added successfully to $username")
            }.addOnFailureListener { e ->
                // 문서가 없으면 새로 생성
                if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    val newUserData = mapOf(
                        "name" to listOf("하트호야") // devices 필드를 빈 배열로 설정 후 추가
                    )
                    userRef.set(newUserData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "New user created and device added successfully")
                        }
                        .addOnFailureListener { ex ->
                            Log.e("Firestore", "Error creating user: ${ex.message}")
                        }
                } else {
                    Log.e("Firestore", "Error updating user data: ${e.message}")
                }
            }
        }

        fun refreshData(macadd : String) {
            lifecycleScope.launch {
                if (DataHolder.userDevices.indexOf(macadd) == -1) {
                    saveUserDataToFirestore(macadd)
                    DataHolder.userDevices.add(macadd)
                    val nowData = fetchNowData(macadd)
                    if (nowData != null) {
                        DataHolder.devicesCurrentData.add(nowData)

                        val historicalDataList = fetchHistoricalData(macadd)
                        DataHolder.historicalDataMap[macadd] = historicalDataList
                        withContext(Dispatchers.Main) {
                            addNewPage(nowData.name ?: "하트호야", macadd, kakaoemail)
                        }
                        updateIndicators(0)
                        setNowDataListener(macadd, currentPosition)
                        setHistoricalDataListener(macadd, currentPosition)

                        // ViewModel 업데이트
                        sharedViewModel.updateNowData(macadd, nowData)
                    }
                }
            }
        }
        // 기기 추가 리프레시를 위한 ActivityResultLauncher 초기화
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val refreshRequired = result.data?.getBooleanExtra("REFRESH_REQUIRED", false) ?: true
                val macAddress = result.data?.getStringExtra("MAC_ADDRESS") // MAC 주소 추출
                Log.d("ActivityResult", "MAC Address: $macAddress")

                if (refreshRequired) {
                    Log.d("ActivityResult", "Refreshing data")
                    refreshData(macAddress.toString())
                }
            } else {
                Log.w("ActivityResult", "RESULT_CANCELED or no data returned")
            }
        }


        // 프레그먼트 관련 함수
        // 프레그먼트 포지션(몇 번인지 확인 하 수 있음)
        // 프레그먼트가 변화할 때 마다 호출 ex 이동, 생성
        // 프레그먼트 관련 함수
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                if (DataHolder.devicesCurrentData.size > position) {
                    val currentD = DataHolder.devicesCurrentData[position]
                    currentTemperature.text = currentD.temperature?.roundToInt().toString() + " ℃"
                    currentMoisture.text = currentD.humidity?.roundToInt().toString() + " %"

                    val deviceId = DataHolder.userDevices[currentPosition]
                    val historicalDataList = DataHolder.historicalDataMap[deviceId]

                    if (historicalDataList != null) {
                        // 어제 날짜 문자열 생성
                        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterdayDateString = dateFormat.format(calendar.time)

                        // 어제 데이터 찾기
                        val yesterdayData = historicalDataList.find { it.date == yesterdayDateString }

                        if (yesterdayData != null) {
                            // 어제 데이터로 UI 업데이트
                            yesterdayTemperature.text = yesterdayData.avgTemperature?.roundToInt().toString() + " ℃"
                            yesterdayMoisture.text = yesterdayData.avgHumidity?.roundToInt().toString() + " %"
                        } else {
                            // 어제 데이터가 없을 경우 기본값 설정
                            yesterdayTemperature.text = "00 ℃"
                            yesterdayMoisture.text = "00 %"
                        }
                    } else {
                        // 히스토리 데이터가 없을 경우 기본값 설정
                        yesterdayTemperature.text = "00 ℃"
                        yesterdayMoisture.text = "00 %"
                    }

                    // 차트 데이터 업데이트
                    setChartData(historicalDataList ?: emptyList())
                }else{
                    // 어제 데이터가 없을 경우 기본값 설정
                    yesterdayTemperature.text = "00 ℃"
                    yesterdayMoisture.text = "00 %"
                    currentTemperature.text = "00 ℃"
                    currentMoisture.text = "00 %"
                    setChartData(emptyList())
                }

                // 포지션은 0부터 시작해 +1
                // 식물 추가 창도 포지션을 차지하기 때문에 주의가 필요함
                updateIndicators(position)
                if (position == fragments.size - 1) {
                    addButton.visibility = View.VISIBLE
                    addText.visibility = View.VISIBLE
                    addImage.visibility = View.VISIBLE
                } else {
                    addButton.visibility = View.GONE
                    addText.visibility = View.GONE
                    addImage.visibility = View.GONE
                }
                // 페이지가 변경될 때마다 현재 페이지 인덱스를 SharedPreferences에 저장
                sharedPreferences.edit().putInt("lastPageIndex", position).apply()
                updateButtonInCurrentFragment()
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

    private fun removeDeviceFromFirestore(username: String, deviceId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(username)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentDevices = snapshot.get("devices") as? MutableList<String> ?: mutableListOf()
            val currentNames = snapshot.get("name") as? MutableList<String> ?: mutableListOf()

            // deviceId와 일치하는 이름도 제거 (동기화 필요 시)
            val index = currentDevices.indexOf(deviceId)
            if (index != -1) {
                currentDevices.removeAt(index)
                currentNames.removeAt(index)
            }

            transaction.set(userRef, mapOf("devices" to currentDevices, "name" to currentNames), com.google.firebase.firestore.SetOptions.merge())
        }.addOnSuccessListener {
            Log.d("Firestore", "Device removed successfully from $username")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error removing device: ${e.message}")
        }
    }

    private fun removeFragment(position: Int) {
        var positionn=position
        if(position==0){
            viewPager.setCurrentItem(viewPager.size, false)
        }
        var position=positionn
        // 1. Fragment 리스트에서 제거
        fragments.removeAt(position)
        viewPager.adapter?.notifyItemRemoved(position)

        // 2. 관련 데이터 제거
        val deviceId = DataHolder.userDevices.removeAt(position)
        DataHolder.userDevicesName.removeAt(position)
        DataHolder.devicesCurrentData.removeAt(position)
        DataHolder.historicalDataMap.remove(deviceId)

        // 3. Firestore에서 사용자 데이터 업데이트 (선택 사항)
        removeDeviceFromFirestore(kakaoemail, deviceId)

        // 4. 현재 포지션이 유효한지 확인하고, 필요시 포지션 조정
        val newPosition = if (position >= fragments.size) fragments.size - 1 else position
        viewPager.setCurrentItem(newPosition, false)

        // 5. 인디케이터 및 UI 업데이트
        updateIndicators(newPosition)
    }


    // 제스처 핸들링 함수
    private fun handleLongPress() {
        if (currentPosition < fragments.size) {
            val items = arrayOf("하트호야", "스투키", "선인장", "피쉬본", "괴마옥","기기제거")
            val fragment = fragments[currentPosition]
            if (fragment is Fragment_main_Hearthoya && fragment.isAdded) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("식물을 선택해주세요")

                builder.setItems(items) { _, which ->
                    when (which) {
                        0 -> fragment.changeToHearthoya()
                        1 -> fragment.changeToStucky()
                        2 -> fragment.changeToCactus()
                        3 -> fragment.changeToFishbone()
                        4 -> fragment.changeToHaunted_house()
                        5 -> removeFragment(currentPosition)
                    }
                }
                builder.create().show()
            } else if (fragment is Fragment_main_Haunted_house && fragment.isAdded) {
                // Fragment_main_Haunted_house에 대한 처리
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("식물을 선택해주세요")

                builder.setItems(items) { _, which ->
                    when (which) {
                        0 -> fragment.changeToHearthoya()
                        1 -> fragment.changeToStucky()
                        2 -> fragment.changeToCactus()
                        3 -> fragment.changeToFishbone()
                        4 -> fragment.changeToHaunted_house()
                        5 -> removeFragment(currentPosition)
                    }
                }
                builder.create().show()
            }else if (fragment is Fragment_main_Stucky && fragment.isAdded) {
                // Fragment_main_Haunted_house에 대한 처리
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("식물을 선택해주세요")

                builder.setItems(items) { _, which ->
                    when (which) {
                        0 -> fragment.changeToHearthoya()
                        1 -> fragment.changeToStucky()
                        2 -> fragment.changeToCactus()
                        3 -> fragment.changeToFishbone()
                        4 -> fragment.changeToHaunted_house()
                        5 -> removeFragment(currentPosition)
                    }
                }
                builder.create().show()
            }else if (fragment is Fragment_main_Haunted_house && fragment.isAdded) {
                // Fragment_main_Haunted_house에 대한 처리
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("식물을 선택해주세요")

                builder.setItems(items) { _, which ->
                    when (which) {
                        0 -> fragment.changeToHearthoya()
                        1 -> fragment.changeToStucky()
                        2 -> fragment.changeToCactus()
                        3 -> fragment.changeToFishbone()
                        4 -> fragment.changeToHaunted_house()
                        5 -> removeFragment(currentPosition)
                    }
                }
                builder.create().show()
            }else if (fragment is Fragment_main_Cactus && fragment.isAdded) {
                // Fragment_main_Haunted_house에 대한 처리
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("식물을 선택해주세요")

                builder.setItems(items) { _, which ->
                    when (which) {
                        0 -> fragment.changeToHearthoya()
                        1 -> fragment.changeToStucky()
                        2 -> fragment.changeToCactus()
                        3 -> fragment.changeToFishbone()
                        4 -> fragment.changeToHaunted_house()
                        5 -> removeFragment(currentPosition)
                    }
                }
                builder.create().show()
            }else if (fragment is Fragment_main_Fishbone && fragment.isAdded) {
                // Fragment_main_Haunted_house에 대한 처리
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("식물을 선택해주세요")

                builder.setItems(items) { _, which ->
                    when (which) {
                        0 -> fragment.changeToHearthoya()
                        1 -> fragment.changeToStucky()
                        2 -> fragment.changeToCactus()
                        3 -> fragment.changeToFishbone()
                        4 -> fragment.changeToHaunted_house()
                        5 -> removeFragment(currentPosition)
                    }
                }
                builder.create().show()
            } else {
                Log.w("Fragment_Main_page", "현재 프래그먼트가 Fragment_main_Hearthoya가 아니거나 아직 활성화되지 않았습니다.")
            }
        } else {
            Log.w("Fragment_Main_page", "currentPosition이 fragments 리스트의 범위를 벗어났습니다.")
        }
    }

    // ViewModel의 데이터를 관찰하여 UI 업데이트
    private fun observeViewModel() {
        sharedViewModel.nowDataMap.observe(viewLifecycleOwner) { nowDataMap ->
            // 현재 페이지의 deviceId를 가져옴
            if (currentPosition < DataHolder.userDevices.size) {
                val deviceId = DataHolder.userDevices[currentPosition]
                val nowData = nowDataMap[deviceId]
                if (nowData != null) {
                    // 현재 프래그먼트의 UI 업데이트
                    val currentFragment = fragments[currentPosition]
                    if (currentFragment is UpdatableFragment) {
                        currentFragment.updateUI(nowData)
                    }

                    // 추가로 UI 요소 업데이트
                    currentTemperature.text = "${nowData.temperature?.roundToInt()} ℃"
                    currentMoisture.text = "${nowData.humidity?.roundToInt()} %"
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel 관찰 시작
        observeViewModel()
    }

    // 유저 정보가 없으면 문서 생성, 있으면 유저 정보에 기기 맥주소 추가
    fun saveUserDataToFirestore(username: String, deviceId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(username)

        // devices 배열 필드 업데이트 (기존 데이터에 추가)
        userRef.update("devices", FieldValue.arrayUnion(deviceId))
            .addOnSuccessListener {
                // 저장 성공 처리
                Log.d("Firestore", "Device added successfully to $username")
            }
            .addOnFailureListener { exception ->
                // 문서가 없으면 새로 생성
                if (exception is FirebaseFirestoreException && exception.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    val newUserData = mapOf(
                        "devices" to emptyList<String>(), // devices 필드를 빈 배열로 설정
                        "name" to emptyList<String>() // devices 필드를 빈 배열로 설정
                    )
                    userRef.set(newUserData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "New user created and device added successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error creating user: ${e.message}")
                        }
                } else {
                    Log.e("Firestore", "Error updating user data: ${exception.message}")
                }
            }
    }


    // 파이어베이스에서 6일 이전의 데이터 가져오는 함수
    suspend fun fetchHistoricalData(deviceId: String): List<HistoricalData> {
        val historicalDataList = mutableListOf<HistoricalData>()

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.time = today

        // 현재 날짜부터 6일 전까지의 날짜 문자열을 생성합니다.
        val dateStrings = mutableListOf<String>()
        for (i in 0..6) {
            val dateString = dateFormat.format(calendar.time)
            dateStrings.add(dateString)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // 컬렉션 레퍼런스를 가져옵니다.
        val collectionRef = db.collection("testCollection")
            .document("data")
            .collection(deviceId)

        // 각 날짜에 대해 문서를 가져옵니다.
        for (dateString in dateStrings) {
            val docRef = collectionRef.document(dateString)
            try {
                val documentSnapshot = docRef.get().await()
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val avgHumidity = documentSnapshot.getDouble("avgHumidity")
                    val avgMoistureADC = documentSnapshot.getDouble("avgMoistureADC")
                    val avgTemperature = documentSnapshot.getDouble("avgTemperature")

                    val historicalData = HistoricalData(
                        date = dateString,
                        avgHumidity = avgHumidity,
                        avgMoistureADC = avgMoistureADC,
                        avgTemperature = avgTemperature
                    )
                    historicalDataList.add(historicalData)
                } else {
                    // 해당 날짜에 문서가 없는 경우 처리
                    Log.d(TAG, "No data for date: $dateString")
                }
            } catch (exception: Exception) {
                Log.d(TAG, "Failed to fetch data for date $dateString", exception)
            }
        }

        return historicalDataList
    }


    // 파이어베이스에서 유저 값 받아오는
    suspend fun fetchUserData(username: String):UserData? {
        val docRef = db.collection("users")
            .document(username)

        try {
            val documentSnapshot = docRef.get().await()
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val userData = documentSnapshot.toObject(UserData::class.java)
                if (userData != null) {
                    userDevices.clear()
                    userDevices.addAll(userData.devices ?: emptyList())
                    return userData
                } else {
                    Log.d(TAG, "User data is null")
                }
            } else {
                Log.d(TAG, "No such document")
            }
        } catch (exception: Exception) {
            Log.d(TAG, "get failed with ", exception)
        }
        // 실패한 경우 null 반환
        return null
    }

    // 파이어베이스에서 현재 값을 가져오는 함수
    suspend fun fetchNowData(deviceId: String): NowData? {
        val docRef = db.collection("testCollection")
            .document("data")
            .collection(deviceId)
            .document("nowdata")

        try {
            val documentSnapshot = docRef.get().await()
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val nowData = documentSnapshot.toObject(NowData::class.java)
                if (nowData != null) {
                    return nowData
                }
            } else {
                Log.d(TAG, "No such document for device: $deviceId")
            }
        } catch (exception: Exception) {
            Log.d(TAG, "get failed with ", exception)
        }
        // 실패한 경우 null 반환
        return null
    }

    // 파이어베이스 nowdata 문서에 대한 리스너 설정 함수
    fun setNowDataListener(deviceId: String, deviceIndex: Int) {
        val docRef = db.collection("testCollection")
            .document("data")
            .collection(deviceId)
            .document("nowdata")

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val nowData = snapshot.toObject(NowData::class.java)
                if (nowData != null) {
                    // DataHolder 업데이트
                    Log.d(deviceId,deviceId)
                    Log.d(deviceId,(deviceId == DataHolder.userDevices[deviceIndex]).toString()) // 버그

                    var idx = DataHolder.userDevices.indexOf(deviceId)
                    if (idx != -1) {
                        DataHolder.devicesCurrentData[idx] = nowData

                        // ViewModel 업데이트
                        sharedViewModel.updateNowData(deviceId, nowData)
                    }

                    // 현재 페이지가 해당 기기일 경우 UI 업데이트
                    if (currentPosition < DataHolder.userDevices.size && deviceId.equals(DataHolder.userDevices[currentPosition])) {
                        updateCurrentDeviceUI(nowData)
                    }
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    // 파이어베이스 오늘 날짜 히스토리컬 데이터에 대한 리스너 설정 함수
    fun setHistoricalDataListener(deviceId: String, deviceIndex: Int) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayDateString = dateFormat.format(Date())

        val docRef = db.collection("testCollection")
            .document("data")
            .collection(deviceId)
            .document(todayDateString)

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val avgHumidity = snapshot.getDouble("avgHumidity")
                val avgMoistureADC = snapshot.getDouble("avgMoistureADC")
                val avgTemperature = snapshot.getDouble("avgTemperature")

                val historicalData = HistoricalData(
                    date = todayDateString,
                    avgHumidity = avgHumidity,
                    avgMoistureADC = avgMoistureADC,
                    avgTemperature = avgTemperature
                )

                // DataHolder 업데이트
                val historicalDataList = DataHolder.historicalDataMap[deviceId]?.toMutableList() ?: mutableListOf()
                val index = historicalDataList.indexOfFirst { it.date == todayDateString }
                if (index != -1) {
                    historicalDataList[index] = historicalData
                } else {
                    historicalDataList.add(historicalData)
                }
                DataHolder.historicalDataMap[deviceId] = historicalDataList

                // 현재 페이지가 해당 기기일 경우 차트 업데이트
                if (viewPager.currentItem == DataHolder.userDevices.indexOf(deviceId)) {
                    setChartData(historicalDataList)
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    // UI 업데이트를 위한 함수
    fun updateCurrentDeviceUI(nowData: NowData) {
        currentTemperature.text = nowData.temperature?.roundToInt().toString() + " ℃"
        currentMoisture.text = nowData.humidity?.roundToInt().toString() + " %"
    }


    fun addNewPage(deviceType: String, deviceId:String, email:String) {
        if (fragments.lastOrNull() is Fragment_Blank2) {
            fragments.removeAt(fragments.size - 1)
            viewPager.adapter?.notifyItemRemoved(fragments.size - 1)
        }


        val fragment = when (deviceType) {
            "하트호야" -> Fragment_main_Hearthoya.newInstance(fragments.size + 1,deviceId, email)
            "스투키" -> Fragment_main_Stucky.newInstance(fragments.size + 1,deviceId, email)
            "선인장" -> Fragment_main_Cactus.newInstance(fragments.size + 1,deviceId, email)
            "피쉬본" -> Fragment_main_Fishbone.newInstance(fragments.size + 1,deviceId, email)
            "괴마옥" -> Fragment_main_Haunted_house.newInstance(fragments.size + 1,deviceId, email)
            else -> Fragment_main_Hearthoya.newInstance(fragments.size + 1, deviceId, email)
        }

        fragments.add(fragment)
        addNewPage2()
        updateButtonInCurrentFragment()
    }

    private fun showAddButtonDialog() {
        // Intent로 hardtest 액티비티로 이동
        val intent = Intent(requireContext(), hardtest::class.java)
        launcher.launch(intent)  // Change this line
    }
    private fun addLimitLine() {
        // 가로선을 추가할 위치와 라벨 설정
        val limitLine = LimitLine(50f)


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
        chart.legend.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisLineWidth = 2f

        xAxis.setCenterAxisLabels(false)

        chart.axisRight.isEnabled = false
        val xLabels = chart.xAxis

        xLabels.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val currentDate = getCurrentDate()
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.DAY_OF_MONTH, value.toInt() - 7)
                val formattedDate =
                    SimpleDateFormat("MM/dd", Locale.getDefault()).format(calendar.time)
                val dayOfWeek = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time)
                val combinedText = "$formattedDate<br>${dayOfWeek.uppercase(Locale.getDefault())}"
                return formattedDate
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
    private fun initializeButtonInCurrentFragment() {
        val currentIndex = viewPager.currentItem

        val currentFragment = fragments[currentIndex]

        val button_harthoya_grow = currentFragment.view?.findViewById<Button>(R.id.harthoya_grow)

        button_harthoya_grow?.setOnClickListener {
            val customDialogFragment = Fragment_CustomDialog_hearhoya()
            customDialogFragment.show(parentFragmentManager, "custom_dialog")
        }


        val button_stucky_grow = currentFragment.view?.findViewById<Button>(R.id.stucky_grow)

        button_stucky_grow?.setOnClickListener {
            Toast.makeText(requireContext(), "스투키 버튼이 클릭되었습니다.", Toast.LENGTH_SHORT).show()
        }

        val button_cactus_grow = currentFragment.view?.findViewById<Button>(R.id.cactus_grow)

        button_cactus_grow?.setOnClickListener {
            Toast.makeText(requireContext(), "선인장 버튼이 클릭되었습니다.", Toast.LENGTH_SHORT).show()
        }

        val button_fishbone_grow = currentFragment.view?.findViewById<Button>(R.id.fishbone_grow)

        button_fishbone_grow?.setOnClickListener {
            Toast.makeText(requireContext(), "피쉬본 버튼이 클릭되었습니다.", Toast.LENGTH_SHORT).show()
        }

        val button_haunted_house_grow = currentFragment.view?.findViewById<Button>(R.id.haunted_house_grow)

        button_haunted_house_grow?.setOnClickListener {
            Toast.makeText(requireContext(), "괴마옥 버튼이 클릭되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 필요한 시점에 이 메서드를 호출하세요. 예를 들어, 인디케이터를 업데이트한 후나 onPageSelected에서 호출할 수 있습니다.
    private fun updateButtonInCurrentFragment() {
        initializeButtonInCurrentFragment()
    }

    private fun setChartData(historicalDataList: List<HistoricalData>) {
        val entries = mutableListOf<Entry>()
        val xLabels = mutableListOf<String>()

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        // 데이터를 날짜순으로 정렬합니다.
        val sortedData = historicalDataList.sortedBy { it.date }

        for ((index, data) in sortedData.withIndex()) {
            val xValue = index.toFloat() + 1 // x값은 1부터 시작합니다.
            val yValue = data.avgMoistureADC?.let {
                ((4095-it.toFloat()) / 4095.0f) * 100
            }
                ?: 0f // avgHumidity를 사용합니다.

            entries.add(Entry(xValue, yValue))

            // x축 레이블을 위한 날짜 포맷팅
            val date = dateFormat.parse(data.date)
            val formattedDate = displayDateFormat.format(date)
            xLabels.add(formattedDate)
        }

        val dataSet = LineDataSet(entries, null)

        // 데이터셋 속성을 설정합니다.
        val colorString = "#54B22D"
        val color = Color.parseColor(colorString)
        dataSet.color = color
        dataSet.setDrawIcons(true)
        dataSet.lineWidth = 1f
        dataSet.setDrawValues(false)

        // 값에 따라 아이콘을 설정합니다.
        for (i in entries.indices) {
            val value = entries[i].y
            val iconResId = if (value >= 50) {
                R.drawable.test1
            } else {
                R.drawable.test
            }
            entries[i].icon = context?.getDrawable(iconResId)
        }

        val lineData = LineData(dataSet)
        chart.data = lineData

        // x축 레이블을 설정합니다.
        val xAxis = chart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt() - 1
                return if (index >= 0 && index < xLabels.size) xLabels[index] else ""
            }
        }

        chart.invalidate()
    }


    private fun setChartData() {
        val entries = mutableListOf<Entry>()

        // 1~7일 동안의 1~7의 값을 가지는 데이터를 entries에 추가
        entries.add(Entry(1.toFloat(), 0.toFloat()))
        entries.add(Entry(2.toFloat(), 0.toFloat()))
        entries.add(Entry(3.toFloat(), 0.toFloat()))
        entries.add(Entry(4.toFloat(), 0.toFloat()))
        entries.add(Entry(5.toFloat(), 0.toFloat()))
        entries.add(Entry(6.toFloat(), 0.toFloat()))
        entries.add(Entry(7.toFloat(), 0.toFloat()))

        val icons = mutableListOf<Int>()

        for (entry in entries) {
            val value = entry.y
            val iconResId = if (value >= 50) {
                R.drawable.test1
            } else {
                R.drawable.test
            }
            icons.add(iconResId)
        }

        for (i in entries.indices) {
            entries[i].icon = context?.getDrawable(icons[i])
        }

        val colorString = "#54B22D"
        val color = Color.parseColor(colorString)
        val dataSet = LineDataSet(entries, null)

        dataSet.color = color
        dataSet.setDrawIcons(true)
        dataSet.lineWidth = 1f

        val lineData = LineData(dataSet)

//         아래의 주석 코드를 사용하면 차트 위에 데이터 값이 나타납니다.
        dataSet.setDrawValues(false)

        chart.data = lineData
        chart.invalidate()
    }

    private fun getCurrentDate(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}