package com.example.smartpot

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var BottomNV: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        BottomNV = findViewById(R.id.nav_view)
        BottomNV.setOnNavigationItemSelectedListener { menuItem ->
            bottomNavigate(menuItem.itemId)
            true
        }
        BottomNV.selectedItemId = R.id.navigation_1
    }

    private fun bottomNavigate(id: Int) {
        val tag = id.toString()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val currentFragment = fragmentManager.primaryNavigationFragment
        currentFragment?.let { fragmentTransaction.hide(it) }

        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = when (id) {
                R.id.navigation_1 -> Fragment_Main_page()
                R.id.navigation_2 -> Fragment_Plant_Diary_page()
                R.id.navigation_3 -> Fragment_Store_page()
                else -> Fragment_My_page()
            }
            fragmentTransaction.add(R.id.content_layout, fragment, tag)
        } else {
            fragmentTransaction.show(fragment)
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragment)
        fragmentTransaction.setReorderingAllowed(true)
        fragmentTransaction.commitNow()
    }
}