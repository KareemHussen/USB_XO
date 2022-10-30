package com.example.usbxo


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    var gameState = MutableLiveData("---------")
    var is_X_Won = MutableLiveData(false)
    var end = MutableLiveData(false)
    var myScore = MutableLiveData(0)
    var hisScore = MutableLiveData(0)
    var isHostTurn = MutableLiveData(true)
    var disconnected = MutableLiveData(false)
    var myPlayAgain = MutableLiveData(false)
    var hisPlayAgain = MutableLiveData(false)
    var draw = MutableLiveData(false)




    fun play(index : Int , xo : Char ){

        if (gameState.value!![index] == '-'){

            gameState.value = gameState.value!!.substring(0, index) + xo + gameState.value!!.substring(index + 1)

                if    (((gameState.value!![0] == gameState.value!![1]  && gameState.value!![1] == gameState.value!![2]) && (gameState.value!![0] == 'X'))
                    || ((gameState.value!![0] == gameState.value!![3]  && gameState.value!![3] == gameState.value!![6]) && (gameState.value!![0] == 'X'))
                    || ((gameState.value!![0] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![8] &&  (gameState.value!![0] == 'X')))
                    || ((gameState.value!![1] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![7]) && (gameState.value!![1] == 'X'))
                    || ((gameState.value!![2] == gameState.value!![5]  && gameState.value!![5] == gameState.value!![8]) && (gameState.value!![2] == 'X'))
                    || ((gameState.value!![2] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![6]) && (gameState.value!![2] == 'X'))
                    || ((gameState.value!![3] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![5]) && (gameState.value!![3] == 'X'))
                    || ((gameState.value!![6] == gameState.value!![7]  && gameState.value!![7] == gameState.value!![8]) && (gameState.value!![6] == 'X'))
                ) {
                    is_X_Won.value = true
                    end.value = true

                } else  if (((gameState.value!![0] == gameState.value!![1]  && gameState.value!![1] == gameState.value!![2]) && (gameState.value!![0] == 'O'))
                    || ((gameState.value!![0] == gameState.value!![3]  && gameState.value!![3] == gameState.value!![6]) && (gameState.value!![0] == 'O'))
                    || ((gameState.value!![0] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![8]  && (gameState.value!![0] == 'O')))
                    || ((gameState.value!![1] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![7]) && (gameState.value!![1] == 'O'))
                    || ((gameState.value!![2] == gameState.value!![5]  && gameState.value!![5] == gameState.value!![8]) && (gameState.value!![2] == 'O'))
                    || ((gameState.value!![2] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![6]) && (gameState.value!![2] == 'O'))
                    || ((gameState.value!![3] == gameState.value!![4]  && gameState.value!![4] == gameState.value!![5]) && (gameState.value!![3] == 'O'))
                    || ((gameState.value!![6] == gameState.value!![7]  && gameState.value!![7] == gameState.value!![8]) && (gameState.value!![6] == 'O'))
                ) {
                    is_X_Won.value = false
                    end.value = true

                } else if (!gameState.value!!.contains('-')){
                    draw.value = true
                    end.value = true
                }
            }

        
    }

    fun resetGame(){
         gameState.value = "---------"
         is_X_Won.value = false
         end.value = false
         myPlayAgain.value = false
         hisPlayAgain.value = false
         draw.value = false
    }



}