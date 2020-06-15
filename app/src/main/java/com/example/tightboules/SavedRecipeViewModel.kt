package com.example.tightboules

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.StringBuilder

class SavedRecipeViewModel(application: Application, parent_id: Long) : AndroidViewModel(application) {

    val database = DatabaseScheduler.getInstance(application)
    val intervalDao = database?.getIntervalDao()
    val scheduleDao: ScheduleDao? = database?.getScheduleDao()
    var parentID = parent_id
    val start = MutableLiveData<Interval>()
    val notes = MutableLiveData<String>()
    val recipeName = scheduleDao?.getName(parentID)
    val intervalData = intervalDao?.getAllIntervalsInSchedule(parentID)
    lateinit var selectedInterval : Interval

    fun getStart() {
        viewModelScope.launch {
            start.postValue(intervalDao?.getStart(parentID))
        }
    }

    fun getScheduleNotes(){
        viewModelScope.launch {
           notes.postValue(scheduleDao?.getNotes(parentID) ?: "No Notes")
        }
    }

    fun updateStep(step : Interval){
        viewModelScope.launch{
            intervalDao?.update(step)
        }
    }

    fun updateScheduleName(name : String){
        viewModelScope.launch{
            val newName = StringBuilder()
            var count = scheduleDao?.getCount(name) ?: 0
            if (count > 1) {
                if(name.equals("")){
                    newName.append("No Name")
                }else {
                    newName.append(name)
                }
                count ++
                newName.append("(#" + count + ")")
            }else{
                newName.append(name)
            }
            scheduleDao?.updateScheduleName(parentID,newName.toString())
        }
    }

    fun updateScheduleNotes(notes : String){
        viewModelScope.launch {
            scheduleDao?.updateScheduleNotes(parentID,notes)
        }
    }

    fun update(newTime: Int) {
        viewModelScope.launch {
            val list = intervalData?.value
            if (list != null) {
                var last = newTime
                for (item in list) {
                    item.time = last + item.span
                    last = item.time
                }
                intervalDao?.updateAll(list)
            }
        }
    }

    fun checkforDuplicateNames(name : String) : String{
        val newName =
        viewModelScope.launch {
            val newName = StringBuilder()
            val count = scheduleDao?.getCount(name) ?: 0
            if (count > 0) {
                newName.append(name)
                newName.append("(#" + count + ")")
            }
        }
            return newName.toString()
    }

    fun duplicate(navController : NavController) {
        viewModelScope.launch {
            val duplicateID = System.currentTimeMillis()
            val schedule = scheduleDao?.getSelected(parentID)
            val reg = Regex(" \\(\\# \\d+\\)")
            var oldName = schedule?.name?.replace(reg,"") ?: ""
            val duplicates = scheduleDao?.getCount(oldName) ?: 0
            System.out.println("duplicates = " + duplicates + " oldName = " + oldName)
            val myDuplicate = createDuplicate(schedule, duplicates, duplicateID)
            scheduleDao?.insert(myDuplicate)
            if(schedule != null){
                val children = intervalDao?.getAsList(schedule.id)
                if(children != null) {
                    for (child in children) {
                        child.id = System.currentTimeMillis()
                        System.out.println("child id = " + child.id)
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

    fun createDuplicate(oldSchedule: Schedule?, count: Int, id : Long): Schedule {
        val reg = Regex(" \\(\\# \\d+\\)")
        val dtf = DateTimeFormat.forPattern(" dd, YYYY");
        val dateTime = DateTime.now()
        val name = oldSchedule?.name?.replace(reg,"") ?: "No Name"
        System.out.println(" name = " + name)
        val schedule = Schedule(
             id,
            name + " (# " + (count + 1) + ")",
            ScheduleViewModel.monthOfYearMap.get(dateTime.monthOfYear) + dateTime.toString(dtf),
            oldSchedule?.description ?: "No Name",
            oldSchedule?.start ?: 0,
            oldSchedule?.end ?: 0,
            oldSchedule?.duration ?: 0,
            oldSchedule?.steps ?: 0
        )
        return schedule
    }


}