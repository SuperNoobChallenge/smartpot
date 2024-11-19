package com.example.smartpot

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class hardtest : AppCompatActivity() {

    private val TAG = "BluetoothChat"
    private val DEVICE_NAME = "ESP32_BT" // ESP32에서 설정한 이름과 동일해야 합니다.
    private val UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private val REQUEST_PERMISSIONS = 1

    private lateinit var btn_bltPairing: Button
    private lateinit var btn_wifiSearch: Button
    private lateinit var ListView_wifiNetworks: ListView
    private val wifiNetworks = ArrayList<String>()
    private lateinit var wifiAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.hardtest)

        // UI 컴포넌트 초기화
        btn_bltPairing = findViewById(R.id.btn_bltPairing)
        btn_wifiSearch = findViewById(R.id.btn_wifiSearch)
        ListView_wifiNetworks = findViewById(R.id.ListView_wifiNetworks)

        // WiFi 리스트 어댑터 설정
        wifiAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, wifiNetworks)
        ListView_wifiNetworks.adapter = wifiAdapter

        // WiFi 리스트 아이템 클릭 리스너 설정
        ListView_wifiNetworks.setOnItemClickListener { parent, view, position, id ->
            val selectedSSID = wifiNetworks[position]
            showPasswordDialog(selectedSSID)
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // 권한 확인
        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasBluetoothConnectPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocationPermission || !hasCoarseLocationPermission || !hasBluetoothConnectPermission) {
            // 권한 요청
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT
                ), REQUEST_PERMISSIONS
            )
        }

        // "페어링" 버튼 클릭 리스너
        btn_bltPairing.setOnClickListener {
            connectToDevice()
        }

        // "WiFi 검색" 버튼 클릭 리스너
        btn_wifiSearch.setOnClickListener {
            sendMessage("START_WIFI_SCAN\n")
        }
    }

    private fun showPasswordDialog(ssid: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("비밀번호 입력")
        builder.setMessage("SSID: $ssid")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("완료") { dialog, which ->
            val password = input.text.toString()
            sendWiFiCredentials(ssid, password)
        }

        builder.setNegativeButton("취소") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendWiFiCredentials(ssid: String, password: String) {
        val message = "WIFI_CREDENTIALS:$ssid,$password\n"
        sendMessage(message)
    }

    private fun connectToDevice() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSIONS
            )
            return
        }

        val device = findPairedDeviceByName(DEVICE_NAME)

        if (device != null) {
            Thread {
                try {
                    bluetoothSocket =
                        device.createRfcommSocketToServiceRecord(UUID_INSECURE)
                    bluetoothSocket?.connect() // 연결 시도

                    inputStream = bluetoothSocket?.inputStream
                    outputStream = bluetoothSocket?.outputStream

                    runOnUiThread {
                        Log.d(TAG, "블루투스 연결 성공")
                        Toast.makeText(this, "블루투스 연결 성공", Toast.LENGTH_SHORT).show()
                    }

                    // 연결 성공 후 메시지 전송
                    sendMessage("안녕하세요, ESP32!\n")

                    // 데이터 수신 대기
                    listenForData()

                } catch (e: IOException) {
                    Log.e(TAG, "블루투스 연결 실패", e)
                    runOnUiThread {
                        Toast.makeText(this, "블루투스 연결 실패: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "권한 오류 발생", e)
                    runOnUiThread {
                        Toast.makeText(this, "권한 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        } else {
            Log.e(TAG, "기기를 찾을 수 없습니다.")
            runOnUiThread {
                Toast.makeText(this, "기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findPairedDeviceByName(name: String): BluetoothDevice? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSIONS
            )
            return null
        }

        val pairedDevices: Set<BluetoothDevice>?
        try {
            pairedDevices = bluetoothAdapter.bondedDevices
        } catch (e: SecurityException) {
            Log.e(TAG, "권한 오류 발생", e)
            return null
        }

        for (device in pairedDevices) {
            if (device.name == name) {
                return device
            }
        }
        return null
    }

    private fun listenForData() {
        val buffer = ByteArray(1024)
        var bytes: Int

        var isScanningWifi = false

        while (true) {
            try {
                bytes = inputStream?.read(buffer) ?: break
                val incomingMessage = String(buffer, 0, bytes)
                Log.d(TAG, "받은 메시지: $incomingMessage")
                runOnUiThread {
                    val lines = incomingMessage.split("\n")
                    for (line in lines) {
                        val trimmedLine = line.trim()
                        when {
                            trimmedLine == "WIFI_SCAN_START" -> {
                                isScanningWifi = true
                                wifiNetworks.clear()
                            }
                            trimmedLine == "WIFI_SCAN_END" -> {
                                isScanningWifi = false
                                // 리스트뷰 업데이트
                                wifiAdapter.notifyDataSetChanged()
                            }
                            isScanningWifi -> {
                                // WiFi 네트워크 추가
                                wifiNetworks.add(trimmedLine)
                            }
                            trimmedLine == "WIFI_CONNECT_SUCCESS" -> {
                                Toast.makeText(this, "Wi-Fi 연결 성공!", Toast.LENGTH_SHORT).show()
                            }
                            trimmedLine == "WIFI_CONNECT_FAILED" -> {
                                Toast.makeText(this, "Wi-Fi 연결 실패!", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // 기타 메시지 처리
                                Toast.makeText(this, "ESP32로부터: $trimmedLine", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "데이터 수신 오류", e)
                break
            }
        }
    }

    // 데이터 전송 함수
    private fun sendMessage(message: String) {
        try {
            outputStream?.write(message.toByteArray())
            outputStream?.write('\n'.toInt()) // 개행 문자 추가
            Log.d(TAG, "메시지 전송: $message")
        } catch (e: IOException) {
            Log.e(TAG, "메시지 전송 오류", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            var allPermissionsGranted = true
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    }
                }
            } else {
                allPermissionsGranted = false
            }

            if (!allPermissionsGranted) {
                Toast.makeText(
                    this,
                    "블루투스 기능을 사용하려면 필요한 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
