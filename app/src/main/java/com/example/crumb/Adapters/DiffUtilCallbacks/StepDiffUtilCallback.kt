package com.example.crumb.Adapters.DiffUtilCallbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.crumb.Models.Interval

class StepDiffUtilCallback(private val oldList : List<Interval>, private val newList : List<Interval> ) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}