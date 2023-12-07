package com.example.smartpot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartpot.databinding.ActivityKakaoLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class Kakao_login_Activity : AppCompatActivity() {
    private var _binding: ActivityKakaoLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityKakaoLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** HashKey확인 */
        val keyHash = Utility.getKeyHash(this)
       // TextMsg(this, "HashKey: ${keyHash}")

        /** KakoSDK init */
        KakaoSdk.init(this, "91a7ed67032c8c01c8ab18e5486817fd")

        binding.btnStartKakaoLogin.setOnClickListener {
            kakaoLogin() //로그인
        }
        binding.btnRegister.setOnClickListener {
            // 여기에 버튼 클릭 이벤트에 수행할 작업을 추가
            // 예: 다른 화면으로 이동하는 등의 동작을 수행
            // 예: 예시로 다른 화면으로 이동하는 코드
            val intent = Intent(this@Kakao_login_Activity, Activity_Login_Jion_Membership::class.java)
            startActivity(intent)
        }
    }

    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                TextMsg(this, "카카오계정으로 로그인 실패")
                setLogin(false)
            } else if (token != null) {
                //TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                UserApiClient.instance.me { user, error ->
                    if (user != null) {
                        Name(user.kakaoAccount?.profile?.nickname ?: "Unknown")
                    }
                    setLogin(true)
                    val intent = Intent(this@Kakao_login_Activity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
//                    TextMsg(this, "카카오톡으로 로그인 실패 : ${error}")
                    TextMsg(this, "카카오톡으로 로그인 실패")

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    UserApiClient.instance.me { user, error ->
                        if (user != null) {
                            Name(user.kakaoAccount?.profile?.nickname ?: "Unknown")
                        }
                        setLogin(true)
                        val intent = Intent(this@Kakao_login_Activity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun TextMsg(act: Activity, msg : String){
        Toast.makeText(act, msg, Toast.LENGTH_LONG).show()
    }

    private fun setLogin(bool: Boolean){
        binding.btnStartKakaoLogin.visibility = if(bool) View.GONE else View.VISIBLE
    }
    private fun Name(msg: String){
        Toast.makeText(this, msg+"님 환영합니다 ⊂(･ω･*⊂)", Toast.LENGTH_SHORT).show()
    }
}