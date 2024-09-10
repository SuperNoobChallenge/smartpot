package com.example.smartpot.Mypage

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.smartpot.Fragment_check
import com.example.smartpot.R
import com.example.smartpot.login.Kakao_login_Activity
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
//        val btnWithdraw = view.findViewById<Button>(R.id.btnKakaoWithdraw)

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
        val btnOrderHistory = view.findViewById<Button>(R.id.btnOrderHistory)
        val btnCancelReturnHistory = view.findViewById<Button>(R.id.btnCancelReturnHistory)
        val btnDeliveryManagement = view.findViewById<Button>(R.id.btnDeliveryManagement)
        val btnPoint = view.findViewById<Button>(R.id.btnPoint)
        val btnFavoriteProducts = view.findViewById<Button>(R.id.btnFavoriteProducts)
        val btnProductReviews = view.findViewById<Button>(R.id.btnProductReviews)
        val btnMyInquiries = view.findViewById<Button>(R.id.btnMyInquiries)

        btnOrderHistory.setOnClickListener {
            // Handle click for Order History button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }

        btnCancelReturnHistory.setOnClickListener {
            // Handle click for Cancel/Return History button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }

        btnDeliveryManagement.setOnClickListener {
            // Handle click for Delivery Management button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }

        btnPoint.setOnClickListener {
            // Handle click for Point button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }

        btnFavoriteProducts.setOnClickListener {
            // Handle click for Favorite Products button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }

        btnProductReviews.setOnClickListener {
            // Handle click for Product Reviews button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }

        btnMyInquiries.setOnClickListener {
            // Handle click for My Inquiries button
            val intent = Intent(activity, Fragment_check::class.java)
            startActivity(intent)
        }
//        btnWithdraw.setOnClickListener {
//            showWithdrawalConfirmationDialog()
//        }
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
        // Get user information
        UserApiClient.instance.me { user: User?, error ->
            if (error != null) {
            } else if (user != null) {
                updateUI(user.kakaoAccount?.profile?.nickname, user.kakaoAccount?.email)

                // Load and display the profile image
                user.kakaoAccount?.profile?.thumbnailImageUrl?.let { imageUrl ->

                        Glide.with(this@Fragment_My_page)
                            .load(imageUrl)
                            .placeholder(R.drawable.background_my_page_circle)
                            .error(R.drawable.background_my_page_circle)
                            .fitCenter()


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
