package com.example.usbxo

import android.hardware.usb.*
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


class GameActivity : AppCompatActivity() {

    lateinit var accessoryCommunicator : AccessoryCommunicator

    // Thread Safe
    val keepThreadAlive = AtomicBoolean(true)
    var sendBuffer: ArrayList<String> = ArrayList()
    val viewModel by viewModels<GameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val type = intent.getStringExtra("type")


        viewModel.myPlayAgain.observe(this, Observer {
            if (it){
                if (viewModel.hisPlayAgain.value!!){
                    resetGame()
                } else {
                    loadingAnim.visibility = View.VISIBLE
                    XO_Layout.alpha = 0.5f
                    disableGame()
                }
            }

        })

        viewModel.hisPlayAgain.observe(this, Observer {
            if (it){
                if (viewModel.myPlayAgain.value!!){
                    resetGame()
                }
            }


        })

        viewModel.myScore.observe(this, Observer {
            myScore.text = it.toString()
        })

        viewModel.hisScore.observe(this, Observer {
            hisScore.text = it.toString()
        })

        viewModel.end.observe(this, Observer {


            if (viewModel.end.value!!){

                XO_Layout.alpha = 0.5f

                disableGame()

                if (viewModel.draw.value!!){

                    lottieDraw.visibility = View.VISIBLE
                    lottieDraw.playAnimation()

                    Handler().postDelayed({

                        XO_Layout.alpha = 1f
                        lottieDraw.visibility = View.GONE
                        lottieDraw.pauseAnimation()

                        val dialog = MaterialDialog(this).show {
                            positiveButton(R.string.agree) { dialog ->

                                if (type == "Host"){
                                    sendBuffer.add("playagain")
                                }else {
                                    accessoryCommunicator.send("playagain".toByteArray())
                                }

                                viewModel.myPlayAgain.value = true

                            }

                            negativeButton(R.string.disagree) { dialog ->
                                finish()
                            }

                            title(R.string.dialogTitle)

                            message(text = "Play again or you are afraid")
                        }

                        dialog.setCancelable(false)
                        dialog.setCanceledOnTouchOutside(false)
                    }, 2000 )

                } else if ( (viewModel.is_X_Won.value!! && type == "Host" ) || ( !viewModel.is_X_Won.value!! && type != "Host") ){

                    lottieWin.visibility = View.VISIBLE
                    lottieWin.playAnimation()

                    viewModel.myScore.value = viewModel.myScore.value!! + 1

                    Handler().postDelayed({

                        XO_Layout.alpha = 1f
                        lottieWin.visibility = View.GONE
                        lottieWin.pauseAnimation()

                        val dialog = MaterialDialog(this).show {
                            positiveButton(R.string.agree) { dialog ->

                                if (type == "Host"){
                                    sendBuffer.add("playagain")
                                }else {
                                    accessoryCommunicator.send("playagain".toByteArray())
                                }

                                viewModel.myPlayAgain.value = true

                            }

                            negativeButton(R.string.disagree) { dialog ->
                                finish()
                            }

                            title(R.string.dialogTitle)

                            message(text = "Play again or you are afraid")
                        }

                        dialog.setCancelable(false)
                        dialog.setCanceledOnTouchOutside(false)
                    }, 2000 )


                } else{

                    lottieLose.visibility = View.VISIBLE
                    lottieLose.playAnimation()

                    viewModel.hisScore.value = viewModel.hisScore.value!! + 1

                    Handler().postDelayed({

                        XO_Layout.alpha = 1f
                        lottieWin.visibility = View.GONE
                        lottieWin.pauseAnimation()

                        lottieLose.visibility = View.GONE
                        lottieLose.pauseAnimation()

                        val dialog = MaterialDialog(this).show {
                            positiveButton(R.string.agree) {

                                if (type == "Host"){
                                    sendBuffer.add("playagain")
                                }else {
                                    accessoryCommunicator.send("playagain".toByteArray())
                                }

                                viewModel.myPlayAgain.value = true
                            }

                            negativeButton(R.string.disagree) { dialog ->
                                finish()
                            }

                            title(R.string.dialogTitle)

                            message(text = "Play again or you are afraid")
                        }


                        dialog.setCancelable(false)
                        dialog.setCanceledOnTouchOutside(false)

                    }, 2000 )
                }


            }


        })

        viewModel.gameState.observe(this, Observer {
            if (it[0] != '-'){
                cube0.text = it[0].toString()
            }
            if (it[1] != '-'){
                cube1.text = it[1].toString()
            }
            if (it[2] != '-'){
                cube2.text = it[2].toString()
            }
            if (it[3] != '-'){
                cube3.text = it[3].toString()
            }
            if (it[4] != '-'){
                cube4.text = it[4].toString()
            }
            if (it[5] != '-'){
                cube5.text = it[5].toString()
            }
            if (it[6] != '-'){
                cube6.text = it[6].toString()
            }
            if (it[7] != '-'){
                cube7.text = it[7].toString()
            }
            if (it[8] != '-'){
                cube8.text = it[8].toString()
            }
        })

