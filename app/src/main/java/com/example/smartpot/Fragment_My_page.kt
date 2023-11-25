package com.example.smartpot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.smartpot.Kakao_login_Activity
import com.example.smartpot.R
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User

class Fragment_My_page : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.page_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views

        // Find views
        val btnLogout = view.findViewById<Button>(R.id.btnKakaoLogout)

        // Set click listener for logout button
        btnLogout.setOnClickListener {
            // Perform Kakao logout
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    // Handle the error
                } else {
                    // On successful logout, navigate to Kakao_login_Activity
                    val intent = Intent(activity, Kakao_login_Activity::class.java)
                    startActivity(intent)
                    activity?.finish() // Optional: Finish the current activity
                }
            }
        }

        // Load user information
        loadUserInfo()
    }
    private fun loadUserInfo() {
        val ivProfileImage = view?.findViewById<ImageView>(R.id.ivProfileImage)
        // Get user information
        UserApiClient.instance.me { user: User?, error ->
            if (error != null) {
                // Handle the error
            } else if (user != null) {
                // Update UI with user information
                updateUI(user.kakaoAccount?.profile?.nickname, user.kakaoAccount?.email)

                // Load and display the profile image
                user.kakaoAccount?.profile?.thumbnailImageUrl?.let { imageUrl ->
                    if (ivProfileImage != null) {
                        Glide.with(this@Fragment_My_page)
                            .load(imageUrl)
                            .placeholder(R.drawable.background_my_page_circle)
                            .error(R.drawable.background_my_page_circle)
                            .fitCenter()
                            .into(ivProfileImage)
                    }
                }
            }
        }
    }

    private fun updateUI(name: String?, email: String?) {
        // Update UI with user information
        val tvName = view?.findViewById<TextView>(R.id.tvKakaoName)
        val tvEmail = view?.findViewById<TextView>(R.id.tvKakaoEmail)

        tvName?.text = name ?: "KakaoTalk User"
        tvEmail?.text = email ?: "kakao@example.com"
    }
}
