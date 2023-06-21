package kr.puze.easyfarm

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import kr.puze.easyfarm.Bluetooth.Beacon
import kr.puze.easyfarm.Bluetooth.Paired
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("LongLogTag", "MissingPermission")
class PlantActivity : AppCompatActivity() {

    lateinit var mScanCallback: ScanCallback
    var REQUEST_ENABLE_BT = 100
    var REQUEST_CODE_DISCOVERABLE_BT = 101
    var REQUEST_PERMISSION_BT_SCAN = 201
    var REQUEST_PERMISSION_BT_CONNECT = 202
    lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var mBluetoothLeScanner: BluetoothLeScanner
    lateinit var mBluetoothLeAdvertiser: BluetoothLeAdvertiser
    lateinit var beacon: Vector<Beacon>
    lateinit var mDevices: Set<BluetoothDevice>
    lateinit var mSocket: BluetoothSocket
    lateinit var mOutputStream: OutputStream
    lateinit var mInputStream: InputStream
    lateinit var mWorkerThread: Thread
    var simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREAN)
    var isBluetoothConnected = false
    var result = ""
    var showBluetoothDialog by mutableStateOf(false)
    var hum = mutableStateOf("습도")
    var temp = mutableStateOf("온도")
    var ledState = mutableStateOf("State")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //블루투스 설정 코드 실행
        settingBluetooth()

        // Android Compose 를 이용한 View 구성
        setContent {
            ContentWidget()
        }
    }

    @Composable
    fun ContentWidget() {
        //블루투스 다이얼로그를 보여주는 코드
        if (showBluetoothDialog) {
            BluetoothDialog(setShowDialog = {
                showBluetoothDialog = it
            })
        }

        var expanded by remember { mutableStateOf(false) }
        val items = listOf(
            "5",
            "10",
            "15",
            "20",
            "25",
            "30",
            "35",
            "40",
            "45",
            "50",
            "55",
            "60",
            "65",
            "70",
            "75",
            "80",
            "85",
            "90",
            "95",
            "100"
        )
        var selectedIndex by remember { mutableStateOf(11) }
        var ledStatus by remember { mutableStateOf(true) }
        // 화면을 최대로, 배경색상을 #FFFFFF, #3399FF 의 그라데이션으로 설정
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.parseColor("#B2EBF4")),
                            Color(android.graphics.Color.parseColor("#FFFFFF"))
                        )
                    )
                )
                .fillMaxSize()
        ) {
            // Column 안의 View 를 Column 의 중심으로 설정
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ActionBar 구성
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(android.graphics.Color.parseColor("#B2EBF4")))
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Easy Farm",
                        color = Color(android.graphics.Color.parseColor("#FFFFFF")),
                        fontSize = 36.sp
                    )
                }
                // ActionBar 를 제외한 나머지 부분의 위쪽 1/3 영역을 차지하도록 설정
                Row(
                    modifier = Modifier
                        .weight(1f, true)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f, true)
                    )
                    // 습도가 표기된 원형 뷰
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(2f, true)
                            .clip(CircleShape)
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .border(
                                width = 5.dp,
                                color = Color(android.graphics.Color.parseColor("#ffffff")),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = hum.value + "%",
                                    color = Color(android.graphics.Color.parseColor("#56767C")),
                            fontSize = 28.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f, true)
                    )
                }
                // ActionBar 를 제외한 나머지 부분의 중간쪽 1/3 영역을 차지하도록 설정
                Row(
                    modifier = Modifier
                        .weight(1f, true)
                ) {
                    // 온도가 표기된 원형 뷰와 LED 여부가 표기된 원형 뷰를 1:1 로 구성
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f, true)
                            .clip(CircleShape)
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .border(
                                width = 5.dp,
                                color = Color(android.graphics.Color.parseColor("#ffffff")),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = temp.value + "°C",
                            color = Color(android.graphics.Color.parseColor("#56767C")),
                            fontSize = 28.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f, true)
                            .clip(CircleShape)
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .border(
                                width = 5.dp,
                                color = Color(android.graphics.Color.parseColor("#ffffff")),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = ledState.value,
                            color = Color(android.graphics.Color.parseColor("#56767C")),
                            fontSize = 28.sp
                        )
                    }
                }
                // ActionBar 를 제외한 나머지 부분의 아래쪽 1/3 영역을 차지하도록 설정
                Row(
                    modifier = Modifier
                        .weight(1f, true)
                        .background(androidx.compose.ui.graphics.Color.White)

                    ) {
                    // 습도를 변경 영역
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f, true)
                            .fillMaxSize()
                            .aspectRatio(1f)
                    ) {
                        Column(
                            Modifier
                                .align(Alignment.Center)
                                .clickable { expanded = true },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "습도",
                                color = Color(android.graphics.Color.parseColor("#000000")),
                                fontSize = 28.sp
                            )
                            Text(
                                text = "${items[selectedIndex]}%",
                                color = Color(android.graphics.Color.parseColor("#000000")),
                                fontSize = 56.sp
                            )
                            // 습도를 선택할 수 있는 DropdownMenu 구성
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color.White
                                    )
                            ) {
                                items.forEachIndexed { index, s ->
                                    DropdownMenuItem(onClick = {
                                        selectedIndex = index
                                        expanded = false
                                        sendDataToBluetooth(items[selectedIndex])
                                    }) {
                                        Text(text = "$s%")
                                    }
                                }
                            }
                        }
                    }
                    // LED 여부 변경의 영역
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f, true)
                            .fillMaxSize()
                            .aspectRatio(1f)
                    ) {
                        Column(
                            // 해당 View 를 눌렀을 때에 led 값의 상태를 변경하는 로직
                            Modifier
                                .align(Alignment.Center)
                                .clickable {
                                    ledStatus = !ledStatus
                                    sendDataToBluetooth(ledStatus.toString())
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "LED",
                                color = Color(android.graphics.Color.parseColor("#000000")),
                                fontSize = 28.sp
                            )
                            Text(
                                text = if (ledStatus) {
                                    "ON"
                                } else {
                                    "OFF"
                                },
                                color = Color(android.graphics.Color.parseColor("#000000")),
                                fontSize = 56.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BluetoothDialog(setShowDialog: (Boolean) -> Unit) {
        var mBluetoothAdapter: BluetoothAdapter? = null
        var pairedDevices: Set<BluetoothDevice>? = null
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            pairedDevices = mBluetoothAdapter.bondedDevices

//        startDiscovery()
//        mBluetoothLeScanner.startScan(getScanCallback("04:32:F4"))
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                this@PlantActivity,
                "블루투스 기기 탐색 실패! (${e.toString()}/${e.message})",
                Toast.LENGTH_SHORT
            ).show()
        }

        Dialog(onDismissRequest = {
            setShowDialog(false)
        }) {
            Surface(
                modifier = Modifier
                    .wrapContentSize(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        "블루투스 기기 선택"
                    )
                    pairedDevices?.forEach { device ->
                        BluetoothDialog(device = device)
                    }
                }
            }
        }
    }

    @Composable
    fun BluetoothDialog(device: BluetoothDevice) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                connectToSelectedDevice(device)
            }
        ) {
            Text(text = "${device.name ?: "UNKNOWN"}\n${device.address ?: "UNKNOWN"}\n${device.uuids[0] ?: "UNKNOWN"}")
        }
    }

    private fun settingBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@PlantActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    REQUEST_PERMISSION_BT_SCAN
                )
                return
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@PlantActivity,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_PERMISSION_BT_CONNECT
                    )
                    return
                }
            }
        }
        if (!mBluetoothAdapter.isEnabled) {
            startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        } else {
            mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
            mBluetoothLeAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
            beacon = Vector()
            showBluetoothDialog = true
        }
    }

    fun getScanCallback(address: String): ScanCallback {
        mScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                try {
                    runOnUiThread {
                        beacon.add(
                            Beacon(
                                result.device.address,
                                result.rssi,
                                simpleDateFormat.format(Date()),
                                result.device
                            )
                        )
                        if (ActivityCompat.checkSelfPermission(
                                this@PlantActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            //권한이 없을 때에 권한을 받는 코드
                            ActivityCompat.requestPermissions(
                                this@PlantActivity,
                                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                102
                            )
                            return@runOnUiThread
                        }
                        if (beacon.lastElement().address.contains(address)) beacon.lastElement().device.createBond()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onBatchScanResults(results: List<ScanResult?>) {
                super.onBatchScanResults(results)
                Log.d("onBatchScanResults", results.size.toString() + "")
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d("onScanFailed()", errorCode.toString() + "")
            }
        }

        return mScanCallback
    }

    private fun connectToSelectedDevice(bluetoothDevice: BluetoothDevice) {
        var mRemoteDevice = bluetoothDevice
        //블루투스의 기본 UUID 로 사용하는 블루투스 기기의 UUID 를 임의로 변경했다면 변경한 값을 입력해야 함
        var uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        if (mRemoteDevice != null) {
            try {
                Log.d("LOGTAG", "connectToSelectedDevice: ${bluetoothDevice.name}")
                // 소켓 생성
                mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid)
                // RFCOMM 채널을 통한 연결
                mSocket.connect()
                // 데이터 송수신을 위한 스트림 열기
                mOutputStream = mSocket.outputStream
                mInputStream = mSocket.inputStream

                beginListenForData()
                isBluetoothConnected = true
                Toast.makeText(this@PlantActivity, "블루투스 연결 성공!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 블루투스 연결 중 오류 발생
                Toast.makeText(this@PlantActivity, "블루투스 연결 실패!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDeviceFromBondedList(name: String): BluetoothDevice? {
        var selectedDevice: BluetoothDevice? = null

        for (device: BluetoothDevice in mDevices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //권한이 없을 때에 권한을 받는 코드
                ActivityCompat.requestPermissions(
                    this@PlantActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    101
                )
                return null
            }
            if (name == device.name) {
                selectedDevice = device
                break
            }
        }

        return selectedDevice
    }

    private fun sendDataToBluetooth(data: String) {
        if (isBluetoothConnected) {
            // 전송할 데이터 입력
            val msgBuffer = (data + "\n").toByteArray()
            try {
                //OutputStream 의 write 를 통해 블루투스에게 데이터를 전송
                mOutputStream.write(msgBuffer)
            } catch (e: IOException) {
                Toast.makeText(this@PlantActivity, "Connection Failure", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this@PlantActivity, "블루투스가 연결되지 않았습니다. 재연결합니다.", Toast.LENGTH_LONG)
                .show()
            settingBluetooth()
        }
    }

    // 블루투스로부터 데이터를 수신받는 코드
    // Thread 를 사용하여 실시간으로 수신할 수 있음
    private fun beginListenForData() {
        val handler = Handler()

        mWorkerThread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    var bytesAvailable = mInputStream.available()
                    if (bytesAvailable > 0) {
                        var packetBytes = ByteArray(bytesAvailable)
                        mInputStream.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val temperature = packetBytes[0]
                            val humidity = packetBytes[1]
                            var led = packetBytes[2]
                            Log.d("Temp: ", "$temperature")
                            Log.d("Humi: ", "$humidity")
                            Log.d("ledState: ", "${led.toInt()}")
                            temp.value = temperature.toString()
                            hum.value = humidity.toString()
                            if(led.toInt() == 0){
                                ledState.value = "OFF"
                            }
                            else if(led.toInt() == 1){
                                ledState.value = "ON"
                            }
                        }
                    }
                } catch (e: IOException) {

                }
            }
        })
        mWorkerThread.start()

        isBluetoothConnected = true
    }

    // 해당 액티비티를 나가게 되면 블루투스 스캔을 정지
    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //권한이 없다면 할당 해제할 리소스들이 없기 때문에 코드 종료
            return
        }
        mBluetoothLeScanner.stopScan(mScanCallback)
        try {
            mWorkerThread.interrupt();   // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        } catch (e: Exception) {

        }
    }

    // 블루투스가 사용 가능하다면 블루투스 설정 코드를 실행
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) settingBluetooth()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("LOGTAG/onRequestPermissionsResult", "${requestCode}/${grantResults[0]}")
        when (requestCode) {
            REQUEST_PERMISSION_BT_CONNECT -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    settingBluetooth()
                }
            }
            REQUEST_PERMISSION_BT_SCAN -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    settingBluetooth()
                }
            }
        }
    }
}