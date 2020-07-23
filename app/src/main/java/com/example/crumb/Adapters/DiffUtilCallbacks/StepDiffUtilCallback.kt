package com.example.crumb.Adapters.DiffUtilCallbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.crumb.Models.Interval

class StepDiffUtilCallback(oldList : List<Interval>, newList : List<Interval> ) : DiffUtil.Callback() {
    val oldList = oldList
    val newList = newList

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition))
    }
}