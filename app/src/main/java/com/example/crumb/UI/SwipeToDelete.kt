package com.example.crumb.UI

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDelete(context : Context) : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.START + ItemTouchHelper.END) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        System.out.println("swiped" + direction)
    }

}