package com.example.crumb

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

class KeyboardDetection(activity: Activity) {
    val activity = activity
    val view: View
    var previousHeight: Int
    val currentHeight = Rect()
    lateinit var keyBoardObserver: KeyBoardObserver
    val viewTreeObserver: ViewTreeObserver

    init {
        view = activity.window.decorView
        view.getWindowVisibleDisplayFrame(currentHeight)
        previousHeight = currentHeight.height()
        viewTreeObserver = view.viewTreeObserver
    }

    fun removeListener(){
        viewTreeObserver.removeOnGlobalLayoutListener {this}
    }

    fun getListener(){
        viewTreeObserver.addOnGlobalLayoutListener {
            view.getWindowVisibleDisplayFrame(currentHeight)
            if (currentHeight.height() > previousHeight) {
                System.out.println("keyboard dismissed")
                keyBoardObserver.keyboardDismissed()
            } else {
                System.out.println("keyboard displayed")
            }
            previousHeight = currentHeight.height()

        }
    }

    interface KeyBoardObserver {
        fun keyboardDismissed(){

        }
    }

    fun registerObserver(keyBoardObserver: KeyBoardObserver) {
        this.keyBoardObserver = keyBoardObserver
    }

}