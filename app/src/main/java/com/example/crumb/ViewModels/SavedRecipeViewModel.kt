package com.example.crumb.ViewModels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.crumb.*
import com.example.crumb.Dao.ScheduleDao
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Helpers.RecipeToText
import com.example.crumb.Models.Interval
import com.example.crumb.Models.Schedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.StringBuilder

class SavedRecipeViewModel(application: Application, var  parent_id: Long) :
    AndroidViewModel(application) {

    companion object{
        val TAG = " SAVED_RECIPE_VIEWMODEL"
    }

    val database = DatabaseScheduler.getInstance(application)
    private val intervalDao = database?.getIntervalDao()
    private val scheduleDao: ScheduleDao? = database?.getScheduleDao()
    val start = MutableLiveData<Interval>()
    val notes = MutableLiveData<String>()
    val recipeName = scheduleDao?.getName(parent_id)
    var nameField = ""
    val intervalData = intervalDao?.getAllIntervalsInSchedule(parent_id)
    lateinit var selectedInterval: Interval

    init{
        Log.i(TAG, "parent id = $parent_id")
    }

    fun getStart() {
        viewModelScope.launch {
            val startData = intervalDao?.getStart(parent_id)
            Log.i(TAG, "start data A = ${intervalDao?.getAsList(parent_id)}")
            Log.i(TAG, "start data A = ${intervalDao?.getAsList(parent_id)}")
            if (startData != null) {
                start.postValue(startData)
                Log.i(TAG, "start data B = $startData")
            }
        }
    }

    fun getScheduleNotes() {
        viewModelScope.launch {
            notes.postValue(scheduleDao?.getNotes(parent_id) ?: "No Notes")
        }
    }

    fun updateStep(step: Interval) {
        viewModelScope.launch {
            intervalDao?.update(step)
        }
    }

    fun updateScheduleName(name: String) {
        viewModelScope.launch {
            val newName = StringBuilder()
            var count = scheduleDao?.getCount(name) ?: 0
            if (count > 1) {
                if (name == "") {
                    newName.append("No Name")
                } else {
                    newName.append(name)
                }
                count++
                newName.append("(#$count)")
            } else {
                newName.append(name)
            }
            scheduleDao?.updateScheduleName(parent_id, newName.toString())
        }
    }

    fun updateScheduleNotes(notes: String) {
        viewModelScope.launch {
            scheduleDao?.updateScheduleNotes(parent_id, notes)
        }
    }

    fun update(newTime: Int) {
        viewModelScope.launch {
            val list = intervalData?.value
            if (list != null) {
                var last = newTime
                for (item in list) {
                    Log.d(TAG, " update ${last + item.span}")
                    item.time = last + item.span
                    last = item.time
                }
                intervalDao?.updateAll(list)
            }
        }
    }

    fun duplicate(navController: NavController) {
        viewModelScope.launch {
            val duplicateID = System.currentTimeMillis()
            val schedule = scheduleDao?.getSelected(parent_id)
            val reg = Regex(" \\(# \\d+\\)")
            val oldName = schedule?.name?.replace(reg, "") ?: ""
            val duplicates = scheduleDao?.getCount(oldName) ?: 0
            val myDuplicate = createDuplicate(schedule, duplicates, duplicateID)
            scheduleDao?.insert(myDuplicate)
            if (schedule != null) {
                val children = intervalDao?.getAsList(schedule.id)
                if (children != null) {
                    for (child in children) {
                        child.id = System.currentTimeMillis()
                        child.parentId = myDuplicate.id
                        delay(2)
                    }
                    intervalDao?.insertList(children)
                }
            }
            val bundle = Bundle()
            bundle.putLong("parent_id", duplicateID)
            navController.navigate(R.id.action_savedRecipeFragment_to_intervalFragment, bundle)
        }
    }

    private fun createDuplicate(oldSchedule: Schedule?, count: Int, id: Long): Schedule {
        val reg = Regex(" \\(# \\d+\\)")
        val dtf = DateTimeFormat.forPattern(" dd, YYYY")
        val dateTime = DateTime.now()
        val name = oldSchedule?.name?.replace(reg, "") ?: "No Name"
        return Schedule(
            id,
            name + " (# " + (count + 1) + ")",
            ScheduleViewModel.monthOfYearMap[dateTime.monthOfYear] + dateTime.toString(dtf),
            oldSchedule?.description ?: "No Name",
            oldSchedule?.start ?: 0,
            oldSchedule?.end ?: 0,
            oldSchedule?.duration ?: 0,
            oldSchedule?.steps ?: 0
        )
    }

    fun shareTextVersionOfRecipe(colors: IntArray, context: Context) {
        val textIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, generateTextVersionOfRecipe(colors))
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(textIntent, "Recipe")
        context.startActivity(shareIntent)
    }

    private fun generateTextVersionOfRecipe(colors: IntArray): String {
        val recipeToText =
            RecipeToText(intervalData?.value ?: ArrayList(), colors)
        return recipeToText.getText()
    }


}