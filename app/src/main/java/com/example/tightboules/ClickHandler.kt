package com.example.tightboules

import android.os.Handler

class ClickHandler {
    var doubleClick = false
    var counter = 0
    var running = false
    val clickObservers  = ArrayList<Click>()

    fun up() {
        System.out.println(" click handler up, counter = " + counter)
        counter ++
    }

    fun down() {
        if (!running) {
            running = true
            Handler().postDelayed(Runnable {
                if (counter > 1) {
                    doubleClick = true
                    notifyClicked()
                    System.out.println(" 2x clicked" + " counter = " + counter)
                } else {
                    doubleClick = false
                    notifyClicked()
                    System.out.println(" 1x clicked" + " counter = " + counter)
                }

                counter = 0
                running = false
            }, 500)
        }
    }

    fun notifyClicked(){
        for(observer in clickObservers){
            observer.onClick(doubleClick)
        }
    }

    fun register(click : Click){
        clickObservers.add(click)
    }

    interface Click{
        fun onClick(doubleClick : Boolean)
    }
}