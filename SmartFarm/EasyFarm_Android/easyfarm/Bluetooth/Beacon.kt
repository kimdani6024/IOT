package kr.puze.easyfarm.Bluetooth

import android.bluetooth.BluetoothDevice

class Beacon(val address: String, val rssi: Int, val now: String, val device: BluetoothDevice)