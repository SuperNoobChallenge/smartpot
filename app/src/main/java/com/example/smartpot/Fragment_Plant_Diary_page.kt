package com.example.smartpot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class Fragment_Plant_Diary_page : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.page_plant_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버튼을 찾습니다. 예를 들어, 버튼의 ID가 buttonNavigate 이라고 가정합니다.
        val buttonNavigate = view.findViewById<Button>(R.id.plant_diary_plus_btn)

        // 버튼에 클릭 리스너를 설정합니다.
        buttonNavigate.setOnClickListener {
            // 명시적 인텐트를 사용하여 다른 화면으로 이동합니다.
            val intent = Intent(activity, Plant_Diary_Write_page::class.java)
            startActivity(intent)
        }
    }
}
