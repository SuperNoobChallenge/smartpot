package com.example.smartpot

import android.app.AlertDialog
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

        val btnLogout = view.findViewById<Button>(R.id.btnKakaoLogout)
        val btnWithdraw = view.findViewById<Button>(R.id.btnKakaoWithdraw)

        btnLogout.setOnClickListener {
            UserApiClient.instance.logout { error ->
                if (error != null) {
                } else {
                    val intent = Intent(activity, Kakao_login_Activity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }

        btnWithdraw.setOnClickListener {
            showWithdrawalConfirmationDialog()
        }
        loadUserInfo()
    }
    private fun showWithdrawalConfirmationDialog() {
        // Implement a dialog to confirm user withdrawal
        // You can use AlertDialog or any other custom dialog implementation

        // Example using AlertDialog:
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("탈퇴 확인")
            .setMessage("서비스에서 탈퇴하시겠습니까?")
            .setPositiveButton("네") { _, _ ->
                // 사용자가 탈퇴를 확인함
                withdrawUser()
            }
            .setNegativeButton("아니오") { _, _ ->
                // 사용자가 탈퇴를 취소함
            }
            .show()
    }
    private fun withdrawUser() {
        // Implement user withdrawal logic using Kakao Login API
        UserApiClient.instance.unlink { unlinkError ->
            if (unlinkError != null) {
                // Handle withdrawal error
            } else {
                // User successfully withdrawn
                val intent = Intent(activity, Kakao_login_Activity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
    }
    private fun loadUserInfo() {
        val ivProfileImage = view?.findViewById<ImageView>(R.id.ivProfileImage)
        // Get user information
        UserApiClient.instance.me { user: User?, error ->
            if (error != null) {
            } else if (user != null) {
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
        val tvName = view?.findViewById<TextView>(R.id.tvKakaoName)
        val tvEmail = view?.findViewById<TextView>(R.id.tvKakaoEmail)

        tvName?.text = name ?: "KakaoTalk User"
        tvEmail?.text = email ?: "kakao@example.com"
    }
}