        viewModel.isHostTurn.observe(this, Observer {


            if (it){

                if (type == "Host"){
                    myTurn.visibility = View.VISIBLE
                    hisTurn.visibility = View.GONE
                } else {
                    hisTurn.visibility = View.VISIBLE
                    myTurn.visibility = View.GONE
                }

            } else {

                if (type == "Host"){
                    hisTurn.visibility = View.VISIBLE
                    myTurn.visibility = View.GONE
                } else {
                    myTurn.visibility = View.VISIBLE
                    hisTurn.visibility = View.GONE
                }

            }
        })


        if (type == "Host") {

            Toast.makeText(this , "Host" , Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                CommunicationRunnable().run()
            }

            cube0.setOnClickListener {
                if (cube0.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(0 , 'X')
                    sendBuffer.add("0")
                    viewModel.isHostTurn.value = false
                }
            }

            cube1.setOnClickListener {
                if (cube1.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(1 , 'X')
                    sendBuffer.add("1")
                    viewModel.isHostTurn.value = false
                }
            }

            cube2.setOnClickListener {
                if (cube2.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(2 , 'X')
                    sendBuffer.add("2")
                    viewModel.isHostTurn.value = false
                }
            }

            cube3.setOnClickListener {
                if (cube3.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(3 , 'X')
                    sendBuffer.add("3")
                    viewModel.isHostTurn.value = false
                }
            }

            cube4.setOnClickListener {
                if (cube4.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(4 , 'X')
                    sendBuffer.add("4")
                    viewModel.isHostTurn.value = false
                }
            }

            cube5.setOnClickListener {
                if (cube5.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(5 , 'X')
                    sendBuffer.add("5")
                    viewModel.isHostTurn.value = false
                }
            }

            cube6.setOnClickListener {
                if (cube6.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(6 , 'X')
                    sendBuffer.add("6")
                    viewModel.isHostTurn.value = false
                }
            }

            cube7.setOnClickListener {
                if (cube7.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(7 , 'X')
                    sendBuffer.add("7")
                    viewModel.isHostTurn.value = false
                }
            }

            cube8.setOnClickListener {
                if (cube8.text.toString().isEmpty() && viewModel.isHostTurn.value!!){
                    viewModel.play(8 , 'X')
                    sendBuffer.add("8")
                    viewModel.isHostTurn.value = false
                }
            }


        } else {

            Toast.makeText(this , "Client" , Toast.LENGTH_SHORT).show()


            cube0.setOnClickListener {
                if (cube0.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("0".toByteArray())
                    viewModel.play(0 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube1.setOnClickListener {
                if (cube1.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("1".toByteArray())
                    viewModel.play(1 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube2.setOnClickListener {
                if (cube2.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("2".toByteArray())
                    viewModel.play(2 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube3.setOnClickListener {
                if (cube3.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("3".toByteArray())
                    viewModel.play(3 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube4.setOnClickListener {
                if (cube4.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("4".toByteArray())
                    viewModel.play(4 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube5.setOnClickListener {
                if (cube5.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("5".toByteArray())
                    viewModel.play(5 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube6.setOnClickListener {
                if (cube6.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("6".toByteArray())
                    viewModel.play(6 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube7.setOnClickListener {
                if (cube7.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("7".toByteArray())
                    viewModel.play(7 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            cube8.setOnClickListener {
                if (cube8.text.toString().isEmpty() && !viewModel.isHostTurn.value!!){
                    accessoryCommunicator.send("8".toByteArray())
                    viewModel.play(8 , 'O')
                    viewModel.isHostTurn.value = true
                }
            }

            accessoryCommunicator = object : AccessoryCommunicator(this) {
                override fun onReceive(payload: ByteArray?, length: Int) {
                    
                    runOnUiThread {

                        if (String(payload!!, 0 , length) == "playagain"){
                            viewModel.hisPlayAgain.value = true
                        }

                        if (String(payload, 0 , length) == "disconnected"){
                            Toast.makeText(this@GameActivity , "Haha Coward Escape" , Toast.LENGTH_LONG).show()
                        }

                        if (String(payload, 0 , length) == "0"){
                            viewModel.play(0 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "1"){
                            viewModel.play(1 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "2"){
                            viewModel.play(2 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "3"){
                            viewModel.play(3 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "4"){
                            viewModel.play(4 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "5"){
                            viewModel.play(5 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "6"){
                            viewModel.play(6 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "7"){
                            viewModel.play(7 , 'X')
                            viewModel.isHostTurn.value = false
                        }

                        if (String(payload, 0 , length) == "8"){
                            viewModel.play(8 , 'X')
                            viewModel.isHostTurn.value = false
                        }
                        

                    }


                }

                override fun onError(msg: String) {
                    Toast.makeText(this@GameActivity ,msg , Toast.LENGTH_SHORT).show()
                }

                override fun onConnected() {
                    Toast.makeText(this@GameActivity ,"Connected" , Toast.LENGTH_SHORT).show()
                }

                override fun onDisconnected() {
                 Toast.makeText(this@GameActivity ,"Disconnected" , Toast.LENGTH_SHORT).show()

                }
            }



        }
    }



    private fun resetGame() {
        enableGame()
        viewModel.resetGame()
        XO_Layout.alpha = 1f
        loadingAnim.visibility = View.GONE
        cube0.text = ""
        cube1.text = ""
        cube2.text = ""
        cube3.text = ""
        cube4.text = ""
        cube5.text = ""
        cube6.text = ""
        cube7.text = ""
        cube8.text = ""

    }



    private fun disableGame() {
        cube0.isEnabled = false
        cube1.isEnabled = false
        cube2.isEnabled = false
        cube3.isEnabled = false
        cube4.isEnabled = false
        cube5.isEnabled = false
        cube6.isEnabled = false
        cube7.isEnabled = false
        cube8.isEnabled = false
    }

    private fun enableGame() {
        cube0.isEnabled = true
        cube1.isEnabled = true
        cube2.isEnabled = true
        cube3.isEnabled = true
        cube4.isEnabled = true
        cube5.isEnabled = true
        cube6.isEnabled = true
        cube7.isEnabled = true
        cube8.isEnabled = true
    }


    inner class CommunicationRunnable : Runnable {
        override fun run() {
            val usbManager = getSystemService(USB_SERVICE) as UsbManager
            val device = intent.getParcelableExtra<UsbDevice>("device")
            var endpointIn: UsbEndpoint? = null
            var endpointOut: UsbEndpoint? = null
            val usbInterface = device?.getInterface(0)

            for (i in 0 until device?.getInterface(0)?.endpointCount!!) {

                val endpoint = device.getInterface(0).getEndpoint(i)

                if (endpoint?.direction == UsbConstants.USB_DIR_IN) {
                    endpointIn = endpoint
                }

                if (endpoint?.direction == UsbConstants.USB_DIR_OUT) {
                    endpointOut = endpoint
                }
            }
            if (endpointIn == null) {
//                Toast.makeText(this@GameActivity ,"Input Endpoint not found" , Toast.LENGTH_SHORT).show()
                return
            }
            if (endpointOut == null) {
//                Toast.makeText(this@GameActivity ,"Output Endpoint not found" , Toast.LENGTH_SHORT).show()
                return
            }
            val connection = usbManager.openDevice(device)

            if (connection == null) {
//                Toast.makeText(this@GameActivity ,"Could not open device" , Toast.LENGTH_SHORT).show()
                return
            }

            val claimResult = connection.claimInterface(usbInterface, true)

            if (!claimResult) {
                Toast.makeText(this@GameActivity ,"Could not claim device" , Toast.LENGTH_SHORT).show()


            } else {

                val buff = ByteArray(Constants.BUFFER_SIZE_IN_BYTES)

                while (keepThreadAlive.get()) {

                    val bytesTransferred = connection.bulkTransfer(
                        endpointIn,
                        buff,
                        buff.size,
                        Constants.USB_TIMEOUT_IN_MS
                    )

                    if (bytesTransferred > 0) {

                        runOnUiThread {
                            if (String(buff, 0, bytesTransferred) == "playagain"){
                                viewModel.hisPlayAgain.value = true


                            } else if (String(buff, 0, bytesTransferred) == "disconnected"){
                                Toast.makeText(this@GameActivity , "Haha Coward Escape" , Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.play(String(buff, 0, bytesTransferred).toInt() , 'O')
                                viewModel.isHostTurn.value = true
                            }
                        }

                    }

                    synchronized(Any()) {
                        if (sendBuffer.size > 0) {

                            val sendBuff: ByteArray = sendBuffer.get(0).toString().toByteArray()

                            connection.bulkTransfer(
                                endpointOut,
                                sendBuff,
                                sendBuff.size,
                                Constants.USB_TIMEOUT_IN_MS
                            )

                            sendBuffer.removeAt(0)

                        }
                    }

                }
            }

            connection.releaseInterface(usbInterface)
            connection.close()
        }
    }

    override fun onStop() {
        super.onStop()
        keepThreadAlive.set(false)

        val type = intent.getStringExtra("type")

        if (type == "Host"){
            sendBuffer.add("disconnected")
        } else {
            accessoryCommunicator.send("disconnected".toByteArray())
        }
    }


}