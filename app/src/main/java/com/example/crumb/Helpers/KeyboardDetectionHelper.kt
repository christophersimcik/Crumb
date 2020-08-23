package com.example.crumb.Helpers

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

class KeyboardDetectionHelper(val activity: Activity) {
    val view: View = activity.window.decorView
    private var previousHeight: Int
    private val currentHeight = Rect()
    private lateinit var keyBoardObserver: KeyBoardObserver
    private val viewTreeObserver: ViewTreeObserver

    init {
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
                println()
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