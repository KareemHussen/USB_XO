package com.example.usbxo

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private var mUsbManager: UsbManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mUsbManager = getSystemService(USB_SERVICE) as UsbManager
    }

    override fun onResume() {
        super.onResume()

        val deviceList = mUsbManager!!.deviceList

        if (deviceList == null || deviceList.size == 0) {


            Handler().postDelayed(Runnable {

                Intent(this, GameActivity::class.java).apply {
                    putExtra("type", "Client")
                    addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(this)
                }
                finish()

            },4100 )


            return

        }

        for (device in deviceList.values) {
            initAccessory(device!!)
        }

        if (searchForUsbAccessory(deviceList)) {
            return
        }
    }


    private fun searchForUsbAccessory(deviceList: HashMap<String, UsbDevice>): Boolean {
        for (device in deviceList.values) {
            if (isUsbAccessory(device)) {
                Handler().postDelayed(Runnable {

                    Intent(this, GameActivity::class.java).apply {

                        putExtra("device", device)
                        putExtra("type", "Host")
                        addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(this)
                    }
                    finish()

                },2100 )



                return true
            }
        }
        return false
    }

    private fun isUsbAccessory(device: UsbDevice): Boolean {
        return device.productId == 0x2d00 || device.productId == 0x2d01
    }

    private fun initAccessory(device: UsbDevice): Boolean {
        val connection = mUsbManager!!.openDevice(device) ?: return false
        initStringControlTransfer(connection, 0, "quandoo") // MANUFACTURER
        initStringControlTransfer(connection, 1, "Android2AndroidAccessory") // MODEL
        initStringControlTransfer(
            connection,
            2,
            "showcasing android2android USB communication"
        ) // DESCRIPTION
        initStringControlTransfer(connection, 3, "0.1") // VERSION
        initStringControlTransfer(connection, 4, "http://quandoo.de") // URI
        initStringControlTransfer(connection, 5, "42") // SERIAL
        connection.controlTransfer(0x40, 53, 0, 0, byteArrayOf(), 0, Constants.USB_TIMEOUT_IN_MS)
        connection.close()
        return true
    }

    private fun initStringControlTransfer(
        deviceConnection: UsbDeviceConnection,
        index: Int,
        string: String
    ) {
        deviceConnection.controlTransfer(
            0x40,
            52,
            0,
            index,
            string.toByteArray(),
            string.length,
            Constants.USB_TIMEOUT_IN_MS
        )
    }
}