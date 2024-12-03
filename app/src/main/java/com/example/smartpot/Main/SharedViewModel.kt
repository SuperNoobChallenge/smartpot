// 파일: com/example/smartpot/Main/SharedViewModel.kt
package com.example.smartpot.Main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartpot.Main.Fragment_Main_page.NowData

class SharedViewModel : ViewModel() {
    // deviceId별 NowData를 저장하는 MutableLiveData
    private val _nowDataMap = MutableLiveData<Map<String, NowData>>()
    val nowDataMap: LiveData<Map<String, NowData>> get() = _nowDataMap

    // 초기화
    init {
        _nowDataMap.value = emptyMap()
    }

    // 특정 deviceId의 NowData 업데이트
    fun updateNowData(deviceId: String, nowData: NowData) {
        val currentMap = _nowDataMap.value?.toMutableMap() ?: mutableMapOf()
        currentMap[deviceId] = nowData
        _nowDataMap.value = currentMap
    }
}
